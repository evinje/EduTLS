package crypto;


/**
 * The cipher interface for cipher algorithms
 * used in encryption and decryption
 *
 * @author 	Eivind Vinje
 */
public interface ICipher {
	public int getBlockSize();
	public byte[] encrypt(byte[] input);
	public void cipher(byte[] input, int inOff, byte[] output, int outOff);
	public String getAlgorithmName();
	public void init(boolean forEncryption, byte[] key);
}
