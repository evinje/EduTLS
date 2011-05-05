package tls;

import java.security.SecureRandom;
import java.util.ArrayList;

import tls.State.ConnectionEnd;
import tls.handshake.ChangeCipherSpec;
import tls.handshake.ClientHello;
import tls.handshake.ClientKeyExchange;
import tls.handshake.Finished;
import tls.handshake.IHandshakeMessage;
import tls.handshake.ServerCertificate;
import tls.handshake.ServerHello;
import tls.handshake.ServerHelloDone;
import tls.handshake.ServerKeyExchange;

import common.Log;
import common.LogEvent;
import common.Tools;

public class TLSHandshake {
	public static final byte HELLO_REQUEST = 0;
	public static final byte CLIENT_HELLO = 1;
	public static final byte SERVER_HELLO = 2;
	public static final byte CERTIFICATE = 11;
	public static final byte SERVER_KEY_EXCHANGE = 12;
	public static final byte CERTIFICATE_REQUEST = 13;
	public static final byte SERVER_HELLO_DONE = 14;
	public static final byte CERTIFICATE_VERIFY = 15;
	public static final byte CLIENT_KEY_EXCHANGE = 16;
	public static final byte FINISHED = 20;
	
	public static final byte CHANGE_CIPHER_SPEC = 99;

	public static final int HEADER_SIZE = 4;
	public static final int SESSION_SIZE = 16;
	public static final int RANDOM_SIZE = 28;
	
	private static SecureRandom sr = new SecureRandom();

	private State state;
	private byte[] clientRandom;
	private byte[] serverRandom;
	private byte[] preMasterSecret;
	private byte[] sessionId;

	private byte lastMessage;
	//private byte[] message;
	private byte type;
	private byte[] content;
	private ArrayList<IHandshakeMessage> responseQueue;
	private boolean isFinished = false;
	
	private ClientHello clientHello;
	private ServerHello serverHello;
	private ServerCertificate serverCertificate;
	private ServerKeyExchange serverKeyExchange;
	private ServerHelloDone serverHelloDone;
	private Finished serverFinished;
	private ClientKeyExchange clientKeyExchange;
	private Finished clientFinished;


	public TLSHandshake(State state) throws AlertException {
		this.state = state;
		state.addHandshakeLog("Starting " + state.getEntityType() + " handshake");
		type = HELLO_REQUEST;
		sessionId = state.getSessionId();
		lastMessage = HELLO_REQUEST;
		responseQueue = new ArrayList<IHandshakeMessage>();
		if(state.getEntityType()==ConnectionEnd.Client)
			clientHandshake();
	}
	
	public void initNewConnection() throws AlertException {
		type = HELLO_REQUEST;
		sessionId = state.getSessionId();
		lastMessage = HELLO_REQUEST;
		if(state.getEntityType()==ConnectionEnd.Client)
			clientHandshake();
	}
	
	public boolean isFinished() {
		return isFinished;
	}

	public void receive(byte[] message) throws AlertException {
		if(message.length < HEADER_SIZE) {
			state.addHandshakeLog(new LogEvent("Incoming handshake message error", "message too short, handshake will abort!"));
			throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Message too short");
		}
		//this.message = message;
		//Tools.print("Handshake in: " + Tools.byteArrayToString(message) + " From: " + Thread.currentThread().getStackTrace()[2].getClassName());
		type = message[0];
		int contentSize = (int)(message[1] & 0xFF)*256*256 +(int)(message[2] & 0xFF)*256 + (int)(message[3] & 0xFF);
		if(contentSize != (message.length-HEADER_SIZE)) {
			state.addHandshakeLog(new LogEvent("Incoming handshake message error","wrong message size, handshake will abort!"));
			throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Wrong message size: " + contentSize + " (expected " + (message.length-HEADER_SIZE) + ")");
		}
		content = new byte[message.length-HEADER_SIZE];
		Tools.byteCopy(message, content, HEADER_SIZE);

		if(state.getEntityType() == ConnectionEnd.Client)
			clientHandshake();
		else if(state.getEntityType() == ConnectionEnd.Server)
			serverHandshake();
		else
			throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Not client nor server??");
		lastMessage = type;
	}

	public boolean hasMoreMessages() {
		return (responseQueue.size()>0);
	}

