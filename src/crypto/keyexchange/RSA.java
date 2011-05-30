package crypto.keyexchange;
/*
 * Ideas in the RSA implementation is taken from
 * http://pajhome.org.uk/crypt/rsa/implementation.html
 * 
 * The miller rabin test is copied from
 * http://en.literateprograms.org/Miller-Rabin_primality_test_(Java)
 * 
 */

import java.math.BigInteger;
import java.util.Random;

import tls.TLSEngine;
import common.Tools;

public class RSA implements crypto.IKeyExchange {
	public final static String ALGORITHM_NAME = "RSA";
	private BigInteger n, d, e;
	private static final Random rnd = new Random();
	/**
	 * Generates a RSA key pair
	 * 
	 * @param size The number of bits in the key
	 * @returns	Nothing
	 */
	public RSA(int size) {
		initKeys(size);
	}
	
	
	public RSA(BigInteger modulus, BigInteger pubKey) {
		this.n = modulus;
		this.e = pubKey;
	}
	/**
	 * Creates a RSA instance with predefined
	 * values
	 * 
	 * @param modulus The modulus (n)
	 * @param pubKey The public value (e)
	 * @param modulus The private value (d)
	 * @returns	Nothing
	 */
	public RSA(BigInteger modulus, BigInteger pubKey, BigInteger privKey) {
		this.n = modulus;
		this.e = pubKey;
		this.d = privKey;
	}
	
	@Override
	public void initKeys(int size) {
		if(size<=0)
			return;
		//Globals.log.add("Starting to generate a " + size + " bits RSA key pair", Log.TYPE.INIT, "Generating random numbers and use the Miller–Rabin primality test to check whether it's a prime or not");
		StringBuilder primeGenLog = new StringBuilder();
		long time = System.currentTimeMillis();
		BigInteger p, q;
		int tests = 0;
		// The primes could be generated with new BigInteger(size, 1.0, rnd) 
		do {
			tests++;
			p = new BigInteger(size,rnd);
		} while(!None.miller_rabin(p));
		//Globals.log.add("Generated prime p in " + tests + " tests ", Log.TYPE.INIT, "Value of p:" + Globals.NEWLINE + p);
		tests = 0;
		do {
			tests++;
			q = new BigInteger(size,rnd);
		} while(!None.miller_rabin(q));
		//Globals.log.add("Generated prime q in " + tests + " tests ", Log.TYPE.INIT,"Value of q:" + Globals.NEWLINE + q);
		
		primeGenLog.append("Calculate n = p*q");
		n = p.multiply(q);
		primeGenLog.append("Find how many coprimes there exists of n by using the Euler's totient function: m = (p-1)*(q-1)");
		BigInteger m = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
		primeGenLog.append("Sets e initally to 65537, which is a common value to ensure efficient encryption and sufficient strength");
		e = new BigInteger("65537");
		while(m.gcd(e).intValue() > 1) 
			e = e.add(new BigInteger("2"));
		primeGenLog.append("Greatest Common Divisor (GCD) of e and m must be 1. To satisfy this, e is: " + e);
		primeGenLog.append("Calculates d to satisfy d*e=1 (mod m)");
		d = e.modInverse(m);
		primeGenLog.append("Generation of private and public key finished");
		primeGenLog.append("[RSA PRIVATE] " + roundBigInt(d));
		primeGenLog.append("[RSA PUBLIC] " + roundBigInt(e));
		primeGenLog.append("[RSA MODULUS] " + roundBigInt(n));
		//Tools.print(primeGenLog.toString());
		//Globals.log.add("Generated RSA keys in " + (System.currentTimeMillis()-time) + "ms", Log.TYPE.INIT,primeGenLog.toString());
	}
	
	private String roundBigInt(BigInteger large) {
		String s = large.toString().substring(0,1);
		s += "," + large.toString().substring(1,3);
		s += "*10^" + String.valueOf(large.toString().length()-1);
		return s;
	}
	
//	public BigInteger getPrivate() { return d; }
//	public BigInteger getPublic() { return e; }
//	public BigInteger getModulus() { return n; }
//	
	public BigInteger encrypt(BigInteger message) {
		return message.modPow(e, n);
	}

	public BigInteger decrypt(BigInteger message) {
		return message.modPow(d, n);
	}

	public BigInteger decrypt(String message) {
		return decrypt(new BigInteger(message.getBytes(TLSEngine.ENCODING)));
	}
	
	public BigInteger encrypt(String message) {
		return encrypt(new BigInteger(message.getBytes(TLSEngine.ENCODING)));
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("RSA Keys with size " + n.bitLength());
		sb.append("[RSA PRIVATE] " + d);
		sb.append("[RSA PUBLIC] " + e);
		sb.append("[RSA MODULUS] " + n);
		return sb.toString();
	}
//	
//	public static BigInteger pow(BigInteger base, BigInteger exp){
//		BigInteger p = BigInteger.ONE;
//		BigInteger zero = new BigInteger("0");
//		BigInteger two = new BigInteger("2");
//		while (exp.compareTo(zero) > 0) { 
//			if (exp.remainder(two).compareTo(BigInteger.ONE) == 0) 
//				p = p.multiply(base); 
//			base = base.multiply(base);
//			exp=exp.divide(two); 
//		}
//		return p;
//		}
	
	@Override
	public void setYb(BigInteger yb) {
		encrypt(yb);
	}
	
	@Override
	public BigInteger getSecretKey() {
		return d;
	}


	@Override
	public String getName() {
		return ALGORITHM_NAME;
	}

	@Override
	public BigInteger getPublicKey() {
		return e;
	}


	@Override
	public BigInteger getPublicModulus() {
		return n;
	}


	@Override
	public boolean requireServerKeyExchange() {
		return false;
	}


	@Override
	public BigInteger getServerKeyExchangeMessage() {
		// RSA does not use server key exchange message
		return null;
	}
}
