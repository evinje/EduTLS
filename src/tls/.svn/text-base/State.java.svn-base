package tls;

import server.IPeerHost;

import common.LogEvent;
import common.Tools;

import crypto.ICipher;
import crypto.ICompression;
import crypto.IKeyExchange;
import crypto.IMac;
import crypto.PRF;

public class State {
	public static enum ConnectionEnd { Server, Client }
	
	private ICipher cipherAlgorithm;
	private IMac macAlgorithm;
	private ICompression compressionMethod;
	private IKeyExchange keyExchangeAlgorithm;
	
	private byte[] masterSecret = new byte[48];
	private byte[] clientRandom = new byte[32];
	private byte[] serverRandom = new byte[32];

	private byte[] client_write_MAC_key;
	private byte[] server_write_MAC_key;
	private byte[] client_write_encryption_key;
	private byte[] server_write_encryption_key;
	private byte[] client_write_IV;
	private byte[] server_write_IV;
	
	private IPeerHost peer;
	private boolean changeCipherSpecClient;
	private boolean changeCipherSpecServer;
	
	private int sequenceNumberOut;
	private int sequenceNumberIn;
	
	private StringBuilder handshakeLog;
	
	public State(IPeerHost peer) {
		this.peer = peer;
		this.cipherAlgorithm = new crypto.cipher.None();
		this.macAlgorithm = new crypto.mac.None();
		this.compressionMethod = new crypto.compression.None();
		this.keyExchangeAlgorithm = new crypto.keyexchange.None();
		this.sequenceNumberOut = 0;
		this.sequenceNumberIn = 0;
		this.changeCipherSpecClient = false;
		this.changeCipherSpecServer = false;
		handshakeLog = new StringBuilder("Initializing connection state with BulkCipherAlgorithm.null and CompressionMethod.null");
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
	}
	public void setChangeCipherSpecServer() {
		this.changeCipherSpecServer = true;
	}
	
	public String getPeerHost() {
		return peer.getPeerId();
	}
	
	public int getSequenceNumberOut() {
		return sequenceNumberOut++;
	}
	
	public int getSequenceNumberIn() {
		return sequenceNumberIn++;
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
		this.cipherAlgorithm = cipherSuite.getCipher();
		this.keyExchangeAlgorithm = cipherSuite.getKeyExchange();
		this.macAlgorithm = cipherSuite.getMac();
		this.compressionMethod = cipherSuite.getCompression();
		generateKeys();
	}
	
//	public void setCipherAlgorithm(ICipher cipherAlgorithm) {
//		this.cipherAlgorithm = cipherAlgorithm;
//	}

	public ICipher getCipherAlgorithm() {
		return cipherAlgorithm;
	}
	
//	public void setKeyExchangeAlgorithm(IKeyExchange keyExchangeAlgorithm) {
//		this.keyExchangeAlgorithm = keyExchangeAlgorithm;
//	}
	
	public IKeyExchange getKeyExchangeAlgorithm() {
		return keyExchangeAlgorithm;
	}

//	public void setMacAlgorithm(IMac macAlgorithm) {
//		this.macAlgorithm = macAlgorithm;
//	}

	public IMac getMacAlgorithm() {
		return macAlgorithm;
	}

//	public void setCompressionMethod(ICompression compressionMethod) {
//		this.compressionMethod = compressionMethod;
//	}

	public ICompression getCompressionMethod() {
		return compressionMethod;
	}
//
//	public void setMasterSecret(byte[] masterSecret) {
//		this.masterSecret = masterSecret;
//	}

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
	
	public void addHandshakeLog(String log) {
		double time = Math.abs(System.currentTimeMillis() - LogEvent.APP_START);
		StringBuilder sb = new StringBuilder();
		sb.append(time/1000);
		while(sb.length() < 8)
			sb.insert(0," ");
		handshakeLog.append("[" + sb.toString() + "] " + log + LogEvent.NEWLINE);
	}
	
	public String getHandshakeLog() {
		return handshakeLog.toString();
	}
	
	private void generateKeys() {
		// setting up the right size
		client_write_MAC_key = new byte[getMacAlgorithm().getSize()];
		server_write_MAC_key = new byte[getMacAlgorithm().getSize()];
		client_write_encryption_key = new byte[getCipherAlgorithm().getBlockSize()];
		server_write_encryption_key = new byte[getCipherAlgorithm().getBlockSize()];
		client_write_IV = new byte[getCipherAlgorithm().getBlockSize()];
		server_write_IV = new byte[getCipherAlgorithm().getBlockSize()];
		
		// the key block has the total sum of all the read and write keys
		byte[] key_block = new byte[client_write_MAC_key.length*2+client_write_encryption_key.length*2+client_write_IV.length*2];
		
		// seed is a concatenation of server random and client random
		byte[] seed = Tools.byteAppend(getServerRandom(),getClientRandom());
		// use the PRF function to fill the key block
		PRF.generate(getMasterSecret(), "key expansion", seed, key_block);
		int offset = 0;
		Tools.byteCopy(key_block, client_write_MAC_key, offset);
		offset += client_write_MAC_key.length;
		
		Tools.byteCopy(key_block, server_write_MAC_key, offset);
		offset += server_write_MAC_key.length;
		
		Tools.byteCopy(key_block, client_write_encryption_key, offset);
		offset += client_write_encryption_key.length;
		
		Tools.byteCopy(key_block, server_write_encryption_key, offset);
		offset += server_write_encryption_key.length;
		
		Tools.byteCopy(key_block, client_write_IV, offset);
		offset += client_write_IV.length;
		
		Tools.byteCopy(key_block, server_write_IV, offset);
		offset += server_write_IV.length;
	}
	
}
