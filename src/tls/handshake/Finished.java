package tls.handshake;

import java.util.ArrayList;

import tls.State;
import tls.State.ConnectionEnd;
import tls.TLSHandshake;

public class Finished implements IHandshakeMessage {
	private State state;
	
	public Finished(State state, ArrayList<IHandshakeMessage> messages) {
		this.state = state;
	}
	
	public Finished(State state, byte[] value) {
		// TODO: fix this
		this.state = state;
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
		// PRF(master_secret, finished_label, Hash(handshake_messages))
		if(state.getEntityType() == ConnectionEnd.Client)
			return "PRF(master_secret, \"client finished\", Hash(handshake_messages))";
		return "PRF(master_secret, \"server finished\", Hash(handshake_messages))";
	}
	
}
