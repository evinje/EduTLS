package crypto.hash;

public class None implements crypto.IHash {

	@Override
	public byte[] getHash(byte[] input) {
		return new byte[0];
	}

	@Override
	public int getSize() {
		return 0;
	}

	@Override
	public String getName() {
		return "None";
	}

}
