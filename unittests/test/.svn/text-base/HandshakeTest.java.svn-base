package test;

import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;
import tls.AlertException;
import tls.Certificate;
import tls.State;
import tls.TLSHandshake;
import tls.handshake.ClientHello;
import tls.handshake.IHandshakeMessage;

public class HandshakeTest extends TestCase {
	State state;
	PeerTestImpl peer;
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		peer = new PeerTestImpl();
		state = new State(peer);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testClientHandshake() throws AlertException {
		peer.setPeerClient(true);
		TLSHandshake handshake = new TLSHandshake(state);
		byte[] helloRequest = { TLSHandshake.HELLO_REQUEST };
		IHandshakeMessage response;
		if(!handshake.hasMoreMessages())
			fail("Expected a message");
		response = handshake.getNextMessage();
		if(handshake.hasMoreMessages()) {
			fail("More messages???");
		}
		// Check for message size below minimum header size 
		try {
			handshake.receive(helloRequest);
			fail("Exception not thrown");
		} catch (AlertException e) {
			if(e.getAlertCode() != AlertException.handshake_failure)
				fail("Wrong error code, expected handshake_failure, got: " + e.getAlertDescription());
		}
		helloRequest = new byte[] { TLSHandshake.HELLO_REQUEST, 0, 0, 0, 1 };
		// Check for wrong reported message size (0)
		try {
			handshake.receive(helloRequest);
			fail("Exception not thrown");
		} catch (AlertException e) {
			if(e.getAlertCode() != AlertException.handshake_failure)
				fail("Wrong error code, expected handshake_failure, got: " + e.getAlertDescription());
		}
		// Check for a valid hello request
		helloRequest = new byte[] { TLSHandshake.HELLO_REQUEST, 0, 0, 0 };
		handshake.receive(helloRequest);
		
		ClientHello clientHello = new ClientHello(response.getByte());
		assertEquals(clientHello.getClientRandom().length,TLSHandshake.RANDOM_SIZE);
	}
	
	public void testServerHandshake() throws AlertException {
		peer.setPeerClient(false);
		TLSHandshake handshake = new TLSHandshake(state);
		byte[] helloRequest = { TLSHandshake.HELLO_REQUEST, 0, 0, 0 };
		byte[] response;
		// Try to send Hello Request from client, only server can send that 
		try {
			handshake.receive(helloRequest);
			fail("Exception not thrown");
		} catch (AlertException e) {
			if(e.getAlertCode() != AlertException.handshake_failure)
				fail("Wrong error code, expected handshake_failure, got: " + e.getAlertDescription());
		}
		helloRequest = new byte[]{ TLSHandshake.CLIENT_KEY_EXCHANGE, 0, 0, 0};
		// Try to send client key exchange before a valid client hello
		try {
			handshake.receive(helloRequest);
			fail("Exception not thrown");
		} catch (AlertException e) {
			if(e.getAlertCode() != AlertException.handshake_failure)
				fail("Wrong error code, expected handshake_failure, got: " + e.getAlertDescription());
		}
		
	}
	
	public void testClientHello() throws AlertException {
		ClientHello clientHello;
		// try to create an invalid client hello
		try {
			clientHello = new ClientHello(new byte[] {}, new byte[] {}, null);
			fail("No exception thrown");
		} catch (AlertException e) {
			if(e.getAlertCode() != AlertException.handshake_failure)
				fail("Wrong error code, expected handshake_failure, got " + e.getAlertDescription());
		}
		byte[] clientRandom = new byte[] {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
		byte[] sessionId = new byte[TLSHandshake.SESSION_SIZE];
		clientHello = new ClientHello(clientRandom, sessionId, null);
//		if(clientHello.getCipherSuites().size() != Globals.cipherSuites.size())
//			fail("Not same size of cipher suites");
//		byte[] clientHelloExpected = new byte[] {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-64,35,0,53,0,47};
//		if(!Tools.compareByteArray(clientHello.getByte(), clientHelloExpected))
//			fail("Client Hello not as expected");
	}
	
//	public void testServerHello() throws AlertException {
//		ClientHello clientHello;
//		int cipherSuiteIndex = Globals.cipherSuites.size()-1;
//		byte[] clientRandom = new byte[] {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
//		byte[] serverRandom = new byte[] {9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9};
//		byte[] sessionId = new byte[] {7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7};
//		ArrayList<CipherSuite> cipherSuites = new ArrayList<CipherSuite>();
//		cipherSuites.add(Globals.cipherSuites.get(cipherSuiteIndex));
//		clientHello = new ClientHello(clientRandom, sessionId, cipherSuites);
//		ServerHello serverHello = new ServerHello(clientHello, serverRandom);
//		assertEquals(serverHello.getChosenCipherSuite(), Globals.cipherSuites.get(cipherSuiteIndex));
//	}

	public void testCertificate() throws UnsupportedEncodingException, AlertException {
		crypto.keyexchange.RSA rsa = new crypto.keyexchange.RSA(512);
		String subject = "JUnit test subject";
		Certificate cert = new Certificate(subject, rsa, new crypto.mac.SHA1());
		Certificate cert2 = new Certificate(cert.getByteValue());
		
		assertEquals(subject, cert2.getSubject());
		assertEquals(cert.getNotValidBefore(), cert2.getNotValidBefore());
		assertEquals(cert.getNotValidAfter(), cert2.getNotValidAfter());
		
	}
}
