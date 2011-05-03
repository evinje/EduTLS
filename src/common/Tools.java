package common;

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
	// -----------------------------------------------------------------
	// Creates a hex string.
	// -----------------------------------------------------------------
	public static String toHexString(byte[] array) {       
		StringBuffer buffer = new StringBuffer (array.length*2);
		for (int i=0;i<array.length;i++)
			buffer.append (s_hexUpper[array[i] & 0xFF]);
		return  buffer.toString();
	}
	
	/**
	    * <p>Returns a byte array from a string of hexadecimal digits.</p>
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
	    * <p>Returns a number from <code>0</code> to <code>15</code> corresponding
	    * to the designated hexadecimal digit.</p>
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
	
	public static byte[] byteAppend(byte[] one, byte[] two) {
		byte[] tmp = new byte[one.length+two.length];
		System.arraycopy(one, 0, tmp, 0, one.length);
		System.arraycopy(two, 0, tmp, one.length, two.length);
		return tmp;
	}
	public static byte[] byteAppend(byte one, byte two) {
		byte[] tmp = new byte[2];
		tmp[0] = one;
		tmp[1] = two;
		return tmp;
	}
	
	public static void byteCopy(byte[] src, byte[] dest) {
		byteCopy(src,dest,0, 0);
	}
	
	public static void byteCopy(byte[] src, byte[] dest, int from) {
		byteCopy(src,dest,from, 0);
	}
	
	public static void byteCopy(byte[] src, byte[] dest, int from, int length) {
		if(length > 0 && src.length>=length && dest.length>=length)
			System.arraycopy(src, from, dest, 0, length);
		else if(src.length > dest.length)
			System.arraycopy(src, from, dest, 0, dest.length);
		else
			System.arraycopy(src, 0, dest, from, src.length);
	}
	
	
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
	

	public static boolean compareByteArray(byte[] one, byte[] two) {
		if(one.length != two.length)
			return false;
		for(int i = 0; i < one.length; i++) {
			if(one[i] != two[i])
				return false;
		}
		
		return true;
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
	public static void printerr(String msg) {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		System.err.println(ste.getClassName() + " " + ste.getMethodName() + " " + ste.getLineNumber() + ": " + msg);
	}
}