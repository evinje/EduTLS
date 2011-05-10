package tests;

import common.Tools;

public class AppImpl implements tls.IApplication {
	byte[] lastMessage;
	
	@Override
	public void getMessage(byte[] message) {
		lastMessage = new byte[message.length];
		Tools.byteCopy(message, lastMessage);
	}
	
	public byte[] getLastMessage() {
		return lastMessage;
	}

	@Override
	public void getStatus(STATUS status, String message, String details) {
		// Dont need to do anything with this...
	}

}
