package server;

import tls.State;
import tls.TLSRecord;

public interface IPeerHost {
	public String getPeerId();
	public TLSRecord read(State state);
	public void write(TLSRecord record);
	public void close();
	public boolean reconnect();
	public boolean isClient();
	public boolean isConnected();
}
