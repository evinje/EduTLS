package tls.handshake;

import tls.TLSHandshake;

public class Finished implements IHandshakeMessage {

	public Finished() {
		
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
	public String getString() {
		return "Finished";
	}
	
}
