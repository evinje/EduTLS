package crypto.cipher;

import common.Tools;

public class None implements crypto.ICipher {

	@Override
	public int getBlockSize() {
		return 16;
	}

	@Override
	public String getName() {
		return "None";
	}

	@Override
	public void init(boolean forEncryption, byte[] key) {
		
	}

	@Override
	public void cipher(byte[] input, int inOff, byte[] output, int outOff) {
		Tools.byteCopy(input, output);
	}

	@Override
	public int getKeySize() {
		return 0;
	}

}
