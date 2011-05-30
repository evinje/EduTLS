package crypto;

import java.util.ArrayList;

public interface IHash {
	public byte[] getHash(byte[] input);
	public int getSize();
	public String getName();
	public static ArrayList<IHash> allHashAlgorithms = new ArrayList<IHash>();
}
