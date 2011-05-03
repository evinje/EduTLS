package crypto;

public interface ICompression {
	public byte[] compress(byte[] input);
	public byte[] decompress(byte[] input);
}
