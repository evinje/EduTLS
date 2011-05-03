package tls.handshake;

import tls.TLSHandshake;

public class ChangeCipherSpec implements IHandshakeMessage {

	@Override
	public byte[] getByte() {
		return new byte[] { 1 };
	}

	@Override
	public byte getType() {
		return TLSHandshake.CHANGE_CIPHER_SPEC;
	}

	@Override
	public String getString() {
		// TODO Auto-generated method stub
		return "Change Cipher Spec";
	}

}
