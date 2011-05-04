package tls.handshake;

import tls.TLSHandshake;

public class ServerHelloDone implements IHandshakeMessage {

	@Override
	public byte[] getByte() {
		return new byte[] { '1' };
	}

	@Override
	public byte getType() {
		return TLSHandshake.SERVER_HELLO_DONE;
	}

	@Override
	public String toString() {
		return "ServerHelloDone";
	}

	@Override
	public String getStringValue() {
		return "<empty message>";
	}

}
