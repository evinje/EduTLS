package test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import junit.framework.TestCase;
import server.PeerSocket;

import common.Tools;

public class SocketTest extends TestCase {
	server.Listener listener;
	int THREAD_WAIT = 500;
	
	protected void setUp() { 
		listener = new server.Listener(null);
		try {
			// Need to wait for serversocket thread to execute
			Thread.sleep(THREAD_WAIT);
		} catch (InterruptedException e1) {
			fail("Can't sleep thread (wait for serversocket to bind)");
		}		
   } 
	
	public void testSocketPerformance() {
		long start = System.currentTimeMillis();
		for(int i = 0; i < 5; i++) {
			Tools.print("Test " + i + ": " + (System.currentTimeMillis()-start) + "ms");
			if(!PeerSocket.testConnection("localhost"))
				fail("Didn't work :(");
		}
	}
	
	public void aaatestSocket() {
		Socket s;
		byte[] output = new byte[] {  };
		byte[] inHeader = new byte[tls.TLSEngine.HEADER_SIZE];
		byte[] inContent;
		try {
			s = new Socket("localhost",server.Listener.PORT);
			s.setSoTimeout(1000);
			OutputStream os = s.getOutputStream();
			InputStream is = s.getInputStream();
			
			os.write(output);
			os.flush();
			
			is.read(inHeader);
			if(inHeader[0] != tls.TLSEngine.ALERT)
				fail("Wrong return code");
			int contentSize = inHeader[1];
			inContent = new byte[contentSize];
			is.read(inContent);
			
			s.close();
		} catch (UnknownHostException e) {
			fail("testSocket() - UnknownHostException");
		} catch (SocketException e) {
			fail("testSocket() - SocketException" + e.getMessage());
		} catch (IOException e) {
			fail("testSocket() - IOException");
		}
	}
	
	
	protected void tearDown() {
		listener.close();
		try {
			Thread.sleep(THREAD_WAIT);
		} catch (InterruptedException e1) {
			fail("Can't sleep thread (wait for serversocket to close)");
		}
		try {
			new Socket("localhost", server.Listener.PORT);
			fail("Socket not closed");
		} catch (UnknownHostException e) {
		} catch (IOException e) { }
	}
}
