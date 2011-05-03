package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import tls.AlertException;
import tls.ConnectionStates;
import tls.IApplication;
import tls.TLSAlert;
import tls.TLSEngine;
import tls.TLSRecord;

import common.Tools;

public class Listener implements Runnable {
	public static final int PORT = 12345;
	public static final byte CONNECTION_TYPE_TEST = '0';
	public static final byte CONNECTION_TYPE_TLS = '1';
	private ServerSocket server;
	private boolean listen;
	//private ConnectionStates connectionStates;
	private IApplication app;
	//	private Thread connectionThread;

	public Listener(IApplication app) {
		//connectionStates = new ConnectionStates();
		listen = true;
		this.app = app;
		Thread t = new Thread(this);
		t.start();
	}

	//	public Listener(IApplication app,IPeerHost peer) {
	//		connectionStates = new ConnectionStates();
	//		this.app = app;
	//		new ConnectionHandler(peer, connectionStates);
	//	}

	@Override
	public void run() {
		try {

			server = new ServerSocket(PORT);
			server.setSoTimeout(0);
			while(listen) {
				new ConnectionHandler(server.accept());
			}
		} catch(SocketException e) {
			// Socket 
			System.err.println("Server cannot bind to port " + PORT + ": " + e.getMessage());
			//System.exit(0);
		} catch (IOException e) {
			System.err.println("Listener thread run() IOException: " + e.getMessage());
		}
	}

	public void close() {
		listen = false;
		try {
			Tools.printerr("Socket closing");
			if(server != null && !server.isClosed())
				server.close();
		} catch (IOException e) {
			System.err.println("IOException Listener.close()");
		} 
	}

	public class ConnectionHandler implements Runnable {
		Socket socket;
		IPeerHost peer;
		ConnectionStates connectionStates;
		TLSEngine tlsengine;

		public ConnectionHandler(Socket socket) {
			this.socket = socket;
			//this.connectionStates = connectionStates;
			this.run();
		}

//		public ConnectionHandler(IPeerHost peer, ConnectionStates connectionStates) {
//			this.peer = peer;
//			this.connectionStates = connectionStates;
//			this.run();
//		}

		@Override
		public void run() {
			try {
				if(socket != null) {
					if(!socket.isConnected())
						return;
					byte[] b = new byte[1];
					socket.getInputStream().read(b);
					if(b[0]==CONNECTION_TYPE_TLS) {
//						Tools.print("New TLS connection incoming...");
						peer = new PeerSocket(socket);
						//if(!connectionStates.stateExist(peer.getPeerId()))
						//connectionStates.addState(tlsengine.getState());
						tlsengine = new TLSEngine(peer, app);
						byte[] input = new byte[TLSEngine.RECORD_SIZE];
						int s = 0;
						byte[] tmp;
//						Timer timer = new Timer();
//						timer.scheduleAtFixedRate(new TimerTask() {
//							int left = (PeerSocket.SOCKET_TIMEOUT/1000);
//							@Override
//							public void run() {
//								if(left<=0)
//									this.cancel();
//								app.getStatus(STATUS.SESSION_TIMEOUT, String.valueOf(left--));
//								System.out.println(left);
//							}
//							
//						},0,1000);
						
						while(true) {
							s = socket.getInputStream().read(input);
							if(input[3] == (s-TLSEngine.RECORD_SIZE)) {
								tmp = new byte[s];
								Tools.byteCopy(input, tmp, 0, s);
								tlsengine.receive(new TLSRecord(tlsengine.getState(),tmp));	
							}
							else {
								// TODO: Error handling...
								int totalReceived = 0;
								while(totalReceived < s) {
									int size = input[totalReceived+3]+TLSEngine.HEADER_SIZE;
									tmp = new byte[size];
									Tools.byteCopy(input, tmp, totalReceived, totalReceived+size);
									tlsengine.receive(new TLSRecord(tlsengine.getState(),tmp));
									totalReceived += size;
								}
							}
						}
					}
					else if(b[0]==CONNECTION_TYPE_TEST) {
//						Tools.print("New incoming test");
						socket.getOutputStream().write(new byte[] { CONNECTION_TYPE_TEST });
					}
					else
						Tools.printerr("Ooops, no type??");
				}
			} catch (AlertException e) {
				try {
					tlsengine.send(new TLSRecord(tlsengine.getState(),new TLSAlert(e.getAlertLevel(), e.getAlertCode())));
				} catch (AlertException e1) {
				}
			} catch (SocketTimeoutException e) {
				// Timeout socket read, no connection from remote peer
			} catch (IOException e) {
				System.err.println("Ooops Socket IOEXception: " + e.getMessage());
			}
			
			finally {
				if(socket != null && socket.isConnected()) {
					try {
						socket.close();
					} catch (IOException e) {
						Tools.printerr("" + e.getMessage());
						e.printStackTrace();
					}
				}
			}
		}

	}

}