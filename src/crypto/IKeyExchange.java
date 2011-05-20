package crypto;

import java.math.BigInteger;
import java.util.ArrayList;

public interface IKeyExchange {
	public static int NUM_OF_PRIME_TESTS = 20;
	public BigInteger getPublicKey();
	public BigInteger getPublicModulus();
	public BigInteger getSecretKey();
	public BigInteger getServerKeyExchangeMessage();
	public String getAlgorithm();
	public boolean requireServerKeyExchange();
	public void initKeys(int size);
	public void setYb(BigInteger yb);
	public static ArrayList<IKeyExchange> allKeyExchangeAlgorithms = new ArrayList<IKeyExchange>();
}
