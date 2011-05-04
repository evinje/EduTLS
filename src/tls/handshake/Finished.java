package tls.handshake;

import tls.TLSHandshake;

public class Finished implements IHandshakeMessage {

	public Finished() {
		
	}
	public Finished(byte[] value) {
		// TODO: fix this
	}
	
	@Override
	public byte[] getByte() {
		
		return new byte[] { '1' };
	}

	@Override
	public byte getType() {
		return TLSHandshake.FINISHED;
	}

	@Override
	public String toString() {
		return "Finished";
	}
	
	public String getStringValue() {
		return "not implemented";
	}
	
}
