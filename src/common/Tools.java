package common;

import java.io.File;

import crypto.ICipher;

/**
 * The Tools class provides several
 * string and byte manipulation methods
 *
 * @author 	Eivind Vinje
 */

public class Tools {
	private static String[] s_hexUpper;
	// -----------------------------------------------------------------
	// STATIC CONSTRUCTOR
	// -----------------------------------------------------------------
	static {       
		s_hexUpper = new String[256];
		for (int i = 0; i < 256; i++) {       
			String sHexUpper = Integer.toHexString(i & 0xFF).toUpperCase();
			if (sHexUpper.length() == 1)
				s_hexUpper[i] = "0" + sHexUpper;
			else
				s_hexUpper[i] = sHexUpper;
		}
	}
	/**
	 * Creates a hex string of a byte[] array
	 * 
	 * @param array	byte[]
	 * @returns	String Converts the byte[] array to String 
	 */
	public static String toHexString(byte[] array) {       
		StringBuffer buffer = new StringBuffer (array.length*2);
		for (int i=0;i<array.length;i++)
			buffer.append (s_hexUpper[array[i] & 0xFF]);
		return  buffer.toString();
	}
	
	/**
	    * Returns a byte array from a string of hexadecimal digits.
	    *
	    * @param s a string of hexadecimal ASCII characters
	    * @return the decoded byte array from the input hexadecimal string.
	    */
	   public static byte[] toBytesFromString(String s) {
	      int limit = s.length();
	      byte[] result = new byte[((limit + 1) / 2)];
	      int i = 0, j = 0;
	      if ((limit % 2) == 1) {
	         result[j++] = (byte) fromDigit(s.charAt(i++));
	      }
	      while (i < limit) {
	         result[j  ]  = (byte) (fromDigit(s.charAt(i++)) << 4);
	         result[j++] |= (byte)  fromDigit(s.charAt(i++));
	      }
	      return result;
	   }
	   
	   /**
	    * >Returns a number from 0 to 15 corresponding
	    * to the designated hexadecimal digit.
	    *
	    * @param c a hexadecimal ASCII symbol.
	    */
	   public static int fromDigit(char c) {
	      if (c >= '0' && c <= '9') {
	         return c - '0';
	      } else if (c >= 'A' && c <= 'F') {
	         return c - 'A' + 10;
	      } else if (c >= 'a' && c <= 'f') {
	         return c - 'a' + 10;
	      } else
	         throw new IllegalArgumentException("Invalid hexadecimal digit: " + c);
	   }
	
	   /**
	    * Concatenate two byte arrays
	    * @param one	The first byte[] array
	    * @param two 	The second byte[] array
	    * @returns	byte[] A concatenation of one and two
	    */
	public static byte[] byteAppend(byte[] one, byte[] two) {
		byte[] tmp = new byte[one.length+two.length];
		System.arraycopy(one, 0, tmp, 0, one.length);
		System.arraycopy(two, 0, tmp, one.length, two.length);
		return tmp;
	}

	/*
	 * @see byteCopy(byte[] src, byte[] dest, int from, int length)
	 */
	public static void byteCopy(byte[] src, byte[] dest) {
		byteCopy(src,dest,0, 0);
	}
	
	/*
	 * @see byteCopy(byte[] src, byte[] dest, int from, int length)
	 */
	public static void byteCopy(byte[] src, byte[] dest, int from) {
		byteCopy(src,dest,from, 0);
	}
	
	/**
	 * Copy a
	 * @param src byte[], The source
	 * @param dest byte[], The destination
	 * @param from int, The position to copy from
	 * @param length int, How many bytes to copy 
	 * @returns	Nothing, it is a constructor
	 */
	public static void byteCopy(byte[] src, byte[] dest, int from, int length) {
		if(length > 0 && src.length>=length && dest.length>=length)
			System.arraycopy(src, from, dest, 0, length);
		else if(src.length > dest.length)
			System.arraycopy(src, from, dest, 0, dest.length);
		else
			System.arraycopy(src, 0, dest, from, src.length);
	}
	
	/**
	 * Converts an byte array of arbitrary length
	 * into a string, used for printing purposes
	 * 
	 * @param in byte[] The byte array to convert
	 * @returns	String 
	 */
	public static String byteArrayToString(byte[] in) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < in.length; i++) {
			if(i==(in.length-1))
				sb.append((in[i] & 0xFF));
			else
				sb.append((in[i] & 0xFF) + ":");
		}
		return sb.toString();
	}
	
	/**
	 * Compares each byte in the two arrays
	 * and examine if they are equal. If the 
	 * length is different on the arrays,
	 * it always return false
	 * 
	 * @param one byte[] The first byte array
	 * @param two byte[] The second byte array
	 * @returns	boolean 
	 */
	public static boolean compareByteArray(byte[] one, byte[] two) {
		if(one.length != two.length)
			return false;
		for(int i = 0; i < one.length; i++) {
			if(one[i] != two[i])
				return false;
		}
		
		return true;
	}
	
	public static boolean isEmptyByteArray(byte[] array) {
		if(array==null)
			return true;
		byte[] empty = new byte[array.length];
		return compareByteArray(array, empty);
	}
	
	public static void longToByteArray(long value, byte[] buf, int ofs) {
			int tmp = (int) (value >>> 32); 
			buf[ofs] = (byte) (tmp >>> 24); 
			buf[ofs + 1] = (byte) ((tmp >>> 16) & 0x0ff); 
			buf[ofs + 2] = (byte) ((tmp >>> 8) & 0x0ff); 
			buf[ofs + 3] = (byte) tmp; 
			tmp = (int) value; 
			buf[ofs + 4] = (byte) (tmp >>> 24); 
			buf[ofs + 5] = (byte) ((tmp >>> 16) & 0x0ff); 
			buf[ofs + 6] = (byte) ((tmp >>> 8) & 0x0ff); 
			buf[ofs + 7] = (byte) tmp; 
	}
	
	public static long byteArrayToLong(byte[] value, int ofs) {
		/*
		 * Could also used:
		 * ByteBuffer bb = ByteBuffer.wrap(value);
		 * long l = bb.getLong();
		 */
		long l = 0;
		for (int i = ofs; i < ofs+8; i++)
		{
		   l = (l << 8) + (value[i] & 0xff);
		}
		return l;
	}
	
	public static void print(String msg) {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		System.out.println(ste.getClassName() + " " + ste.getMethodName() + " " + ste.getLineNumber() + ": " + msg);
	}
	
	public static void print(byte[] msg) {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		System.out.println(ste.getClassName() + " " + ste.getMethodName() + " " + ste.getLineNumber() + ": " + Tools.byteArrayToString(msg));
	}
	
	public static void printerr(String msg) {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		System.err.println(ste.getClassName() + " " + ste.getMethodName() + " " + ste.getLineNumber() + ": " + msg);
	}
	
}
