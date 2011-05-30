package tls.handshake;

import java.util.ArrayList;

import tls.AlertException;
import tls.CipherSuite;
import tls.TLSEngine;
import tls.TLSHandshake;

import common.LogEvent;
import common.Tools;

import crypto.ICompression;
import crypto.IRandomGen;
import crypto.random.BlumBlumShub;

public class ServerHello implements IHandshakeMessage {
	private byte[] serverRandom;
	private byte[] sessionId;
	private ICompression chosenCompressionMethod;
	private CipherSuite chosenCipherSuite;
	private ClientHello clientHello;
	private boolean sessionResume = false;
	
	public ServerHello(byte[] serverHello) throws AlertException {
		serverRandom = new byte[TLSHandshake.RANDOM_SIZE];
		setSessionId(new byte[TLSHandshake.SESSION_SIZE]);

		
		int offset = 0;
		Tools.byteCopy(serverHello, serverRandom);
		offset += serverRandom.length;
		Tools.byteCopy(serverHello, getSessionId(), offset);
		offset += getSessionId().length;
		if(Tools.isEmptyByteArray(getSessionId())) {
			IRandomGen random = new BlumBlumShub(TLSHandshake.SESSION_SIZE);
			sessionId = random.randBytes(sessionId.length);
//				TLSHandshake.genenerateRandom(getSessionId());
		}

		byte[] cipher = new byte[1];
		Tools.byteCopy(serverHello, cipher, offset);
		offset += 1;
		CipherSuite tmpSuite;
		for(int i = 0; i < cipher.length; i++) {
			tmpSuite = TLSEngine.findCipherSuite(cipher[i]);
			if(tmpSuite != null && tmpSuite.isEnabled()) {
				chosenCipherSuite = tmpSuite;
				break;
			}
		}
		if(chosenCipherSuite == null)
			throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Did not find any valid cipher suites");
		
		for(ICompression comp : ICompression.allCompressionMethods)
			if(serverHello[serverHello.length-1] == comp.getCompressionId())
				chosenCompressionMethod = comp;
	}
	
	public ServerHello(ClientHello clientHello, byte[] serverRandom) throws AlertException {
		this.serverRandom = serverRandom;
		this.clientHello = clientHello;
		ArrayList<CipherSuite> clientSuites = clientHello.getCipherSuites();
		for(CipherSuite serverSuite : TLSEngine.allCipherSuites) {
			for(CipherSuite clientSuite : clientSuites) {
				if((clientSuite.getValue() == serverSuite.getValue()) && clientSuite.isEnabled()) {
					chosenCipherSuite = serverSuite;
				}
			}
			if(chosenCipherSuite != null)
				break;
		}
		if(chosenCipherSuite == null)
			throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Did not find any valid cipher suites");
		if(Tools.isEmptyByteArray(clientHello.getSessionId()))
			setSessionId(new byte[TLSHandshake.SESSION_SIZE]);
		else {
			setSessionId(clientHello.getSessionId());
			sessionResume = true;
		}
		for(ICompression clientComp : clientHello.getCompressionMethods()) {
			// This actually loops through every compression method 
			// and chooses the last one.
			for(ICompression serverComp : ICompression.allCompressionMethods)
				if(clientComp.getCompressionId() == serverComp.getCompressionId())
					chosenCompressionMethod = clientComp;
		}
		
	}
	
	public CipherSuite getChosenCipherSuite() {
		return chosenCipherSuite;
	}
	
	public ICompression getChosenCompressionMethod() {
		return chosenCompressionMethod;
	}
	
	public byte[] getClientRandom() {
		return clientHello.getClientRandom();
	}

	@Override
	public byte[] getByte() {
		int size = TLSHandshake.RANDOM_SIZE + TLSHandshake.SESSION_SIZE + 2;
		byte[] content = new byte[size];
		int offset = 0;
		Tools.byteCopy(serverRandom, content, offset);
		offset += serverRandom.length;
		Tools.byteCopy(getSessionId(), content, offset);
		offset += getSessionId().length;
		content[offset] = chosenCipherSuite.getValue();
		offset += 1;
		content[offset] = chosenCompressionMethod.getCompressionId(); 
		return content;
	}

	@Override
	public byte getType() {
		return TLSHandshake.SERVER_HELLO;
	}

	@Override
	public String toString() {
		return "ServerHello";
	}
	
	@Override
	public String getStringValue() {
		String tmp = "Server Random: " + Tools.byteArrayToString(serverRandom) + LogEvent.NEWLINE;
		tmp += "Session ID: " + Tools.byteArrayToString(getSessionId()) + LogEvent.NEWLINE;
		tmp += "Compression method: " + chosenCompressionMethod.getName() + LogEvent.NEWLINE;
		tmp += "Chosen Cipher Suite: " + chosenCipherSuite.getName();
		return tmp;
	}

	public byte[] getServerRandom() {
		return serverRandom;
	}
	
	public void setSessionId(byte[] sessionId) {
		this.sessionId = sessionId;
	}

	public byte[] getSessionId() {
		return sessionId;
	}
	
	public boolean isSessionResume() {
		return sessionResume;
	}
}
