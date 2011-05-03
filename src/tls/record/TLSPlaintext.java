package tls.record;

import tls.State;
import tls.TLSEngine;

import common.Tools;

public class TLSPlaintext {
	private byte[] plaintext;
	private byte contentType;
	private State state;
	
	public TLSPlaintext(State state, byte[] input, byte contentType) {
		this.state = state;
		int size = input.length;
		plaintext = new byte[size];
		this.contentType = contentType;
		Tools.byteCopy(input, plaintext);
//		plaintext = new byte[size+TLSEngine.HEADER_SIZE];
//		plaintext[0] = contentType;
//		plaintext[1] = TLSEngine.VERSION_TLSv1;
//		if(size<256) {
//			plaintext[2] = 0;
//			plaintext[3] = (byte)(size & 0xFF);
//		}
//		else {
//			plaintext[2] = (byte)((int)Math.ceil(size/256) & 0xFF);
//			plaintext[3] = (byte)((size%256) & 0xFF);
//		}
//		System.arraycopy(input, 0, plaintext, TLSEngine.HEADER_SIZE, size);
	}
	
	public TLSPlaintext(State state, TLSCompressed compressed) {
		this.state = state;
		this.contentType = compressed.getContentType();
		plaintext = new byte[compressed.getUncompressed().length];
		Tools.byteCopy(compressed.getUncompressed(), plaintext);

	}
	
	public byte[] getPlaintext() {
		return plaintext;
	}
	

	public byte getContentType() {
		return contentType;
	}
}
