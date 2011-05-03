package crypto.compression;

import common.Tools;

public class None implements crypto.ICompression {

	@Override
	public byte[] compress(byte[] input) {
		byte[] tmp = new byte[input.length];
		Tools.byteCopy(input, tmp);
		return tmp;
	}

	@Override
	public byte[] decompress(byte[] input) {
		byte[] tmp = new byte[input.length];
		Tools.byteCopy(input, tmp);
		return tmp;
	}

}
