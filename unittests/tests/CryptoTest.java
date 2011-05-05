package tests;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import junit.framework.TestCase;

import common.Tools;

public class CryptoTest extends TestCase {

	public void testSha1() throws NoSuchAlgorithmException, UnsupportedEncodingException {

		// TEST SHA-1
		crypto.mac.SHA1 sha1 = new crypto.mac.SHA1();
		assertEquals(sha1.getSize(), 160);
		byte[] result = sha1.getMac("".getBytes("UTF-8"));
		assertEquals(Tools.toHexString(result),"DA39A3EE5E6B4B0D3255BFEF95601890AFD80709");
		result = sha1.getMac("abc".getBytes("UTF-8"));
		assertEquals(Tools.toHexString(result),"A9993E364706816ABA3E25717850C26C9CD0D89D");
		result = sha1.getMac("abcdefghijklmnopqrstuvwxyz".getBytes("UTF-8"));
		assertEquals(Tools.toHexString(result),"32D10C7B8CF96570CA04CE37F2A19D84240D3A89");
		
	}
	
	public void testSha256() throws UnsupportedEncodingException {
		// TEST SHA-256
		byte[] result;
		crypto.mac.SHA256 sha256 = new crypto.mac.SHA256();
		assertEquals(sha256.getSize(), 256);
		result = sha256.getMac("".getBytes("UTF-8"));
		assertEquals(Tools.toHexString(result),"E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855");
		result = sha256.getMac("abc".getBytes("UTF-8"));
		assertEquals(Tools.toHexString(result),"BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD");
		result = sha256.getMac("abcdefghijklmnopqrstuvwxyz".getBytes("UTF-8"));
		assertEquals(Tools.toHexString(result),"71C480DF93D6AE2F1EFAD1447C66C9525E316218CF51FC8D9ED832F2DAF18B73");
	}

	public void testRijndael() throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		crypto.cipher.Rijndael aes = new crypto.cipher.Rijndael();
		byte[] key = new byte[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
		aes.init(true, key);
		byte[] textOriginal = "here is a littleaaaaaaaaaaaaaaaaaaaaaaaaaaaaajaskldjklsdjgaklgdfklgfjklghdfjklghdfjkghsdfjklghdfjkshqlkwejklqwjEKLØWJQKLEØJQWKLEJKLQWØEJKLAJKLJKLjklsdajkldjsakldjasklødjkaaaaaaaaa".getBytes();
		int rest = (16-(textOriginal.length%16));
		byte[] text = new byte[textOriginal.length+rest];
		System.arraycopy(textOriginal, 0, text, 0, textOriginal.length);
		byte[] plain = new byte[text.length];
		int blocks = (int)Math.ceil(text.length/16);
		byte[][] res = new byte[blocks][16];
		byte[] tmp = new byte[16];
		for(int i = 0; i < blocks; i++) {
			System.arraycopy(text, i*16, tmp, 0, 16);
			aes.processBlock(tmp, 0, res[i], 0);
		}
		aes.init(false, key);
		for(int i = 0; i < blocks; i++) {
			aes.processBlock(res[i], 0, plain, i*16);
		}

		assertEquals(new String(plain), new String(text));
	}
	
	public void testRijndael2() throws InvalidKeyException {
		crypto.cipher.Rijndael2 aes = new crypto.cipher.Rijndael2();
		byte[] key = new byte[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
		Object aesKey = aes.makeKey(key, 16);
		byte[] textOriginal = "here is a littleaaaaaaaaaaaaaaaaaaaaaaaaaaaaajaskldjklsdjgaklgdfklgfjklghdfjklghdfjkghsdfjklghdfjkshqlkwejklqwjEKLØWJQKLEØJQWKLEJKLQWØEJKLAJKLJKLjklsdajkldjsakldjasklødjkaaaaaaaaa".getBytes();
		int rest = (16-(textOriginal.length%16));
		byte[] text = new byte[textOriginal.length+rest];
		System.arraycopy(textOriginal, 0, text, 0, textOriginal.length);
		byte[] plain = new byte[text.length];
		int blocks = (int)Math.ceil(text.length/16);
		byte[][] res = new byte[blocks][16];
		byte[] tmp = new byte[16];
		for(int i = 0; i < blocks; i++) {
			System.arraycopy(text, i*16, tmp, 0, 16);
			aes.encrypt(tmp, 0, res[i], 0, aesKey, 16);
		}
		for(int i = 0; i < blocks; i++) {
			aes.decrypt(res[i], 0, plain, i*16, aesKey, 16);
		}

		assertEquals(new String(plain), new String(text));
	}
	
	public void testDH() throws Exception {
		crypto.keyexchange.DH dhA = new crypto.keyexchange.DH(512);
		crypto.keyexchange.DH dhB = new crypto.keyexchange.DH(512);
		
		assertEquals(dhA.requireServerKeyExchange(),true);
		assertEquals(dhB.requireServerKeyExchange(),true);
		
		dhA.setYb(dhB.getPublicKey());
		dhB.setYb(dhA.getPublicKey());
		
		BigInteger secretA = dhA.getSecretKey();
		BigInteger secretB = dhB.getSecretKey();
		
		assertEquals(secretA.toString(), secretB.toString());
		
	}
}
