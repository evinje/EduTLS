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
	public String toString() {
		return "Change Cipher Spec";
	}

	@Override
	public String getStringValue() {
		return "1";
	}

}
