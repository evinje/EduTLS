package crypto;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import common.Tools;

public class PRF {
	private static int md5len = 32;
	private static int sha1len = 40;
	
	public static void generate(byte[] secret, String label, byte[] seed, byte[] out) {
		// The PRF splits the secret into two halves, S1 (left) and S2 (right)
		byte[] s1 = new byte[secret.length/2];
		byte[] s2 = new byte[secret.length/2];
		Tools.byteCopy(secret, s1, 0);
		Tools.byteCopy(secret, s2, s1.length);
		// The number of rounds that MD5 and SHA1 must perform to 
		// obtain the correct length of the return value
		int md5rounds = (int)Math.ceil((double)out.length/md5len);
		int sha1rounds = (int)Math.ceil((double)out.length/sha1len);
		// strMD5 is a temporary help variable because
		// the HMAC function depends on the previous result
		byte[] md5last = new byte[md5len]; 
		byte[] md5sum = new byte[md5len*md5rounds];
		
		for(int i=0; i < md5rounds; i++) {
			if(i==0)
				md5last = HMAC_hash(s1, Tools.byteAppend(label.getBytes(), seed),"HmacMD5");
			else
				md5last = HMAC_hash(s1, Tools.byteAppend(md5last, seed) ,"HmacMD5");
			Tools.byteCopy(md5last, md5sum, i*md5len);
		}
		
		byte[] sha1last = new byte[sha1len]; 
		byte[] sha1sum = new byte[sha1len*sha1rounds];
		
		for(int i=0; i < sha1rounds; i++) {
			if(i==0)
				sha1last = HMAC_hash(s1, Tools.byteAppend(label.getBytes(), seed),"HmacSHA1");
			else
				sha1last = HMAC_hash(s1, Tools.byteAppend(sha1last, seed) ,"HmacSHA1");
			Tools.byteCopy(sha1last, sha1sum, i*sha1len);
		}
		// XOR the results from the MD5 HMAC and the SHA-1 HMAC
		XOR(md5sum, sha1sum, out);
	}
	
	public static byte[] HMAC_hash(byte[] secret, byte[] seed, String algorithm) {
		// this function uses the Java built-in HMAC functionality
		Key key = new SecretKeySpec(secret, 0, secret.length, algorithm);
		try {
			Mac mac = Mac.getInstance(algorithm);
			mac.init(key);
			return mac.doFinal(seed);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void XOR(byte[] one, byte[] two, byte[] out) {
		// loop through each byte and perform an bitwise XOR
		for(int i=0; i < out.length; i++) {
			out[i] = (byte)(one[i]^two[i]);
		}
		// if either of the two strings are longer
		// than length, the excess part will be discarded
	}
}
