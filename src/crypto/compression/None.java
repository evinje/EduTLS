package crypto.compression;

import common.Tools;

public class None implements crypto.ICompression {
	private boolean enabled = true;
	
	@Override
	public byte[] compress(byte[] input) {
		return input;
	}

	@Override
	public byte[] decompress(byte[] input) {
		return input;
	}

	@Override
	public byte getCompressionId() {
		return 0;
	}

	@Override
	public String getName() {
		return "None";
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void setEnabled(boolean enabled) {
		// Do nothing, this is always enabeld
	}

}
