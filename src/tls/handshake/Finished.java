package tls.handshake;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import tls.State;
import tls.State.ConnectionEnd;
import tls.TLSHandshake;

import common.LogEvent;
import common.Tools;

public class Finished implements IHandshakeMessage {
	private State state;
	private ArrayList<IHandshakeMessage> messages;
	private byte[] prfvalue;
	private ArrayList<String> handshakeMsgString;
	
	/**
	 * Creates the finished message from the 
	 * previous handshake messages
	 * 
	 * @param state The current connection state
	 * @param messages All handshake messages
	 */
	public Finished(State state, ArrayList<IHandshakeMessage> messages) {
		Tools.print(messages.size() + "");
		this.state = state;
		this.messages = messages;
		try {
			calculatePRF(state.getEntityType(false).toString() + " finished");
		} catch (IOException e) {
			state.addHandshakeLog("Error when creating the Finished message");
		}
	}
	
	/**
	 * Verify the received finished message 
	 * 
	 * @param state The current connection state
	 * @param messages All handshake messages
	 */
	public Finished(State state, ArrayList<IHandshakeMessage> messages, byte[] value) {
		Tools.print(messages.size() + "");
		this.state = state;
		this.messages = messages;
		try {
			calculatePRF(state.getEntityType(true).toString() + " finished");
		} catch (IOException e) {
			state.addHandshakeLog("Error when creating the Finished message");
		}
		if(!Tools.compareByteArray(prfvalue, value))
			state.addHandshakeLog("Attention! The incoming handshake message is invalid!!");
	}
	
	/**
	 * Calculates the PRF value of all
	 * handshake messages, the label, 
	 * and the master secret for the
	 * current connection state 
	 * 
	 * @param label The label to the PRF method
	 */
	private void calculatePRF(String label) throws IOException {
		Tools.print(messages.size() + "");
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		handshakeMsgString = new ArrayList<String>();
		for(IHandshakeMessage m : messages) {
			buf.write(m.getByte());
			handshakeMsgString.add(m.toString());
		}
		byte[] value = new byte[buf.size()];
		value = buf.toByteArray();
		prfvalue = new byte[state.getVerifyDataLength()];
		crypto.PRF.generate(state.getMasterSecret(), label, value, prfvalue);
	}
	
	@Override
	public byte[] getByte() {
		return prfvalue;
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
		StringBuilder sb = new StringBuilder();
		if(state.getEntityType() == ConnectionEnd.Client)
			sb.append("PRF(master_secret, \"client finished\", Hash(handshake_messages))");
		else 
			sb.append("PRF(master_secret, \"server finished\", Hash(handshake_messages))");
		sb.append(LogEvent.NEWLINE + LogEvent.NEWLINE);
		sb.append("Where master_secret = " + Tools.byteArrayToString(state.getMasterSecret()));
		sb.append(LogEvent.NEWLINE + " and handshake messages = " + LogEvent.NEWLINE);
		for(String s : handshakeMsgString)
			sb.append(LogEvent.INDENT + s + LogEvent.NEWLINE);
		return sb.toString();
	}
	
}
