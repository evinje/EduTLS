package crypto.mac;

public class None implements crypto.IMac {

	@Override
	public byte[] getMac(byte[] input) {
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
