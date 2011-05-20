package crypto.compression;

import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import common.Tools;

import tls.TLSEngine;

import crypto.ICompression;

public class ZLib implements ICompression {
	Deflater compressor ;
	Inflater decompressor;
	boolean enabled;
	
	public ZLib() {
		compressor = new Deflater();
		decompressor = new Inflater();
		enabled = true;
	}
	
	@Override
	public byte[] compress(byte[] input) {
		compressor = new Deflater(Deflater.DEFAULT_COMPRESSION);
		byte[] tmp = new byte[input.length+50];
		compressor.setInput(input, 0, input.length);
		compressor.finish();
	    int compressedDataLength = compressor.deflate(tmp);

	    byte[] output = new byte[compressedDataLength];
	    Tools.byteCopy(tmp, output);
		return output;
	}

	@Override
	public byte[] decompress(byte[] input) {
		decompressor = new Inflater();
		byte[] tmp = new byte[TLSEngine.RECORD_SIZE];
	    
	    decompressor.setInput(input, 0, input.length);
	    try {
			int resultLength = decompressor.inflate(tmp);
			byte[] output = new byte[resultLength];
			decompressor.end();
			Tools.byteCopy(tmp, output);
			return output;
		} catch (DataFormatException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public byte getCompressionId() {
		return 1;
	}

	@Override
	public String getName() {
		return "ZLib";
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
