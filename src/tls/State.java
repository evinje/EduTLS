package tls;

import server.IPeerCommunicator;

import common.LogEvent;
import common.Tools;

import crypto.ICipher;
import crypto.ICompression;
import crypto.IKeyExchange;
import crypto.IMac;
import crypto.PRF;

public class State {
	public static enum ConnectionEnd { Server, Client }
	
	private CipherSuite cipherSuite;
	private ICompression compressionMethod;
	
	private byte[] preMasterSecret = new byte[46];
	private byte[] masterSecret = new byte[48];
	private byte[] clientRandom = new byte[32];
	private byte[] serverRandom = new byte[32];

	private byte[] client_write_MAC_key;
	private byte[] server_write_MAC_key;
	private byte[] client_write_encryption_key;
	private byte[] server_write_encryption_key;
	private byte[] client_write_IV;
	private byte[] server_write_IV;
	private byte[] sessionId;
	
	private IPeerCommunicator peer;
	private boolean changeCipherSpecClient;
	private boolean changeCipherSpecServer;
	private boolean isResumeSession;

	private LogEvent handshakeLog;
	
	public State(IPeerCommunicator peer) {
		this.peer = peer;
		cipherSuite = new CipherSuite("NONE",(byte)0x0,
				new crypto.mac.None(),
				new crypto.cipher.None(),
				new crypto.keyexchange.None());
		compressionMethod = new crypto.compression.None();
		changeCipherSpecClient = false;
		changeCipherSpecServer = false;
		isResumeSession = false;
		handshakeLog = new LogEvent("Initializing connection state","Remote host is " + peer.getPeerId());
		setSessionId(new byte[TLSHandshake.SESSION_SIZE]);
	}
	
	
	public boolean getChangeCipherSpecClient() {
		return changeCipherSpecClient;
	}
	public boolean getChangeCipherSpecServer() {
		return changeCipherSpecServer;
	}
	
	public boolean getChangeCipherSpec(ConnectionEnd end) {
		if(end == ConnectionEnd.Client)
			return getChangeCipherSpecClient();
		return getChangeCipherSpecServer();
	}
	
	public void setChangeCipherSpecClient() {
		this.changeCipherSpecClient = true;
		handshakeLog.addDetails("Change Cipher Spec for the client entity has been set", true);
	}
	public void setChangeCipherSpecServer() {
		this.changeCipherSpecServer = true;
		handshakeLog.addDetails("Change Cipher Spec for the server entity has been set", true);
	}
	
	public String getPeerHost() {
		return peer.getPeerId();
	}

	public ConnectionEnd getEntityType() {
		return getEntityType(false);
	}
	public ConnectionEnd getEntityType(boolean inverted) {
		if(inverted) {
			if(!peer.isClient())
				return ConnectionEnd.Client;
			return ConnectionEnd.Server;
		}
		if(peer.isClient())
			return ConnectionEnd.Client;
		return ConnectionEnd.Server;
	}

	public void setCipherSuite(CipherSuite cipherSuite) {
		this.cipherSuite = cipherSuite;
		handshakeLog.addDetails("Cipher Suite has been changed; " + cipherSuite.getName(), true);
		generateKeys();
	}

	public ICipher getCipherAlgorithm() {
		return cipherSuite.getCipher();
	}
	
	public IKeyExchange getKeyExchangeAlgorithm() {
		return cipherSuite.getKeyExchange();
	}

	public IMac getMacAlgorithm() {
		return cipherSuite.getMac();
	}

	public void setCompressionMethod(ICompression compressionMethod) {
		this.compressionMethod = compressionMethod;
		handshakeLog.addDetails("Compression method has been changed; " + compressionMethod.getName(), true);
	}

	public ICompression getCompressionMethod() {
		return compressionMethod;
	}

	public byte[] getMasterSecret() {
		return masterSecret;
	}

	public void setClientRandom(byte[] clientRandom) {
		this.clientRandom = clientRandom;
	}

	public byte[] getClientRandom() {
		return clientRandom;
	}

	public void setServerRandom(byte[] serverRandom) {
		this.serverRandom = serverRandom;
	}

	public byte[] getServerRandom() {
		return serverRandom;
	}
	
	public byte[] getEncryptionKeyRead() {
		if(peer.isClient())
			return server_write_encryption_key;
		else
			return client_write_encryption_key;
	}
	
	public byte[] getEncryptionKeyWrite() {
		if(peer.isClient())
			return client_write_encryption_key;
		else
			return server_write_encryption_key;
	}
	

