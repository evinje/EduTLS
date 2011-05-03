package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import tls.AlertException;
import tls.State;
import tls.TLSEngine;
import tls.TLSRecord;

import common.Tools;

public class PeerSocket implements IPeerHost {
	public static int SOCKET_TIMEOUT = 5000;
	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private String host;
	private boolean isClient;

	public static boolean testConnection(String host) {
		try {
			Socket tmpSocket = new Socket(host, Listener.PORT);
			tmpSocket.setSoTimeout(SOCKET_TIMEOUT);
			tmpSocket.getOutputStream().write(new byte[] { Listener.CONNECTION_TYPE_TEST });
			byte[] b = new byte[1];
			tmpSocket.getInputStream().read(b);
			tmpSocket.close();
			if(b[0]!=Listener.CONNECTION_TYPE_TEST)
				return false;
			// host is alive :D
		} catch (UnknownHostException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public PeerSocket(Socket socket) throws IOException {
		this.socket = socket;
		this.host = socket.getInetAddress().getHostAddress();
		init();
		this.isClient = false;
	}

	public PeerSocket(String host) throws UnknownHostException, IOException {
		this.host = host;
		this.socket = new Socket(host, Listener.PORT);
		init();
		this.os.write(new byte[] { Listener.CONNECTION_TYPE_TLS });
		this.isClient = true;
	}
	
	public boolean reconnect() {
		try {
			this.socket = new Socket(host, Listener.PORT);
			init();
			this.os.write(new byte[] { Listener.CONNECTION_TYPE_TLS });
		} catch (UnknownHostException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	private void init() throws IOException {
		this.socket.setSoTimeout(SOCKET_TIMEOUT);
		this.is = socket.getInputStream();
		this.os = socket.getOutputStream();
	}

	@Override
	public String getPeerId() {
		return socket.getInetAddress().getHostAddress();
	}

	@Override
	public boolean isConnected() {
		return socket.isConnected();
	}


	@Override
	public TLSRecord read(State state) {
		TLSRecord record;
		byte[] b = new byte[TLSEngine.RECORD_SIZE];
		byte[] tmp, input;
		int size = 0;
		try {
//			socket.setKeepAlive(true);
			size = is.read(b);
			
			if(size>0) {
				input = new byte[size];
				Tools.byteCopy(b, input);
				
				int contentSize = (int)(input[2] & 0xFF)*256 + (int)(input[3] & 0xFF);
				if(contentSize == (size-TLSEngine.HEADER_SIZE)) {
					try {
						record = new TLSRecord(state,input);
//						socket.setKeepAlive(false);
						return record;
					} catch (AlertException e) {
						Tools.printerr(e.getAlertDescription());
					}
				
				}
				else {
					Tools.printerr("SOMETHING IS WRONG!! (" + contentSize + "!=" + size + ")" + Tools.byteArrayToString(input));
				}
			}
//					// TODO: Error handling...
//					Tools.printerr("More than one: S:" + size + " CS:" + contentSize + " " + Tools.byteArrayToString(input));
//					int totalReceived = 0;
//					Tools.print(state.getEntityType() + " " +Tools.byteArrayToString(input));
//					while(totalReceived < (input.length-TLSEngine.HEADER_SIZE)) {
//						size = (int)(input[totalReceived+2] & 0xFF)*256 + (int)(input[totalReceived+3] & 0xFF);
//						size += TLSEngine.HEADER_SIZE;
//						//Tools.print("size: " + size + " totalreceived:" + totalReceived);
//						tmp = new byte[size];
//						Tools.byteCopy(input, tmp, totalReceived, totalReceived+size);
//						try {
//							records.add(new TLSRecord(state, tmp));
//						} catch (AlertException e) {
//							//Tools.printerr(e.getAlertDescription());
//						}
//						totalReceived += size;
//						//Tools.print("Reveived:" + size);
//					}
//				}
//			}
		} catch (IOException e) {
			e.printStackTrace();
		}
//		return records;
		return null;
	}

	@Override
	public synchronized void write(TLSRecord record) {
		try {
			os.write(record.getCiphertext());
			os.flush();
			Thread.sleep(80);
		} catch (IOException e) {
			Tools.printerr("" + e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			Tools.printerr("" + e.getMessage());
			e.printStackTrace();
		}

	}

	@Override
	public void close() {
		if(socket.isConnected())
			try {
				socket.close();
			} catch (IOException e) {
				Tools.printerr("" + e.getMessage());
				e.printStackTrace();
			}
	}

	@Override
	public boolean isClient() {
		return isClient;
	}

	public String toString() {
		return socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
	}

}
