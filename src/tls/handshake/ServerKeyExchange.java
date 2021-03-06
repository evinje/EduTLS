package tls.handshake;

import common.Tools;

import tls.TLSEngine;
import tls.TLSHandshake;

public class ServerKeyExchange implements IHandshakeMessage {
	byte[] value;
	
	public ServerKeyExchange(ServerHello serverHello) {
		String v = serverHello.getChosenCipherSuite().getKeyExchange().getServerKeyExchangeMessage().toString();
		value = v.getBytes(TLSEngine.ENCODING);
	}
	
	public ServerKeyExchange(byte[] value) {
		this.value = value;
	}
	
	@Override
	public byte[] getByte() {
		return value;
	}

	@Override
	public byte getType() {
		return TLSHandshake.SERVER_KEY_EXCHANGE;
	}

	@Override
	public String toString() {
		return "ServerKeyExchange";
	}

	@Override
	public String getStringValue() {
		return Tools.byteArrayToString(value);
	}

}
