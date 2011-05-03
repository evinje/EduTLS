package crypto;

/**
 * The compression interface for compression
 * methods used by TLS
 *
 * @author 	Eivind Vinje
 */
public interface ICompression {
	public byte[] compress(byte[] input);
	public byte[] decompress(byte[] input);
}
