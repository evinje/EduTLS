package tests;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import tls.TLSEngine;

import junit.framework.TestCase;

import common.Tools;
import crypto.keyexchange.RSA;

public class CryptoTest extends TestCase {

	public void testSha1() throws NoSuchAlgorithmException, UnsupportedEncodingException {

		// TEST SHA-1
		crypto.hash.SHA1 sha1 = new crypto.hash.SHA1();
		assertEquals(sha1.getSize(), 160);
		byte[] result = sha1.getHash("".getBytes("UTF-8"));
		assertEquals(Tools.toHexString(result),"DA39A3EE5E6B4B0D3255BFEF95601890AFD80709");
		result = sha1.getHash("abc".getBytes("UTF-8"));
		assertEquals(Tools.toHexString(result),"A9993E364706816ABA3E25717850C26C9CD0D89D");
		result = sha1.getHash("abcdefghijklmnopqrstuvwxyz".getBytes("UTF-8"));
		assertEquals(Tools.toHexString(result),"32D10C7B8CF96570CA04CE37F2A19D84240D3A89");
		
	}
	
	public void testSha256() throws UnsupportedEncodingException {
		// TEST SHA-256
		byte[] result;
		crypto.hash.SHA256 sha256 = new crypto.hash.SHA256();
		assertEquals(sha256.getSize(), 256);
		result = sha256.getHash("".getBytes("UTF-8"));
		assertEquals(Tools.toHexString(result),"E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855");
		result = sha256.getHash("abc".getBytes("UTF-8"));
		assertEquals(Tools.toHexString(result),"BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD");
		result = sha256.getHash("abcdefghijklmnopqrstuvwxyz".getBytes("UTF-8"));
		assertEquals(Tools.toHexString(result),"71C480DF93D6AE2F1EFAD1447C66C9525E316218CF51FC8D9ED832F2DAF18B73");
	}

	public void testRijndael() throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		crypto.ICipher aes = new crypto.cipher.Rijndael();
		byte[] key = new byte[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f','0','0','0','0','0','0','0','0'};
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
			aes.cipher(tmp, 0, res[i], 0);
		}
		aes.init(false, key);
		for(int i = 0; i < blocks; i++) {
			aes.cipher(res[i], 0, plain, i*16);
		}

		assertEquals(new String(plain), new String(text));
	}
	
	public void testRijndael2() throws InvalidKeyException {
		crypto.ICipher aes = new crypto.cipher.Rijndael2();
		byte[] key = new byte[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f','0','0','0','0','0','0','0','0'};
		
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
			aes.cipher(tmp, 0, res[i], 0);
		}
		aes.init(false, key);
		for(int i = 0; i < blocks; i++) {
			aes.cipher(res[i], 0, plain, i*16);
		}

		assertEquals(new String(plain), new String(text));
	}
	
	public void testDes() {
		crypto.ICipher des = new crypto.cipher.DES();
		byte[] key = new byte[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f','0','0','0','0','0','0','0','0'};
		des.init(true, key);
		byte[] textOriginal = "here is a littleaaaaaaaaaaaaaaaaaaaaaaaaaaaaajaskldjklsdjgaklgdfklgfjklghdfjklghdfjkghsdfjklghdfjkshqlkwejklqwjEKLØWJQKLEØJQWKLEJKLQWØEJKLAJKLJKLjklsdajkldjsakldjasklødjkaaaaaaaaa".getBytes();
		int block_size = des.getBlockSize();
		int rest = (block_size-(textOriginal.length%block_size));
		byte[] text = new byte[textOriginal.length+rest];
		System.arraycopy(textOriginal, 0, text, 0, textOriginal.length);
		byte[] plain = new byte[text.length];
		int blocks = (int)Math.ceil(text.length/block_size);
		byte[][] res = new byte[blocks][block_size];
		byte[] tmp = new byte[block_size];
		for(int i = 0; i < blocks; i++) {
			System.arraycopy(text, i*block_size, tmp, 0, block_size);
			des.cipher(tmp, 0, res[i], 0);
		}
		des.init(false, key);
		for(int i = 0; i < blocks; i++) {
			des.cipher(res[i], 0, plain, i*block_size);
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
	
	public void testRSA() {
		crypto.keyexchange.RSA rsa = new RSA(512);
		BigInteger secret = new BigInteger("1234567890");
		BigInteger encrypted = rsa.encrypt(secret);
		BigInteger decrypted = rsa.decrypt(encrypted);
		assertNotSame(encrypted.toString(), decrypted.toString());
		assertEquals(secret.toString(), decrypted.toString());
	}
	
	public void testZlip() {
		crypto.compression.ZLib zlib = new crypto.compression.ZLib();
		String message = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, " +
		"sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
		"Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris " +
		"nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in " +
		"reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla " +
		"pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa " +
		"qui officia deserunt mollit anim id est laborum";
		
		byte[] input = message.getBytes(TLSEngine.ENCODING);
		byte[] output = zlib.compress(input);
		byte[] res = zlib.decompress(output);
		
		assertEquals(new String(res,TLSEngine.ENCODING), message);
	}
}
