package tests;

import java.util.ArrayList;

import tls.State;
import tls.TLSRecord;

public class PeerTestImpl implements server.IPeerCommunicator {
	//byte[] content;
	private boolean isClient;
	ArrayList<TLSRecord> contentQueue;
	
    public PeerTestImpl() {
    	contentQueue = new ArrayList<TLSRecord>();
    	isClient = true;
    }

    public void setPeerClient(boolean isClient) {
    	this.isClient = isClient;
    }
	@Override
	public String getPeerId() {
		return "127.0.0.1";
	}

	public int queueSize() {
		return contentQueue.size();
	}

//	@Override
//	public int available() {
//		if(contentQueue.size() == 0)
//			return 0;
//		return contentQueue.get(0).length;
//	}



	@Override
	public TLSRecord read(State state) {
		if(contentQueue.size()==0)
			return null;
		TLSRecord record;
		record = contentQueue.get(0);
		contentQueue.remove(0);
		return record;
	}


	@Override
	public void write(TLSRecord record) {
		contentQueue.add(record);
	}

	@Override
	public boolean isConnected() {
		return true;
	}

	@Override
	public void close() {
	}

	@Override
	public boolean isClient() {
		return isClient;
	}

	@Override
	public boolean reconnect() {
		return true;
	}
   
}
