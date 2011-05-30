package crypto.keyexchange;

import java.math.BigInteger;
import java.util.Random;

public class None implements crypto.IKeyExchange {
	private static Random rnd = new Random();
	
	@Override
	public BigInteger getPublicKey() {
		return BigInteger.ZERO;
	}

	@Override
	public BigInteger getPublicModulus() {
		return BigInteger.ZERO;
	}

	@Override
	public String getName() {
		return "None";
	}

	@Override
	public boolean requireServerKeyExchange() {
		return false;
	}

	public static boolean miller_rabin(BigInteger n) {
		// this is an even
		if(!n.testBit(0))
			return false;
		BigInteger a;
	    for (int i = 0; i < crypto.IKeyExchange.NUM_OF_PRIME_TESTS; i++) {
	        do {
	            a = new BigInteger(n.bitLength(), rnd);
	        } while (a.equals(BigInteger.ZERO));
	        if (!miller_rabin_pass(a, n)) {
	            return false;
	        }
	    }
	    return true;
	}
	
	private static boolean miller_rabin_pass(BigInteger a, BigInteger n) {
	    BigInteger n_minus_one = n.subtract(BigInteger.ONE);
	    BigInteger d = n_minus_one;
	    int s = d.getLowestSetBit();
	    d = d.shiftRight(s);
	    BigInteger a_to_power = a.modPow(d, n);
	    if (a_to_power.equals(BigInteger.ONE)) return true;
	    for (int i = 0; i < s-1; i++) {
	        if (a_to_power.equals(n_minus_one)) return true;
	        a_to_power = a_to_power.multiply(a_to_power).mod(n);
	    }
	    if (a_to_power.equals(n_minus_one)) return true;
	    return false;
	}

	@Override
	public BigInteger getServerKeyExchangeMessage() {
		return null;
	}

	@Override
	public BigInteger getSecretKey() {
		return BigInteger.ZERO;
	}

	@Override
	public void initKeys(int size) {
		
	}

	@Override
	public void setYb(BigInteger yb) {
		
	}
}
