package crypto.keyexchange;

import java.math.BigInteger;
import java.util.Random;
import crypto.IKeyExchange;

public class DH implements IKeyExchange {
	// p = prime, g = base/generator
	BigInteger p, g;
	// X = private, Y = public
	BigInteger Xa, Ya, Yb;
	BigInteger secretKey;
	
	Random rnd;

	public DH(int bitLength) {
		// pre-generated p and g
		g = new BigInteger("7013892680988498279188554023449657170451011019571822843525388197555796586834894509532314177249376660084062463676554469138711964180514384717777200947267417");
		p = new BigInteger("7468600078466426114796463824125526412203106612609790292466106804957294177866776167499103011102236201812549527633940659531011732848077078911342294621247679");
		rnd = new Random();
		Xa = new BigInteger(bitLength, rnd);
		Ya = g.modPow(Xa,p);
//		AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
//		paramGen.init(512);
//		AlgorithmParameters params = paramGen.generateParameters();
//		DHParameterSpec dhSkipParamSpec = (DHParameterSpec)params.getParameterSpec(DHParameterSpec.class);
//		KeyPairGenerator aliceKpairGen = KeyPairGenerator.getInstance("DH");
//        aliceKpairGen.initialize(dhSkipParamSpec);
//        KeyPair aliceKpair = aliceKpairGen.generateKeyPair();
//        KeyAgreement aliceKeyAgree = KeyAgreement.getInstance("DH");
//        aliceKeyAgree.init(aliceKpair.getPrivate());
//        Tools.print(dhSkipParamSpec.getG().toString());
//        Tools.print(dhSkipParamSpec.getP().toString());
	}
	
	
	public DH(BigInteger p, BigInteger g) {
		this.p = p;
		this.g = g;
	}

	@Override
	public BigInteger getPublicKey() {
		return Ya;
	}
	
	public void setYb(BigInteger Yb) {
		this.Yb = Yb;
		secretKey = Yb.modPow(Xa, p);
	}
	
	public void setP(BigInteger p) {
		this.p = p;
	}

	@Override
	public BigInteger getPublicModulus() {
		return p;
	}
	
	@Override
	public BigInteger getServerKeyExchangeMessage() {
		return g;
	}

	@Override
	public String getAlgorithm() {
		return "DiffieHellman";
	}

	@Override
	public boolean requireServerKeyExchange() {
		return true;
	}
	
	@Override
	public BigInteger getSecretKey() {
		return secretKey;
	}

}
