package tls;

import java.util.ArrayList;

import tls.handshake.IHandshakeMessage;
import tls.record.TLSCiphertext;
import tls.record.TLSCompressed;
import tls.record.TLSPlaintext;

import common.Log;
import common.LogEvent;
import common.Tools;

/*
 *  struct {
          ContentType type;
          ProtocolVersion version; (Left out)
          uint16 length;
          opaque fragment[TLSPlaintext.length];
      } TLSPlaintext;
 */
public class TLSRecord {
	private State state;
	private ArrayList<TLSPlaintext> tlsplaintext;
	private ArrayList<TLSCompressed> tlscompressed;
	private ArrayList<TLSCiphertext> tlsciphertext;
	private byte contentType;
	private int contentSize;
	private byte[] plaintext;
	private byte[] ciphertext;

	public TLSRecord(State state, TLSAlert alert) {
		this.state = state;
		init();
		contentType = TLSEngine.ALERT;
		plaintext = alert.getContent();
		fragment();
		encrypt();
	}

	public TLSRecord(State state, IHandshakeMessage handshake) {
		this.state = state;
		init();
		//Tools.print(handshake.toString());
		int tmpMessageSize = handshake.getByte().length;
		contentType = TLSEngine.HANDSHAKE;
		byte[] handshakeheader = new byte[TLSEngine.HEADER_SIZE];
		handshakeheader[0] = handshake.getType();
		handshakeheader[1] = (byte)Math.ceil(tmpMessageSize/(256*256));
		handshakeheader[2] = (byte)Math.ceil(tmpMessageSize/256);
		handshakeheader[3] = (byte)(tmpMessageSize%256);
		plaintext = Tools.byteAppend(handshakeheader, handshake.getByte());
		fragment();
		encrypt();
	}

	public TLSRecord(State state, byte[] input) throws AlertException {
		Tools.print("New TLSRecord: " + state.getEntityType() + " " +Tools.byteArrayToString(input));
		this.state = state;
		int versionNumber;
		if(input.length < TLSEngine.HEADER_SIZE)
			throw new AlertException(AlertException.alert_fatal,AlertException.unexpected_message, "Too short message");
		contentType = input[0];
		versionNumber = (int)input[1];

		if(versionNumber != TLSEngine.VERSION_TLSv1) {
			throw new AlertException(AlertException.alert_fatal,AlertException.protocol_version, "Unsupported version number: " + versionNumber);
		}

		// Since java byte is signed (from -127 to +127) 
		// we need a conversion to have 0 to 255 
		contentSize = (int)(input[2] & 0xFF)*256 + (int)(input[3] & 0xFF);

		if(contentSize<0 || ((TLSEngine.HEADER_SIZE + contentSize) > TLSEngine.RECORD_SIZE)) 
			throw new AlertException(AlertException.alert_fatal,AlertException.record_overflow, "Wrong content size: " + contentSize);

		if(contentSize==0 && input[0] != TLSEngine.APPLICATION)
			throw new AlertException(AlertException.alert_fatal,AlertException.illegal_parameter, "Content size cannot be empty unless application");

		boolean validContentType = false;
		if(contentType == TLSEngine.ALERT)
			validContentType=true;
		else if(contentType == TLSEngine.APPLICATION)
			validContentType=true;
		else if(contentType == TLSEngine.HANDSHAKE)
			validContentType=true;
		if(!validContentType)
			throw new AlertException(AlertException.alert_fatal,AlertException.illegal_parameter, "Not valid content type");

		if(contentSize > (input.length - TLSEngine.HEADER_SIZE)) {
			throw new AlertException(AlertException.alert_fatal,AlertException.illegal_parameter, "Wrong reported content size (" + contentSize + "/" + input.length + ")");
		}
		init();
		ciphertext = new byte[input.length-TLSEngine.HEADER_SIZE];
		Tools.byteCopy(input, ciphertext, TLSEngine.HEADER_SIZE);
		defragment();
		decrypt();
	}

	public TLSRecord(State state, byte[] plain, byte contentType) {
		this.state = state;
		this.contentType = contentType;
		plaintext = new byte[plain.length];
		Tools.byteCopy(plain, plaintext);
		LogEvent le = new LogEvent("New TLSRecord created","Plaintext: " + Tools.byteArrayToString(plaintext));
		if(contentType==TLSEngine.APPLICATION)
			Log.get().add(le);
		init();
		fragment();
		encrypt();
		le.addDetails("Ciphertext: " + Tools.byteArrayToString(getCiphertext()));
	}

	private void init() {
		tlsplaintext = new ArrayList<TLSPlaintext>();
		tlscompressed = new ArrayList<TLSCompressed>();
		tlsciphertext = new ArrayList<TLSCiphertext>();
	}

	public byte getContentType() {
		return contentType;
	}
	
	public String getContentTypeName() {
		if(contentType == TLSEngine.ALERT)
			return "Alert";
		else if(contentType == TLSEngine.APPLICATION)
			return "Application";
		else if(contentType == TLSEngine.HANDSHAKE)
			return "Handshake";
		return "Invalid";
	}

