package tls;

import crypto.ICipher;
import crypto.ICompression;
import crypto.IKeyExchange;
import crypto.IHash;

public class CipherSuite {
	private IHash macAlg;
	private ICipher cipherAlg;
	private IKeyExchange keyExchangeAlg;
	private String name;
	private byte value;
	
	private boolean isEnabled;
	
	/**
	 * @param name String, name of the cipher suite
	 * @param value byte, the byte value of the cipher suite
	 * @param mac IMac, the mac algorithm
	 * @param cipher ICipher, the cipher algorithm
	 * @param keyEx IKeyexchange, the key exchange algorithm
	 * @param compAlg ICompression, the compression algorithm
	 */
	public CipherSuite(String name, byte value, IHash mac, ICipher cipher, IKeyExchange keyEx) {
		this.name = name;
		this.value = value;
		this.macAlg = mac;
		this.cipherAlg = cipher;
		this.keyExchangeAlg = keyEx;
		isEnabled = true;
	}
	
	public boolean isEnabled() {
		return isEnabled;
	}
	
	public void setEnabled(boolean enabled) {
		isEnabled = enabled;
	}
	
	public String toString() {
		return name;
	}

	public IHash getMac() {
		return macAlg;
	}

	public String getName() {
		return name;
	}
	
	public byte getValue() {
		return value;
	}
	
	public IKeyExchange getKeyExchange() {
		return keyExchangeAlg;
	}
	
	public ICipher getCipher() {
		return cipherAlg;
	}

}
