package tests;

import java.util.Observable;
import java.util.Observer;

import junit.framework.TestCase;
import server.Listener;
import server.PeerSocket;
import tls.TLSEngine;

import common.Log;
import common.LogEvent;
import common.Tools;

public class IntegrationTest extends TestCase implements tls.IApplication, Observer {

	private Listener listener;
	private String messageIn;

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		listener.close();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Log.get().addObserver(this);
		listener = new Listener(this);
	}

	public void testConnection() throws Exception {
		if(!PeerSocket.testConnection("localhost"))
			fail("Cannot test connection");

		PeerSocket clientPeer = new PeerSocket("localhost");
		if(!clientPeer.isConnected())
			fail("Peer not connected");
		if(!clientPeer.isClient())
			fail("Peer not client");
		
		TLSEngine clientEngine = new TLSEngine(clientPeer, this);		
		clientEngine.connect();
		
		if(!clientEngine.handshakeFinished())
			fail("Handshake failure");
		
		byte[] message = "here is a test".getBytes(TLSEngine.ENCODING);
		Tools.print("message out: " + Tools.byteArrayToString(message));
		clientEngine.send(message);
		Thread.sleep(200);
		if(messageIn == null)
			fail("Message is missing");
		assertEquals(new String(message), messageIn);
	}
	
	@Override
	public void getMessage(byte[] message) {
		messageIn = new String(message,TLSEngine.ENCODING);
		Tools.print("message in: " + Tools.byteArrayToString(message));
	}

	@Override
	public void getStatus(STATUS status, String message, String details) {
		// dont need it..
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		LogEvent le = (LogEvent)arg1;
		Tools.print(le.toString() + " " + le.getDetails());
	}
}
