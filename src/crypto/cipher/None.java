package crypto.cipher;

import common.Tools;

public class None implements crypto.ICipher {

	@Override
	public int getBlockSize() {
		return 0;
	}

	@Override
	public byte[] encrypt(byte[] input) {
		return input;
	}

	@Override
	public String getAlgorithmName() {
		return "";
	}

	@Override
	public void init(boolean forEncryption, byte[] key) {
		
	}

	@Override
	public void cipher(byte[] input, int inOff, byte[] output, int outOff) {
		Tools.byteCopy(input, output);
	}

}
