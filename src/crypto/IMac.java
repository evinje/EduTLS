package crypto;

import java.util.ArrayList;

public interface IMac {
	public byte[] getMac(byte[] input);
	public int getSize();
	public String getName();
	public static ArrayList<IMac> allMacAlgorithms = new ArrayList<IMac>();
}