	public byte[] getMacKey() {
		if(peer.isClient())
			return client_write_MAC_key;
		else
			return server_write_MAC_key;
	}
	
	public void addHandshakeLog(LogEvent log) {
		handshakeLog.addLogEvent(log);
	}
	
	public void addHandshakeLog(String info) {
		handshakeLog.addDetails(info, true);
	}
	
	public LogEvent getHandshakeLog() {
		return handshakeLog;
	}
	
	public void setSessionId(byte[] sessionId) {
		this.sessionId = sessionId;
	}

	public byte[] getSessionId() {
		return sessionId;
	}
	
	public CipherSuite getCipherSuite() {
		return cipherSuite;
	}
	
	public void resumeSession(State state) {
		this.cipherSuite = state.getCipherSuite();
		preMasterSecret = state.getPreMasterSecret();
		isResumeSession = true;
		generateKeys();
	}
	
	public boolean isResumeSession() {
		return isResumeSession;
	}

	private void generateKeys() {
		// setting up the right size
		LogEvent keyGeneration = new LogEvent("Generating the key block","");
		client_write_MAC_key = new byte[getMacAlgorithm().getSize()];
		server_write_MAC_key = new byte[getMacAlgorithm().getSize()];
		client_write_encryption_key = new byte[getCipherAlgorithm().getBlockSize()];
		server_write_encryption_key = new byte[getCipherAlgorithm().getBlockSize()];
		client_write_IV = new byte[getCipherAlgorithm().getBlockSize()];
		server_write_IV = new byte[getCipherAlgorithm().getBlockSize()];
		
		// the key block has the total sum of all the read and write keys
		byte[] key_block = new byte[client_write_MAC_key.length*2+client_write_encryption_key.length*2+client_write_IV.length*2];
		keyGeneration.addDetails("Total size of key block: " + key_block.length);
		// seed is a concatenation of server random and client random
		byte[] seed = Tools.byteAppend(getServerRandom(),getClientRandom());
		keyGeneration.addDetails("The seed (server random and client random concatenation): " + Tools.byteArrayToString(seed));
		keyGeneration.addDetails("Pre-master secret (" + preMasterSecret.length + " bytes): " + Tools.byteArrayToString(preMasterSecret));
		// generate master secret from server random and client random
		PRF.generate(preMasterSecret, "master secret", seed, masterSecret);
		// TODO: Not regenerate master secret if it is in the state resume
		keyGeneration.addDetails("Master secret (" + masterSecret.length + " bytes): " + Tools.byteArrayToString(masterSecret));
		// use the PRF function to fill the key block
		PRF.generate(masterSecret, "key expansion", seed, key_block);
		int offset = 0;
		Tools.byteCopy(key_block, client_write_MAC_key, offset);
		offset += client_write_MAC_key.length;
		keyGeneration.addDetails("Client write mac key (" + client_write_MAC_key.length + " bytes): " + Tools.byteArrayToString(client_write_MAC_key));
		
		Tools.byteCopy(key_block, server_write_MAC_key, offset);
		offset += server_write_MAC_key.length;
		keyGeneration.addDetails("Server write mac key (" + server_write_MAC_key.length + " bytes): " + Tools.byteArrayToString(server_write_MAC_key));
		
		Tools.byteCopy(key_block, client_write_encryption_key, offset);
		offset += client_write_encryption_key.length;
		keyGeneration.addDetails("Client write encryption key (" + client_write_encryption_key.length + " bytes): " + Tools.byteArrayToString(client_write_encryption_key));
		
		Tools.byteCopy(key_block, server_write_encryption_key, offset);
		offset += server_write_encryption_key.length;
		keyGeneration.addDetails("Server write encryption key (" + server_write_encryption_key.length + " bytes): " + Tools.byteArrayToString(server_write_encryption_key));
		
		Tools.byteCopy(key_block, client_write_IV, offset);
		offset += client_write_IV.length;
		keyGeneration.addDetails("Cliejnt write IV (" + client_write_IV.length + " bytes): " + Tools.byteArrayToString(client_write_IV));
		
		Tools.byteCopy(key_block, server_write_IV, offset);
		offset += server_write_IV.length;
		keyGeneration.addDetails("Server write IV (" + server_write_IV.length + " bytes): " + Tools.byteArrayToString(server_write_IV));
		
		handshakeLog.addLogEvent(keyGeneration);
	}


	public void setPreMasterSecret(byte[] preMasterSecret) {
		this.preMasterSecret = preMasterSecret;
	}


	public byte[] getPreMasterSecret() {
		return preMasterSecret;
	}
	
}
