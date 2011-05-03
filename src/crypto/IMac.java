package crypto;

public interface IMac {
	public byte[] getMac(byte[] input);
	public int getSize();
}
