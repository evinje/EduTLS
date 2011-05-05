package tls.handshake;

import java.util.ArrayList;

import tls.AlertException;
import tls.CipherSuite;
import tls.TLSEngine;
import tls.TLSHandshake;

import common.LogEvent;
import common.Tools;

public class ClientHello implements IHandshakeMessage {
	byte[] clientRandom;
	byte[] sessionId;
	byte[] compression;
	ArrayList<CipherSuite> cipherSuites;
	
	public ClientHello(byte[] clientHello) throws AlertException {
		clientRandom = new byte[TLSHandshake.RANDOM_SIZE];
		sessionId = new byte[TLSHandshake.SESSION_SIZE];
		byte[] ciphers = new byte[clientHello.length - TLSHandshake.HEADER_SIZE - TLSHandshake.SESSION_SIZE - TLSHandshake.RANDOM_SIZE];
		cipherSuites = new ArrayList<CipherSuite>();
		if(ciphers.length%2 != 0)
			throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Invalid length");
		Tools.byteCopy(clientHello, clientRandom, TLSHandshake.HEADER_SIZE);
		Tools.byteCopy(clientHello, sessionId, TLSHandshake.HEADER_SIZE + TLSHandshake.RANDOM_SIZE);
		Tools.byteCopy(clientHello, ciphers, TLSHandshake.HEADER_SIZE + TLSHandshake.RANDOM_SIZE + TLSHandshake.SESSION_SIZE);
		CipherSuite tmpSuite;
		for(int i = 0; i < (ciphers.length/2); i++) {
			tmpSuite = TLSEngine.findCipherSuite(new byte[] {ciphers[2*i],ciphers[2*i+1]});
			if(tmpSuite != null)
				cipherSuites.add(tmpSuite);
		}
		
		compression = new byte[0];
	}
	
	public ClientHello(byte[] clientRandom, byte[] sessionId, ArrayList<CipherSuite> cipherSuites) throws AlertException {
		if(clientRandom.length != TLSHandshake.RANDOM_SIZE)
			throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "No available cipher suites");
		if(cipherSuites==null || cipherSuites.size()==0)
			getAllCipherSuites();
		else
			this.cipherSuites = (ArrayList<CipherSuite>) cipherSuites.clone();
		this.clientRandom = clientRandom;
		if(sessionId == null)
			this.sessionId = new byte[TLSHandshake.SESSION_SIZE];
		else
			this.sessionId = sessionId;
		compression = new byte[0];
	}
	
	private void getAllCipherSuites() {
		cipherSuites = (ArrayList<CipherSuite>) TLSEngine.cipherSuites.clone();
	}
	
	public byte[] getClientRandom() {
		return clientRandom;
	}
	
	public ArrayList<CipherSuite> getCipherSuites() {
		return cipherSuites;
	}

	@Override
	public byte[] getByte() {
//		if(cipherSuites.size()==0)
//			throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "No available cipher suites");
		byte[] ciphers = new byte[cipherSuites.size()*2];
		int i = 0;
		for(CipherSuite cs : cipherSuites) {
			ciphers[i] = cs.getValue()[0];
			ciphers[i+1] = cs.getValue()[1];
			i+=2;
		}
		byte[] content = new byte[clientRandom.length+sessionId.length+ciphers.length+compression.length];
		int offset = 0;
		Tools.byteCopy(clientRandom, content, offset);
		offset += clientRandom.length;
		Tools.byteCopy(sessionId, content, offset);
		offset += sessionId.length;
		Tools.byteCopy(ciphers, content, offset);
		offset += ciphers.length;
		Tools.byteCopy(compression, content, offset);
		return content;
	}

	@Override
	public byte getType() {
		return TLSHandshake.CLIENT_HELLO;
	}

	@Override
	public String toString() {
		return "ClientHello";
	}

	@Override
	public String getStringValue() {
		String tmp = "Client Random: " + Tools.byteArrayToString(clientRandom) + LogEvent.NEWLINE;
		tmp += "Session ID: " + Tools.byteArrayToString(sessionId) + LogEvent.NEWLINE;
		tmp += "Compression method: None" + LogEvent.NEWLINE;
		tmp += "Cipher Suites: " + LogEvent.NEWLINE;
		for(CipherSuite s : cipherSuites) {
			tmp += LogEvent.INDENT + s.getName() + LogEvent.NEWLINE;
		}
		return tmp;
	}
}
