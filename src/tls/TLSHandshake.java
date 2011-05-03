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
		sessionId = new byte[SESSION_SIZE];
		lastMessage = HELLO_REQUEST;
		responseQueue = new ArrayList<IHandshakeMessage>();
		if(state.getEntityType()==ConnectionEnd.Client)
			clientHandshake();
	}
	
	public boolean isFinished() {
		return isFinished;
	}

	public void receive(byte[] message) throws AlertException {
		if(message.length < HEADER_SIZE) {
			state.addHandshakeLog("Incoming handshake message error: message too short, handshake will abort!");
			throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Message too short");
		}
		//this.message = message;
		//Tools.print("Handshake in: " + Tools.byteArrayToString(message) + " From: " + Thread.currentThread().getStackTrace()[2].getClassName());
		type = message[0];
		int contentSize = (int)(message[1] & 0xFF)*256*256 +(int)(message[2] & 0xFF)*256 + (int)(message[3] & 0xFF);
		if(contentSize != (message.length-HEADER_SIZE)) {
			state.addHandshakeLog("Incoming handshake message error: wrong message size, handshake will abort!");
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
		//int tmpMessageSize = tmpMessage.getByte().length;
//		byte[] header = new byte[HEADER_SIZE];
//		header[0] = tmpMessage.getType();
//		header[1] = (byte)Math.ceil(tmpMessageSize/(256*256));
//		header[2] = (byte)Math.ceil(tmpMessageSize/256);
//		header[3] = (byte)(tmpMessageSize%256);
//		byte[] response = Tools.byteAppend(header, tmpMessage.getByte());
		responseQueue.remove(0);
		
		// Last message sent from server is the finished message.
		state.addHandshakeLog("Sending " + tmpMessage.getString());
		if(state.getEntityType()==ConnectionEnd.Server && tmpMessage.getType()==FINISHED) {
			isFinished=true;
			state.addHandshakeLog("Handshake finished, chosen cipher suite: " + serverHello.getChosenCipherSuite().getName());
		}
		return tmpMessage;
	}

	private void serverHandshake() throws AlertException {
		//Tools.print("serverHandshake() " + type);
		switch(type) {
		case CLIENT_HELLO:
			// Client send ClientHello, Server respond with ServerHello
			state.addHandshakeLog("Received: ClientHello");
			
			if(lastMessage != HELLO_REQUEST && lastMessage != FINISHED)
				throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Unexpected handshake message: " + lastMessage);
			serverRandom = new byte[RANDOM_SIZE];
			genRandom(serverRandom);
			clientHello = new ClientHello(content);
			serverHello = new ServerHello(clientHello, serverRandom);
			responseQueue.add(serverHello);
			serverCertificate = new ServerCertificate("someNickName", serverHello);
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
			state.addHandshakeLog("Received: CertificateVerify");
			// Only used when Client Certificate is sent
			throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Not implemented");
		case CLIENT_KEY_EXCHANGE:
			// First message after ServerHelloDone
			state.addHandshakeLog("Received: ClientKeyExchange");
			if(lastMessage != SERVER_HELLO_DONE)
				throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Unexpected message: " + lastMessage);
			break;
		case CHANGE_CIPHER_SPEC:
			//Tools.printerr("CHANGE_CIPHER_SPEC SERVER");
			state.addHandshakeLog("Received: ChangeCipherSpec");
			state.setChangeCipherSpecClient();
			state.setCipherSuite(serverHello.getChosenCipherSuite());
			break;
		case FINISHED:
			// Client send Finished, Server respond with [ChangeCipherSpec] and Finished
			state.addHandshakeLog("Received: Finished");
			if(lastMessage != CHANGE_CIPHER_SPEC)
				throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Unexpected message: " + lastMessage);
			if(!state.getChangeCipherSpecClient())
				throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Change cipher spec missing");
			responseQueue.add(new ChangeCipherSpec());
			state.setChangeCipherSpecServer();
			serverFinished = new Finished();
			responseQueue.add(serverFinished);
			break;
		default:
			throw new AlertException(AlertException.alert_warning,AlertException.handshake_failure, "Unsupported or unexpected type: "+type);
		}

	}

	private void clientHandshake() throws AlertException {
		//Tools.print("clientHandshake() " + type);
		switch(type) {
		case HELLO_REQUEST:
			// Server send HelloRequest, Client respond with ClientHello
			clientRandom = new byte[RANDOM_SIZE];
			genRandom(clientRandom);
			clientHello = new ClientHello(clientRandom,sessionId,null);
			responseQueue.add(clientHello);
			break;
		case SERVER_HELLO:
			// Server send ServerHello, no response from Client.
			state.addHandshakeLog("Received: ServerHello");
			if(lastMessage != CLIENT_HELLO)
				throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Unexpected message: " + lastMessage);
			serverHello = new ServerHello(content);
			break;
		case CERTIFICATE:
			// Server send Certificate, no response from Client.
			state.addHandshakeLog("Received: Certificate");
			if(lastMessage != SERVER_HELLO)
				throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Unexpected message: " + lastMessage);
			serverCertificate = new ServerCertificate(content);
			break;
		case SERVER_KEY_EXCHANGE:
			// Server send ServerKeyExchange, no response from Client.
			state.addHandshakeLog("Received: ServerKeyExchange");
			if(lastMessage != CERTIFICATE)
				throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Unexpected message: " + lastMessage);
			serverKeyExchange = new ServerKeyExchange(content);
			break;
		case CERTIFICATE_REQUEST:
			state.addHandshakeLog("Received: CertificateRequest");
			// Certificate Request is not implemented
			throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Not implemented");
		case SERVER_HELLO_DONE:
			// Server send ServerHelloDone, Client respond with [ChangeCipherSpec] and Finished.
			state.addHandshakeLog("Received: ServerHelloDone");
			if(lastMessage != SERVER_KEY_EXCHANGE && lastMessage != CERTIFICATE)
				throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Unexpected message: " + lastMessage);
			serverHelloDone = new ServerHelloDone();
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
			//Tools.printerr("CHANGE_CIPHER_SPEC CLIENT");
			state.addHandshakeLog("Received: ChangeCipherSpec");
			state.setChangeCipherSpecServer();
			break;
		case FINISHED:
			// Client send Finished, Server respond with [ChangeCipherSpec] and Finished
			state.addHandshakeLog("Received: Finished");
			if(lastMessage != CHANGE_CIPHER_SPEC)
				throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Unexpected message: " + lastMessage);
			if(!state.getChangeCipherSpecServer())
				throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Missing change cipher spec");
			isFinished=true;
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
