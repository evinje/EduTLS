package tls;

import java.util.ArrayList;

import tls.handshake.IHandshakeMessage;
import tls.record.TLSCiphertext;
import tls.record.TLSCompressed;
import tls.record.TLSPlaintext;

import common.Log;
import common.LogEvent;
import common.Tools;


/**
 * Every message sent to and received from
 * TLSEngine must be a TLSRecord. The content 
 * type of a TLSRecord may be HANDSHAKE,
 * ALERT, APPLICATION or CHANGE CIPHER SPEC.
 *
 * @author 	Eivind Vinje
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

	/**
	 * TLSRecord constructor for an Alert message
	 * @param state State, the current connection state
	 * @param alert	TLSAlert, the alert object
	 * @returns	Nothing, it is a constructor
	 */
	public TLSRecord(State state, TLSAlert alert) {
		this.state = state;
		init();
		contentType = TLSEngine.ALERT;
		plaintext = alert.getContent();
	}

	/**
	 * TLSRecord constructor for a handshake message
	 * @param state State, the current connection state
	 * @param handshake	IHandshakeMessage, the handshake object
	 * @returns	Nothing, it is a constructor
	 */
	public TLSRecord(State state, IHandshakeMessage handshake) {
		this.state = state;
		init();
		int tmpMessageSize = handshake.getByte().length;
		contentType = TLSEngine.HANDSHAKE;
		byte[] handshakeheader = new byte[TLSEngine.HEADER_SIZE];
		handshakeheader[0] = handshake.getType();
		handshakeheader[1] = 0;
		if(tmpMessageSize<256) {
			handshakeheader[2] = 0;
			handshakeheader[3] = (byte)(tmpMessageSize & 0xFF);
		}
		else {
			handshakeheader[2] = (byte)((int)Math.ceil(tmpMessageSize/256) & 0xFF);
			handshakeheader[3] = (byte)((tmpMessageSize%256) & 0xFF);
		}
		plaintext = Tools.byteAppend(handshakeheader, handshake.getByte());
	}

	/**
	 * TLSRecord constructor for an application message
	 * @param state State, the current connection state
	 * @param input	byte[], the byte array value of the record
	 * @returns	Nothing, it is a constructor
	 */
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
		if(contentType==TLSEngine.APPLICATION) {
			int macSize = (state.getMacAlgorithm().getSize()/8);
			byte[] mac = new byte[macSize];
			Tools.byteCopy(input, mac, input.length-mac.length);
			// TODO: Encrypt mac with mac key
			ciphertext = new byte[input.length-TLSEngine.HEADER_SIZE-macSize];
			System.arraycopy(input, TLSEngine.HEADER_SIZE, ciphertext, 0, ciphertext.length);
			byte[] expectedMac = state.getMacAlgorithm().getHash(ciphertext);
			if(!Tools.compareByteArray(mac, expectedMac))
				throw new AlertException(AlertException.alert_fatal,AlertException.insufficient_security, "Mac decryption error");
			defragment();
			decrypt();			
		}
		else {
			plaintext = new byte[input.length-TLSEngine.HEADER_SIZE];
			Tools.byteCopy(input, plaintext, TLSEngine.HEADER_SIZE);
		}
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
		if(contentType != TLSEngine.APPLICATION) {
			return plaintext;
		}
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
		if(contentType==TLSEngine.APPLICATION) {
			byte[] c = new byte[getNumberOfChunks()*TLSEngine.RECORD_SIZE];
			int totalSize = 0;
			for(int i=0; i<getNumberOfChunks(); i++) {
				Tools.byteCopy(tlsciphertext.get(i).getCipher(), c, TLSEngine.RECORD_SIZE*i);
				totalSize += tlsciphertext.get(i).getCipher().length;
			}
			byte[] cipherNoHeader = new byte[totalSize];
			Tools.byteCopy(c, cipherNoHeader);
			// size is reported in bits, not bytes
			byte[] mac = new byte[state.getMacAlgorithm().getSize()/8];
			mac = state.getMacAlgorithm().getHash(cipherNoHeader);
			// TODO: Encrypt mac with mac key
			byte[] cipherWithMac = new byte[cipherNoHeader.length+mac.length];
			cipherWithMac = Tools.byteAppend(cipherNoHeader, mac);
			byte[] header = getHeader(cipherWithMac);
			ciphertext = Tools.byteAppend(header, cipherWithMac);
			return ciphertext;
		}
		byte[] header = getHeader(plaintext);
		byte[] response = Tools.byteAppend(header, plaintext);
		return response;
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
