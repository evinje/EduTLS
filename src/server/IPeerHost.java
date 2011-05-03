package server;

import java.util.ArrayList;

import tls.State;
import tls.TLSRecord;

public interface IPeerHost {
//	public InputStream in();
//	public OutputStream out();
	public String getPeerId();
//	public int available();
//	public int read(byte[] b);
	public TLSRecord read(State state);
	public void write(TLSRecord record);
	public void close();
//	public void flush();
	public boolean reconnect();
	public boolean isClient();
	public boolean isConnected();
}