	private void encrypt() {
//		Tools.print("Start encrypt: " + tlsplaintext.size() + " " + Tools.byteArrayToString(tlsplaintext.get(0).getPlaintext()));
		for(int i = 0; i < tlsplaintext.size(); i++) {
			tlscompressed.add(new TLSCompressed(state, tlsplaintext.get(i)));
			tlsciphertext.add(new TLSCiphertext(state, tlscompressed.get(i)));
			//			Tools.print("Plain " + i + " = " + Tools.byteArrayToString(tlsplaintext.get(i).getPlaintext()));
			//			Tools.print("Compressed " + i + " = " + Tools.byteArrayToString(tlscompressed.get(i).getCompressed()));
			//			Tools.print("Cipher " + i + " = " + Tools.byteArrayToString(tlsciphertext.get(i).getPlain()));
		}
	}

	private void decrypt() {
		for(int i = 0; i < tlsciphertext.size(); i++) {
			tlscompressed.add(new TLSCompressed(state, tlsciphertext.get(i)));
			tlsplaintext.add(new TLSPlaintext(state, tlscompressed.get(i)));
			//			Tools.print("Plain " + i + " = " + Tools.byteArrayToString(tlsplaintext.get(i).getPlaintext()));
			//			Tools.print("Compressed " + i + " = " + Tools.byteArrayToString(tlscompressed.get(i).getCompressed()));
			//			Tools.print("Cipher " + i + " = " + Tools.byteArrayToString(tlsciphertext.get(i).getPlain()));
		}
	}

	public byte[] getPlaintext() {
//		if(plaintext != null && plaintext.length>0)
//			return plaintext;

		byte[] tmp = new byte[tlsplaintext.size()*TLSEngine.RECORD_SIZE];
		int totalSize = 0;
		for(int i = 0; i < tlsplaintext.size(); i++) {
			Tools.byteCopy(tlsplaintext.get(i).getPlaintext(), tmp, i*TLSEngine.RECORD_SIZE);
			totalSize += tlsplaintext.get(i).getPlaintext().length;
		}
		plaintext = new byte[totalSize];
		Tools.byteCopy(tmp, plaintext, 0, plaintext.length);
		return plaintext;

	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < tlsplaintext.size(); i++) {
			sb.append(tlsplaintext.get(i).toString());
		}
		return sb.toString();
	}

	private byte[] getHeader(byte[] input) {
		byte[] header = new byte[TLSEngine.HEADER_SIZE];
		header[0] = contentType;
		header[1] = TLSEngine.VERSION_TLSv1;
		if(input.length<256) {
			header[2] = 0;
			header[3] = (byte)(input.length & 0xFF);
		}
		else {
			header[2] = (byte)((int)Math.ceil(input.length/256) & 0xFF);
			header[3] = (byte)((input.length%256) & 0xFF);
		}
		return header;
	}
	
	public byte[] getCiphertext() {
//		if(ciphertext != null)
//			return ciphertext;
//		if(contentType == TLSEngine.HANDSHAKE)
//			return Tools.byteAppend(getHeader(plaintext), plaintext);
		
		byte[] c = new byte[getNumberOfChunks()*TLSEngine.RECORD_SIZE];
		
		int totalSize = 0;
		for(int i=0; i<getNumberOfChunks(); i++) {
			Tools.byteCopy(tlsciphertext.get(i).getCipher(), c, TLSEngine.RECORD_SIZE*i);
			totalSize += tlsciphertext.get(i).getCipher().length;
		}
		byte[] tmp = new byte[totalSize];
		Tools.byteCopy(c, tmp);
		byte[] header = getHeader(tmp);
		ciphertext = Tools.byteAppend(header, tmp);
		return ciphertext;
	}

	public int getNumberOfChunks() {
		return tlsciphertext.size();
	}

	public void fragment() {
		int numOfChunks = (int)Math.ceil((float)(plaintext.length)/(TLSEngine.FRAGMENT_SIZE));
		byte[] tmpChunk;
		int size;

		for(int i = 0; i < numOfChunks; i++) {
			tmpChunk = new byte[TLSEngine.FRAGMENT_SIZE];
			size = TLSEngine.FRAGMENT_SIZE*i;
			if((size+tmpChunk.length) > plaintext.length)
				tmpChunk = new byte[plaintext.length-size];
			Tools.byteCopy(plaintext, tmpChunk, size);
			tlsplaintext.add(new TLSPlaintext(state, tmpChunk, contentType));
			//Tools.print("Chunk fragment " + i + ": " + Tools.byteArrayToString(tmpChunk));
		}
	}

	public void defragment() throws AlertException {
		if(ciphertext.length < TLSEngine.HEADER_SIZE)
			throw new AlertException(AlertException.alert_warning,AlertException.unexpected_message, "Message too short");
		int size;
		int numOfChunks = (int)Math.ceil((float)(ciphertext.length)/TLSEngine.RECORD_SIZE);
		byte[] tmpChunk;
		
		for(int i = 0; i < numOfChunks; i++) {
			tmpChunk = new byte[TLSEngine.FRAGMENT_SIZE];
			size = TLSEngine.FRAGMENT_SIZE*i;
			if((size+tmpChunk.length) > ciphertext.length)
				tmpChunk = new byte[ciphertext.length-size];
			Tools.byteCopy(ciphertext, tmpChunk, size);
			tlsciphertext.add(new TLSCiphertext(state, tmpChunk));
		}
	}

}
