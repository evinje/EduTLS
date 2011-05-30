package server;

import tls.State;
import tls.TLSRecord;

/**
 * This is an interface for the communication 
 * that EduTLS requires. It defines sending 
 * and receiving of TLSRecord objects, and 
 * other basic communication features like 
 * for instance reconnect and close.
 * 
 * @author Eivind Vinje
 */
public interface IPeerCommunicator {
	public String getPeerId();
	public TLSRecord read(State state);
	public void write(TLSRecord record);
	public void close();
	public boolean reconnect();
	public boolean isClient();
	public boolean isConnected();
}
