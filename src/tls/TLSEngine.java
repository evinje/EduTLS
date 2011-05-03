package tls;

import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.management.timer.Timer;

import server.IPeerHost;
import tls.IApplication.STATUS;

import common.Log;
import common.LogEvent;
import common.Tools;

import crypto.ICipher;
import crypto.ICompression;
import crypto.IKeyExchange;
import crypto.IMac;

public class TLSEngine {
	// TLS max record size is 2^14
	public static final int RECORD_SIZE = 16384;
	public static final int HEADER_SIZE = 4;
	public static final int FRAGMENT_SIZE = RECORD_SIZE-HEADER_SIZE;
	public static final Charset ENCODING = Charset.forName("UTF-8");
	public static byte VERSION_TLSv1 = 33;
	
	public static final byte ALERT = 21;
	public static final byte APPLICATION = 23;
	public static final byte HANDSHAKE = 22;
	
//	public static enum ContentType { 
//		Alert (21), Application (23), Handshake (22);
//		int ct;
//		ContentType(int ct) {  this.ct = ct; }
//		public byte getByte() { return (byte)ct; }
//		}
	public static ArrayList<CipherSuite> cipherSuites = getAllCipherSuites();
	
	private IPeerHost peer;	
	private TLSRecord record;
	private TLSHandshake handshake;
	private IApplication app;
	private State state;
	
//	public TLSEngine(IApplication app) {
//		this.app = app;
//	}
	
	public TLSEngine(IPeerHost peer, IApplication app) throws AlertException {
		this.peer = peer;
		this.app = app;
		state = new State(peer);
		if(!peer.isClient())
			handshake = new TLSHandshake(state);
	}
	
	public boolean connect() throws AlertException, InterruptedException {
		LogEvent le = new LogEvent("Connecting to " + peer.getPeerId(),"");
		Log.get().add(le);
		if(!peer.isClient())
			throw new AlertException(0,0,"WHATTA??");
		handshake = new TLSHandshake(state);
		send(new TLSRecord(state, handshake.getNextMessage()));
		int i = 0;
		Timer timer = new Timer();
		timer.start();
		TLSRecord record;
		while(!handshakeFinished() && i < 8) {
			record = peer.read(state);
			if(record != null) {
					receive(record);
			}
			else
				Thread.sleep(100);
			i++;
		}
		le.setDetails(state.getHandshakeLog());
		if(handshake.isFinished()) {
			le.setDetails("Connection successful");
			return true;
		}
		le.setDetails("Connection failed");
		return false;
	}
	
	public boolean handshakeFinished() {
		return handshake.isFinished();
	}
	
	public void receive(TLSRecord record) throws AlertException  {
		Log.get().add(new LogEvent("Received TLSRecord " + record.getContentTypeName(), Tools.byteArrayToString(record.getPlaintext())));
		if(record.getContentType() == ALERT) {
			Tools.printerr("RECEIVED ALERT: " + Tools.byteArrayToString(record.getPlaintext()));
		}
		else if(record.getContentType() == APPLICATION) {
			if(!state.getChangeCipherSpec(state.getEntityType(true)))
				throw new AlertException(AlertException.alert_fatal,AlertException.insufficient_security, "Cipher Spec not changed");
			if(!handshake.isFinished())
				throw new AlertException(AlertException.alert_fatal,AlertException.insufficient_security, "Handshake not finished");
			app.getMessage(record.getPlaintext());
		}
		else if(record.getContentType() == HANDSHAKE) {
			handshake.receive(record.getPlaintext());
			while(handshake.hasMoreMessages()) {
				send(new TLSRecord(state, handshake.getNextMessage()));
			}
		}
		else {
			throw new AlertException(AlertException.alert_fatal, AlertException.illegal_parameter,"Unknown ContentType");
		}
		
	}
	public void send(byte[] message) throws AlertException {
		send(new TLSRecord(state,message,APPLICATION));
	}

	public synchronized void send(TLSRecord record) throws AlertException {
		LogEvent le = new LogEvent("Sending TLSRecord","");
		if(record.getContentType()==APPLICATION)
			Log.get().add(le);
		if(!peer.isConnected()) {
			le.setDetails("Connection lost, reconnecting...");
			if(!peer.reconnect())
				throw new AlertException(AlertException.alert_fatal, AlertException.close_notify,"Cannot connect");
			le.setDetails("Connection established");
		}
		le.setDetails("Sending data: " + Tools.byteArrayToString(record.getCiphertext()));
		peer.write(record);
	}
	
	public State getState() {
		return state;
	}
	
	private static ArrayList<CipherSuite> getAllCipherSuites() {
		ArrayList<CipherSuite> tmpCipherSuites = new ArrayList<CipherSuite>();
		
		
		byte[] value = new byte[] {(byte)0xC0,0x23};
		IMac sha256 = new crypto.mac.SHA256();
		IMac sha1 = new crypto.mac.SHA1();
		ICipher aes = new crypto.cipher.Rijndael();
		ICompression compression = new crypto.compression.None();
		IKeyExchange rsa = new crypto.keyexchange.RSA(512);
		IKeyExchange dh = new crypto.keyexchange.DH(512);
		
		String name = "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256";
		tmpCipherSuites.add(new CipherSuite(name,value, sha256, aes, compression, dh));
		
		name = "TLS_RSA_WITH_AES_256_CBC_SHA";
		value = new byte[] {0x00, 0x35};
		tmpCipherSuites.add(new CipherSuite(name, value, sha1, aes, compression, rsa));
		
		name = "TLS_RSA_WITH_AES_128_CBC_SHA";
		value = new byte[] {0x00, 0x2F};
		tmpCipherSuites.add(new CipherSuite(name, value, sha1, aes, compression, rsa));
		
		return tmpCipherSuites;
	}
	
	public static CipherSuite findCipherSuite(byte[] value) {
		for(CipherSuite sc : cipherSuites)
			if(sc.getValue()[0] == value[0] && sc.getValue()[1] == value[1])
				return sc;
		return null;
	}
}
