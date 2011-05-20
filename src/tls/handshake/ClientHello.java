package tls.handshake;

import java.util.ArrayList;

import tls.AlertException;
import tls.CipherSuite;
import tls.TLSEngine;
import tls.TLSHandshake;

import common.LogEvent;
import common.Tools;
import crypto.ICompression;

public class ClientHello implements IHandshakeMessage {
	byte[] clientRandom;
	private byte[] sessionId;
	byte[] compression;
	ArrayList<CipherSuite> cipherSuites;
	ArrayList<ICompression> compressionMethods;
	
	public ClientHello(byte[] clientHello) throws AlertException {
		clientRandom = new byte[TLSHandshake.RANDOM_SIZE];
		setSessionId(new byte[TLSHandshake.SESSION_SIZE]);
		compressionMethods = new ArrayList<ICompression>();
		cipherSuites = new ArrayList<CipherSuite>();
		int offset = 0;
		Tools.byteCopy(clientHello, clientRandom);
		offset += clientRandom.length;
		Tools.byteCopy(clientHello, getSessionId(), offset);
		offset += getSessionId().length;
		int cipherSize = clientHello[offset];
		offset += 1;
		byte[] ciphers = new byte[cipherSize];
		if(ciphers.length < 1)
			throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "Invalid length");
		Tools.byteCopy(clientHello, ciphers, offset);
		int compressionSize = clientHello.length-offset;
		compression = new byte[compressionSize];
		Tools.byteCopy(clientHello, compression, offset);
		CipherSuite tmpSuite;
		for(int i = 0; i < (ciphers.length); i++) {
			tmpSuite = TLSEngine.findCipherSuite(ciphers[i]);
			if(tmpSuite != null)
				cipherSuites.add(tmpSuite);
		}
		for(ICompression comp : ICompression.allCompressionMethods) {
			for(int i = 0; i < compression.length; i++) {
				if(compression[i] == comp.getCompressionId() && comp.isEnabled())
					compressionMethods.add(comp);
			}
		}
		if(compressionMethods.size()==0)
			compressionMethods.add(new crypto.compression.None());
	}
	
	public ClientHello(byte[] clientRandom, byte[] sessionId) throws AlertException {
		if(clientRandom.length != TLSHandshake.RANDOM_SIZE)
			throw new AlertException(AlertException.alert_fatal,AlertException.handshake_failure, "No available cipher suites");
		
		cipherSuites = new ArrayList<CipherSuite>();
		compressionMethods = new ArrayList<ICompression>();
		for(CipherSuite cs : TLSEngine.allCipherSuites) {
			if(cs.isEnabled())
				cipherSuites.add(cs);
			}
		this.clientRandom = clientRandom;
		if(sessionId == null)
			this.setSessionId(new byte[TLSHandshake.SESSION_SIZE]);
		else
			this.setSessionId(sessionId);
		for(ICompression comp : ICompression.allCompressionMethods) {
			if(comp.isEnabled())
				compressionMethods.add(comp); 
		}
		byte[] tmpComp = new byte[compressionMethods.size()];
		int i = 0;
		for(ICompression comp : compressionMethods) {
			if(comp.isEnabled()) {
				tmpComp[i] = comp.getCompressionId();
				i++;
			}
		}
		compression = new byte[i];
		Tools.byteCopy(tmpComp, compression);
	}
	
	
	public byte[] getClientRandom() {
		return clientRandom;
	}
	
	public ArrayList<CipherSuite> getCipherSuites() {
		return cipherSuites;
	}
	
	public ArrayList<ICompression> getCompressionMethods() {
		return compressionMethods;
	}

	@Override
	public byte[] getByte() {
		byte[] tmpciphers = new byte[cipherSuites.size()];
		int i = 0;
		for(CipherSuite cs : cipherSuites) {
			if(cs.isEnabled()) {
				tmpciphers[i] = cs.getValue();
				i++;
			}
		}
		byte[] ciphers = new byte[i+1];
		ciphers[0] = (byte)tmpciphers.length;
		System.arraycopy(tmpciphers, 0, ciphers, 1, ciphers.length-1);
		byte[] content = new byte[clientRandom.length+getSessionId().length+ciphers.length+compression.length];
		int offset = 0;
		Tools.byteCopy(clientRandom, content, offset);
		offset += clientRandom.length;
		Tools.byteCopy(getSessionId(), content, offset);
		offset += getSessionId().length;
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
		tmp += "Session ID: " + Tools.byteArrayToString(getSessionId()) + LogEvent.NEWLINE;
		tmp += "Compression method: " + LogEvent.NEWLINE;
		for(ICompression c : compressionMethods) {
			tmp += LogEvent.INDENT + c.getName() + LogEvent.NEWLINE;
		}		
		tmp += "Cipher Suites: " + LogEvent.NEWLINE;
		for(CipherSuite s : cipherSuites) {
			tmp += LogEvent.INDENT + s.getName() + LogEvent.NEWLINE;
		}
		return tmp;
	}

	public void setSessionId(byte[] sessionId) {
		this.sessionId = sessionId;
	}

	public byte[] getSessionId() {
		return sessionId;
	}
}
