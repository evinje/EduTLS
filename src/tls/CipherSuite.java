package tls;

import common.Tools;

import crypto.ICipher;
import crypto.ICompression;
import crypto.IKeyExchange;
import crypto.IMac;

public class CipherSuite {
	
	private static final byte[] TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256 = {(byte)0xc0,0x23};
	private static final byte[] TLS_RSA_WITH_AES_256_CBC_SHA = { 0x00, 0x35 };
	private static final byte[] TLS_RSA_WITH_AES_128_CBC_SHA = { 0x00, 0x2F };
	
	public static final byte[][] CIPHER_SUITE = {
		TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256,
		TLS_RSA_WITH_AES_256_CBC_SHA,
		TLS_RSA_WITH_AES_128_CBC_SHA};
	
	private IMac macAlg;
	private ICompression compressionAlg;
	private ICipher cipherAlg;
	private IKeyExchange keyExchangeAlg;
	private String name;
	private byte[] value;
	
	public CipherSuite(String name, byte[] value, IMac mac, ICipher cipher, ICompression compression, IKeyExchange keyExchange) {
		this.name = name;
		this.value = value;
		this.macAlg = mac;
		this.cipherAlg = cipher;
		this.compressionAlg = compression;
		this.keyExchangeAlg = keyExchange;
	}
	
//	public CipherSuite(String name, byte[] value) {
//		this.name = name;
//		this.value = value;
//		this.macAlg = new crypto.mac.None();
//		this.compressionAlg = new crypto.compression.None();
//		this.keyExchangeAlg = new crypto.keyexchange.None();
//	}
	
	public String toString() {
		return name;
	}

	public IMac getMac() {
		return macAlg;
	}

	public ICompression getCompression() {
		return compressionAlg;
	}
	
	public String getName() {
		return name;
	}
	
	public byte[] getValue() {
		return value;
	}
	
	public IKeyExchange getKeyExchange() {
		return keyExchangeAlg;
	}
	
	public ICipher getCipher() {
		return cipherAlg;
	}
}
