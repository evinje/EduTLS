package test;

import common.Tools;

import junit.framework.TestCase;
import server.Listener;
import server.PeerSocket;
import tls.TLSEngine;

public class IntegrationTest extends TestCase implements tls.IApplication {

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
}