	public IHandshakeMessage getNextMessage() throws AlertException {
		if(!hasMoreMessages())
			throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Queue empty");
		IHandshakeMessage tmpMessage = responseQueue.get(0);
		lastMessage = tmpMessage.getType();
		responseQueue.remove(0);
		// Last message sent from server is the finished message.
		state.addHandshakeLog(new LogEvent("Sending " + tmpMessage.toString(),tmpMessage.getStringValue()));
		if(state.getEntityType()==ConnectionEnd.Server && tmpMessage.getType()==FINISHED) {
			isFinished=true;
			state.addHandshakeLog("Handshake finished, chosen cipher suite: " + serverHello.getChosenCipherSuite().getName());
		}
		return tmpMessage;
	}

	private void serverHandshake() throws AlertException {
		switch(type) {
		case CLIENT_HELLO:
			// Client send ClientHello, Server respond with ServerHello
			if(lastMessage != HELLO_REQUEST && lastMessage != FINISHED)
				throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Unexpected handshake message: " + lastMessage);
			serverRandom = new byte[RANDOM_SIZE];
			genRandom(serverRandom);
			clientHello = new ClientHello(content);
			state.addHandshakeLog(new LogEvent("Received ClientHello",clientHello.getStringValue()));
			serverHello = new ServerHello(clientHello, serverRandom);
			state.setServerRandom(serverRandom);
			state.setClientRandom(clientHello.getClientRandom());
			if(serverHello.isSessionResume()) {
				// TODO: Check and set data
				State tmpState = TLSEngine.findState(serverHello.getSessionId());
				if(tmpState != null) {
					state.resumeSession(tmpState);
					state.addHandshakeLog("Session resume successful");
				}
				else {
					state.addHandshakeLog("Session resume failed. Client provided session id, but was not valid");
					genRandom(sessionId);
					serverHello.setSessionId(sessionId);
				}
			}

			sessionId = serverHello.getSessionId();
			state.setSessionId(sessionId);
			responseQueue.add(serverHello);
			serverCertificate = new ServerCertificate(state.getPeerHost(), serverHello);
			responseQueue.add(serverCertificate);
			// only if DHE_DSS, DHE_RSA, DH_anon
			if(serverHello.getChosenCipherSuite().getKeyExchange().requireServerKeyExchange()) {
				serverKeyExchange = new ServerKeyExchange(serverHello);
				responseQueue.add(serverKeyExchange);
			}
			serverHelloDone = new ServerHelloDone();
			responseQueue.add(serverHelloDone);
			break;
		case CERTIFICATE_VERIFY: 
			state.addHandshakeLog(new LogEvent("Received CertificateVerify", "Handshake failure, not implemented"));
			// Only used when Client Certificate is sent
			throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Not implemented");
		case CLIENT_KEY_EXCHANGE:
			// First message after ServerHelloDone
			clientKeyExchange = new ClientKeyExchange(content);
			state.addHandshakeLog(new LogEvent("Received ClientKeyExchange",clientKeyExchange.getStringValue()));
			if(lastMessage != SERVER_HELLO_DONE)
				throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Unexpected message: " + lastMessage);
			break;
		case CHANGE_CIPHER_SPEC:
			state.addHandshakeLog(new LogEvent("Received ChangeCipherSpec", Tools.byteArrayToString(content)));
			state.setChangeCipherSpecClient();
			state.setCipherSuite(serverHello.getChosenCipherSuite());
			break;
		case FINISHED:
			// Client send Finished, Server respond with [ChangeCipherSpec] and Finished
			if(lastMessage != CHANGE_CIPHER_SPEC)
				throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Unexpected message: " + lastMessage);
			if(!state.getChangeCipherSpecClient())
				throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Change cipher spec missing");
			responseQueue.add(new ChangeCipherSpec());
			clientFinished = new Finished(content);
			state.addHandshakeLog(new LogEvent("Received Finished", clientFinished.getStringValue()));
			state.setChangeCipherSpecServer();
			serverFinished = new Finished();
			responseQueue.add(serverFinished);
			break;
		default:
			throw new AlertException(AlertException.alert_warning,AlertException.handshake_failure, "Unsupported or unexpected type: "+type);
		}

	}

