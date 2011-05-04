package tls.handshake;

import tls.TLSEngine;

public interface IHandshakeMessage {
	public byte[] getByte();
	public byte getType();
	public String getStringValue();
	public String toString();
}
