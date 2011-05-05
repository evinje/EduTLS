package tls.handshake;

import java.util.ArrayList;

import tls.AlertException;
import tls.CipherSuite;
import tls.TLSEngine;
import tls.TLSHandshake;

import common.LogEvent;
import common.Tools;

public class ServerHello implements IHandshakeMessage {
	private byte[] serverRandom;
	private byte[] sessionId;
	private byte[] compression;
	private CipherSuite chosenCipherSuite;
	private ClientHello clientHello;
	private boolean sessionResume = false;
	
	public ServerHello(byte[] serverHello) throws AlertException {
		serverRandom = new byte[TLSHandshake.RANDOM_SIZE];
		setSessionId(new byte[TLSHandshake.SESSION_SIZE]);

		byte[] ciphers = new byte[serverHello.length - TLSHandshake.SESSION_SIZE - TLSHandshake.RANDOM_SIZE];
		if(ciphers.length%2 != 0)
			throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Invalid length");
		Tools.byteCopy(serverHello, serverRandom);
		Tools.byteCopy(serverHello, getSessionId(), TLSHandshake.RANDOM_SIZE);
		if(Tools.isEmptyByteArray(getSessionId()))
				TLSHandshake.genRandom(getSessionId());

		Tools.byteCopy(serverHello, ciphers, TLSHandshake.RANDOM_SIZE +TLSHandshake.SESSION_SIZE);
		CipherSuite tmpSuite;
		for(int i = 0; i < (ciphers.length/2); i++) {
			tmpSuite = TLSEngine.findCipherSuite(new byte[] {ciphers[2*i],ciphers[2*i+1]});
			if(tmpSuite != null && tmpSuite.isEnabled()) {
				chosenCipherSuite = tmpSuite;
				break;
			}
		}
		if(chosenCipherSuite == null)
			throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Did not find any valid cipher suites");
		compression = new byte[0];
	}
	
	public ServerHello(ClientHello clientHello, byte[] serverRandom) throws AlertException {
		this.serverRandom = serverRandom;
		this.clientHello = clientHello;
		ArrayList<CipherSuite> clientSuites = clientHello.getCipherSuites();
		for(CipherSuite serverCS : TLSEngine.allCipherSuites) {
			for(CipherSuite clientCS : clientSuites) {
				if(Tools.compareByteArray(clientCS.getValue(), serverCS.getValue()) && clientCS.isEnabled()) {
					chosenCipherSuite = serverCS;
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
		compression = new byte[0];
	}
	
	public CipherSuite getChosenCipherSuite() {
		return chosenCipherSuite;
	}
	
	public byte[] getClientRandom() {
		return clientHello.getClientRandom();
	}

	@Override
	public byte[] getByte() {
		byte[] ciphers = chosenCipherSuite.getValue();
		int size = TLSHandshake.RANDOM_SIZE + TLSHandshake.SESSION_SIZE + ciphers.length + compression.length;
		byte[] content = new byte[size];
		int offset = 0;
		Tools.byteCopy(serverRandom, content, offset);
		offset += serverRandom.length;
		Tools.byteCopy(getSessionId(), content, offset);
		offset += getSessionId().length;
		Tools.byteCopy(ciphers, content, offset);
		offset += ciphers.length;
		Tools.byteCopy(compression, content, offset);
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
		tmp += "Compression method: None" + LogEvent.NEWLINE;
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
