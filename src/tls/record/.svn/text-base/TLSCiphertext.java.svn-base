package tls.record;

import tls.State;
import tls.TLSEngine;
import common.Tools;

public class TLSCiphertext {
	private byte[] plaintext;
	private byte[] ciphertext;
	private int size;
	private byte contentType;
	private byte version;
	private State state;
	
	public TLSCiphertext(State state, TLSCompressed compressed) {
		this.state = state;
		size = compressed.getCompressed().length;
		contentType = compressed.getContentType();
		plaintext = new byte[size];
		Tools.byteCopy(compressed.getCompressed(), plaintext);
		encrypt();
//		Tools.print("Plain: " + Tools.byteArrayToString(plaintext));
//		Tools.print("Cipher: " + Tools.byteArrayToString(ciphertext));
	}
	
	public TLSCiphertext(State state, byte[] input) {
//		Tools.print("Ciphertext input: " + Tools.byteArrayToString(input));
		this.state = state;
		this.contentType = input[0];
		size = (int)(input[2] & 0xFF)*256 + (int)(input[3] & 0xFF);
		ciphertext = new byte[input.length - TLSEngine.HEADER_SIZE];
		System.arraycopy(input, TLSEngine.HEADER_SIZE, ciphertext, 0, ciphertext.length);
		decrypt();
	}
	
	private void encrypt() {
		if(contentType != TLSEngine.APPLICATION) {
			ciphertext = new byte[plaintext.length];
			Tools.byteCopy(plaintext, ciphertext);
			return;
		}
		
		state.getCipherAlgorithm().init(true, state.getEncryptionKeyWrite());
		int block_size = state.getCipherAlgorithm().getBlockSize();
		int numOfBlocks;
		if(block_size==0)
			numOfBlocks=1;
		else
			numOfBlocks = (int)Math.ceil((float)(plaintext.length)/(block_size));
		
		ciphertext = new byte[numOfBlocks*block_size];
		for(int i = 0; i < numOfBlocks; i++) {
			if(((i+1)*block_size) > size) {
				byte[] t = new byte[block_size];
				System.arraycopy(plaintext, i*block_size, t, 0, plaintext.length-(i*block_size));
				state.getCipherAlgorithm().cipher(t, 0, ciphertext, i*block_size);
			}
			else
				state.getCipherAlgorithm().cipher(plaintext, i*block_size, ciphertext, i*block_size);			
		}
	}
	
	private void decrypt() {
		if(contentType != TLSEngine.APPLICATION) {
			plaintext = new byte[ciphertext.length];
			Tools.byteCopy(ciphertext, plaintext);
			return;
		}
		byte[] tmp = new byte[ciphertext.length];
		plaintext = new byte[size];
		state.getCipherAlgorithm().init(false, state.getEncryptionKeyRead());
		int block_size = state.getCipherAlgorithm().getBlockSize();
		int numOfBlocks;
		if(block_size==0)
			numOfBlocks = 1;
		else
			numOfBlocks = (int)Math.ceil((float)(ciphertext.length)/(block_size));
		
		for(int i = 0; i < numOfBlocks; i++) {
			if(((i+1)*block_size) > size) {
				byte[] t = new byte[block_size];
				System.arraycopy(ciphertext, i*block_size, t, 0, ciphertext.length-(i*block_size));
				state.getCipherAlgorithm().cipher(t, 0, tmp, i*block_size);
			}
			else
				state.getCipherAlgorithm().cipher(ciphertext, i*block_size, tmp, i*block_size);
		}
		
		Tools.byteCopy(tmp, plaintext);
	}
	
	public byte[] getCipher() {
		byte[] tmp = new byte[ciphertext.length + TLSEngine.HEADER_SIZE];
		tmp[0] = contentType;
		tmp[1] = TLSEngine.VERSION_TLSv1;
		if(size<256) {
			tmp[2] = 0;
			tmp[3] = (byte)(size & 0xFF);
		}
		else {
			tmp[2] = (byte)((int)Math.ceil(size/256) & 0xFF);
			tmp[3] = (byte)((size%256) & 0xFF);
		}
		System.arraycopy(ciphertext, 0, tmp, TLSEngine.HEADER_SIZE, ciphertext.length);
		return tmp;
	}
	
	public byte[] getPlain() {
		return plaintext;
	}
	
	public int getSize() {
		return size;
	}
	public byte getVersion() {
		return version;
	}
	public byte getContentType() {
		return contentType;
	}

}