	private void clientHandshake() throws AlertException {
		switch(type) {
		case HELLO_REQUEST:
			// Server send HelloRequest, Client respond with ClientHello
			clientRandom = new byte[RANDOM_SIZE];
			genRandom(clientRandom);
			state.setClientRandom(clientRandom);
			State tmpState = TLSEngine.findState(state.getPeerHost());
			if(tmpState != null)
				sessionId = tmpState.getSessionId();
			clientHello = new ClientHello(clientRandom, sessionId, TLSEngine.allCipherSuites);
			responseQueue.add(clientHello);
			break;
		case SERVER_HELLO:
			// Server send ServerHello, no response from Client.
			
			if(lastMessage != CLIENT_HELLO)
				throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Unexpected message: " + lastMessage);
			serverHello = new ServerHello(content);
			if(!Tools.isEmptyByteArray(sessionId) && Tools.compareByteArray(sessionId, serverHello.getSessionId())) {
				// TODO This is a session resume
				state.setServerRandom(serverHello.getServerRandom());
				state.resumeSession(TLSEngine.findState(sessionId));
				state.addHandshakeLog("Session resume successful");
			}
			else {
				sessionId = serverHello.getSessionId();
				state.setSessionId(sessionId);
			}
			state.addHandshakeLog(new LogEvent("Received ServerHello", serverHello.getStringValue()));
			break;
		case CERTIFICATE:
			// Server send Certificate, no response from Client.
			if(lastMessage != SERVER_HELLO)
				throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Unexpected message: " + lastMessage);
			serverCertificate = new ServerCertificate(content);
			state.addHandshakeLog(new LogEvent("Received Certificate", serverCertificate.getStringValue()));
			break;
		case SERVER_KEY_EXCHANGE:
			// Server send ServerKeyExchange, no response from Client.
			if(lastMessage != CERTIFICATE)
				throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Unexpected message: " + lastMessage);
			serverKeyExchange = new ServerKeyExchange(content);
			state.addHandshakeLog(new LogEvent("Received ServerKeyExchange",serverKeyExchange.getStringValue()));
			break;
		case CERTIFICATE_REQUEST:
			state.addHandshakeLog(new LogEvent("Received CertificateRequest","Handshake failure, not implemented"));
			// Certificate Request is not implemented
			throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Not implemented");
		case SERVER_HELLO_DONE:
			// Server send ServerHelloDone, Client respond with [ChangeCipherSpec] and Finished.
			if(lastMessage != SERVER_KEY_EXCHANGE && lastMessage != CERTIFICATE)
				throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Unexpected message: " + lastMessage);
			serverHelloDone = new ServerHelloDone();
			state.addHandshakeLog(new LogEvent("Received ServerHelloDone",serverHelloDone.getStringValue()));
			if(serverHello.getChosenCipherSuite().getKeyExchange().requireServerKeyExchange())
				clientKeyExchange = new ClientKeyExchange(serverHello.getChosenCipherSuite().getKeyExchange());
			else {
				preMasterSecret = new byte[RANDOM_SIZE];
				genRandom(preMasterSecret);
				clientKeyExchange = new ClientKeyExchange(preMasterSecret);
			}
			responseQueue.add(clientKeyExchange);
			responseQueue.add(new ChangeCipherSpec());
			state.setChangeCipherSpecClient();
			state.setCipherSuite(serverHello.getChosenCipherSuite());
			clientFinished = new Finished();
			responseQueue.add(clientFinished);
			break;
		case CHANGE_CIPHER_SPEC:
			state.addHandshakeLog(new LogEvent("Received ChangeCipherSpec", Tools.byteArrayToString(content)));
			state.setChangeCipherSpecServer();
			break;
		case FINISHED:
			// Client send Finished, Server respond with [ChangeCipherSpec] and Finished
			if(lastMessage != CHANGE_CIPHER_SPEC)
				throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Unexpected message: " + lastMessage);
			if(!state.getChangeCipherSpecServer())
				throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Missing change cipher spec");
			serverFinished = new Finished(content);
			isFinished=true;
			state.addHandshakeLog(new LogEvent("Received Finished", serverFinished.getStringValue()));
			state.addHandshakeLog("Handshake finished, chosen cipher suite: " + serverHello.getChosenCipherSuite().getName());
			break;
		default:
			throw new AlertException(AlertException.alert_warning,AlertException.unexpected_message, "Unsupported type: " + type);
		}
	}

	public static void genRandom(byte[] input) {
		sr.nextBytes(input);
	}

}
