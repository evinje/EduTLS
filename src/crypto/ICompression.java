package crypto;

import java.util.ArrayList;

/**
 * The compression interface for compression
 * methods used by TLS
 *
 * @author 	Eivind Vinje
 */
public interface ICompression {
	public byte[] compress(byte[] input);
	public byte[] decompress(byte[] input);
	public byte getCompressionId();
	public boolean isEnabled();
	public void setEnabled(boolean enabled);
	public String getName();
	public static ArrayList<ICompression> allCompressionMethods = new ArrayList<ICompression>();
}
