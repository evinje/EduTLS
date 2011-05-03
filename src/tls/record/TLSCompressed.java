package tls.record;

import tls.State;
import tls.TLSEngine;

import common.Tools;

import crypto.ICompression;

public class TLSCompressed {
	private byte[] uncompressed;
	private byte[] compressed;
	private byte contentType;
	private State state;
	
	public TLSCompressed(State state, TLSPlaintext plaintext) {
		this.state = state;
		uncompressed = new byte[plaintext.getPlaintext().length];
		Tools.byteCopy(plaintext.getPlaintext(), uncompressed);
		contentType = plaintext.getContentType();
		compress();
//		Tools.print("Uncompressed: " + Tools.byteArrayToString(uncompressed));
//		Tools.print("Compressed: " + Tools.byteArrayToString(compressed));
	}
	
	public TLSCompressed(State state, TLSCiphertext ciphertext) {
		this.state = state;
		compressed = new byte[ciphertext.getPlain().length];
		Tools.byteCopy(ciphertext.getPlain(), compressed);
		contentType = ciphertext.getContentType();
		decompress();
	}
	
	private void compress() {
		compressed = state.getCompressionMethod().compress(uncompressed);
	}
	
	private void decompress() {
		uncompressed = state.getCompressionMethod().decompress(compressed);
	}
	
	public byte[] getCompressed() {
		return compressed;
	}
	
	public byte[] getUncompressed() {
		return uncompressed;
	}
	
	public byte getContentType() {
		return contentType;
	}
}
