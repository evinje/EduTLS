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

/*
 * TLSEngine is the heart of the
 * application, working as a layer
 * between the other host and
 * the application user interface
 * 
 */
public class TLSEngine {
	// TLS max record size is 2^14
	public static final int RECORD_SIZE = 16384;
	public static final int HEADER_SIZE = 4;
	public static final int FRAGMENT_SIZE = RECORD_SIZE-HEADER_SIZE;
	public static final Charset ENCODING = Charset.forName("UTF-8");
	public static byte VERSION_TLSv1 = 33;
	
	// The Record types according to RFC5246
	public static final byte ALERT = 21;
	public static final byte APPLICATION = 23;
	public static final byte HANDSHAKE = 22;
	
	// List of all cipher suites
	public static ArrayList<CipherSuite> cipherSuites = getAllCipherSuites();
	
	private TLSHandshake handshake;
	private IApplication app;
	private State state;
	private IPeerHost peer;	
	
	/**
	 * The TLSEngine constructor
	 * @param peer	IPeerHost, the remote peer
	 * @param app	IApplication, the application utilizing the TLSEngine
	 * @returns	Nothing, it is a constructor
	 */

	public TLSEngine(IPeerHost peer, IApplication app) throws AlertException {
		this.peer = peer;
		this.app = app;
		state = new State(peer);
		if(!peer.isClient())
			handshake = new TLSHandshake(state);
	}
	
	/**
	 * Connect to the remote peer
	 * @returns	boolean Indicating if the connection was established
	 */
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
		le.addDetails(state.getHandshakeLog());
		if(handshake.isFinished()) {
			le.addDetails("Connection successful");
			return true;
		}
		le.addDetails("Connection failed");
		return false;
	}
	
	/**
	 * Method to check if the handshake has
	 * finished successfully
	 * 
	 * @returns	boolean
	 */
	public boolean handshakeFinished() {
		return handshake.isFinished();
	}
	
	/**
	 * Receive an incoming TLSRecord
	 * 
	 * @param record	TLSRecord
	 * @returns	Nothing
	 */
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
	
	/**
	 * @see public synchronized void send(TLSRecord record)
	 */
	public void send(byte[] message) throws AlertException {
		send(new TLSRecord(state,message,APPLICATION));
	}

	/**
	 * Sends a record object to the remote peer
	 * 
	 * @param record TLSRecord, the object to send
	 * @returns	Nothing
	 * @throws AlertException
	 */
	public synchronized void send(TLSRecord record) throws AlertException {
		LogEvent le = new LogEvent("Sending TLSRecord","");
		if(record.getContentType()==APPLICATION)
			Log.get().add(le);
		if(!peer.isConnected()) {
			le.addDetails("Connection lost, reconnecting...");
			if(!peer.reconnect())
				throw new AlertException(AlertException.alert_fatal, AlertException.close_notify,"Cannot connect");
			le.addDetails("Connection established");
		}
		le.addDetails("Sending data: " + Tools.byteArrayToString(record.getCiphertext()));
		peer.write(record);
	}
	
	/**
	 * Returns the state object containing
	 * information about the current connection 
	 * state
	 * 
	 * @returns	State
	 */
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
		
		// TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256
		String name = "ECDHE_AES256_SHA256";
		tmpCipherSuites.add(new CipherSuite(name,value, sha256, aes, compression, dh));
		
		// TLS_RSA_WITH_AES_256_CBC_SHA
		name = "RSA_AES256_SHA-1";
		value = new byte[] {0x00, 0x35};
		tmpCipherSuites.add(new CipherSuite(name, value, sha1, aes, compression, rsa));
		
		// TLS_RSA_WITH_AES_128_CBC_SHA
		name = "RSA_AES128_SHA-1";
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
