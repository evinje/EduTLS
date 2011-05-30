package tls.handshake;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import tls.CertificateAuthority;
import tls.TLSEngine;
import tls.TLSHandshake;

import common.LogEvent;
import common.Tools;

import crypto.IKeyExchange;

public class ServerCertificate implements IHandshakeMessage {
	private final static String CERTIFICATE_INFO = "Certificate:";
	private final static String DATA_INFO = "Data:";
	private final static String VERSION_INFO = "Version:";
	private final static String SERIAL_NUMBER_INFO = "Serial Number:";
	private final static String SIGNATURE_ALG_INFO = "Signature Algorithm:";
	private final static String ISSUER_INFO = "Issuer:";
	private final static String VALIDITY_INFO = "Validity:";
	private final static String VALID_NOT_BEFORE_INFO = "Not Before:";
	private final static String VALID_NOT_AFTER_INFO = "Not After:";
	private final static String SUBJECT_INFO = "Subject:";
	private final static String PUBLIC_KEY_ALG_INFO = "Public Key Algorithm:";
	private final static String PUBLIC_KEY_INFO = "Public Key";
	private final static String MODULUS_INFO = "Modulus";
	private final static String EXPONENT_INFO = "Exponent:";
	
	private static final String NL = LogEvent.NEWLINE;
	private static int serialNumberCounter = 1;
	private static int versionNumber = 1;
	private static Object lock = new Object();
	private static CertificateAuthority ca = new CertificateAuthority();
	private String issuer = "CN=EduTLSv2, O=NTNU, L=Trondheim, S=SorTrondelag, C=NO";
	private String subject = "";
	private Date notValidBefore;
	private Date notValidAfter;
	private int serialNumber;
	private IKeyExchange key;
	private GregorianCalendar calendar;
	
	
	public ServerCertificate(String commonName, ServerHello serverHello) {
		this.subject = "CN=" + commonName;
		this.key = serverHello.getChosenCipherSuite().getKeyExchange();
		calendar = new GregorianCalendar();
		notValidBefore = calendar.getTime();
		calendar.add(Calendar.MONTH, 1);
		notValidAfter = calendar.getTime();
		synchronized(lock) {
			serialNumber = serialNumberCounter++;
		}
	}
	
	public ServerCertificate(byte[] certificate) {
		String cert = new String(certificate, TLSEngine.ENCODING);
		int serialNumberStart = cert.indexOf(SERIAL_NUMBER_INFO)+SERIAL_NUMBER_INFO.length();
		int serialNumberEnd = cert.indexOf(NL,serialNumberStart);
		try {
			serialNumber = Integer.parseInt(cert.substring(serialNumberStart,serialNumberEnd));
		} catch(Exception nfe) {
			Tools.print("Error SN: start: " + serialNumberStart + " end: " + serialNumberEnd);
			serialNumber=-1;
		}
		int subjectStart = cert.indexOf(SUBJECT_INFO)+SUBJECT_INFO.length();
		int subjectEnd = cert.indexOf(NL, subjectStart);
		subject = cert.substring(subjectStart,subjectEnd);
		int notValidBeforeStart = cert.indexOf(VALID_NOT_BEFORE_INFO)+VALID_NOT_BEFORE_INFO.length();
		int notValidBeforeEnd = cert.indexOf(NL, notValidBeforeStart);
		int notValidAfterStart = cert.indexOf(VALID_NOT_AFTER_INFO)+VALID_NOT_AFTER_INFO.length();
		int notValidAfterEnd = cert.indexOf(NL, notValidAfterStart);
		calendar = new GregorianCalendar();
		// Locale.US since month are in english
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.US);
		/*
		 * The dates are not important to us. Of course, a invalid date should
		 * throw an exception in a real environment. It should also
		 * be checked that "today" is between valid_before and valid_after
		 */
		try {
			notValidBefore = sdf.parse(cert.substring(notValidBeforeStart, notValidBeforeEnd));
			notValidAfter = sdf.parse(cert.substring(notValidAfterStart, notValidAfterEnd));
		} catch (ParseException e) {
			notValidBefore = calendar.getTime();
			notValidAfter = calendar.getTime();
		}
		int keyAlgorithmStart = cert.indexOf(PUBLIC_KEY_ALG_INFO)+PUBLIC_KEY_ALG_INFO.length();
		int keyAlgorithmEnd = cert.indexOf(NL, keyAlgorithmStart);
		String keyInf = cert.substring(keyAlgorithmStart, keyAlgorithmEnd);
		
		int keyBitLengthStart = cert.indexOf(PUBLIC_KEY_INFO)+PUBLIC_KEY_INFO.length();
		keyBitLengthStart = cert.indexOf("(",keyBitLengthStart);
		int publicKeyModulusStart = cert.indexOf(MODULUS_INFO)+MODULUS_INFO.length();
		publicKeyModulusStart = cert.indexOf(NL, publicKeyModulusStart)+2;
		int publicKeyModulusEnd = cert.indexOf(NL, publicKeyModulusStart);
		String publicModulusKey = cert.substring(publicKeyModulusStart, publicKeyModulusEnd);
		int publicExponentKeyStart = cert.indexOf(EXPONENT_INFO)+EXPONENT_INFO.length();
		int publicExponentKeyEnd = cert.indexOf(NL, publicExponentKeyStart);
		String publicExponentKey = cert.substring(publicExponentKeyStart, publicExponentKeyEnd);
		
		BigInteger intPublicKeyModulus, intPublicExponentKey;
		try {
			intPublicKeyModulus = new BigInteger(publicModulusKey);
			intPublicExponentKey = new BigInteger(publicExponentKey);
		} catch(Exception e) {
			intPublicKeyModulus = BigInteger.ZERO;
			intPublicExponentKey = BigInteger.ZERO;
		}
		/*
		 * If the algorithm is not DH or RSA, we simply ignore it
		 */
		if(keyInf.equals(crypto.keyexchange.DH.ALGORITHM_NAME))
			key = new crypto.keyexchange.DH(intPublicKeyModulus, intPublicExponentKey);
		else if(keyInf.equals(crypto.keyexchange.RSA.ALGORITHM_NAME))
			key = new crypto.keyexchange.RSA(intPublicKeyModulus, intPublicExponentKey);
		else
			key = new crypto.keyexchange.None();
		

	}
	
	@Override
	public byte[] getByte() {
		return getStringValue().getBytes(TLSEngine.ENCODING);
	}

	@Override
	public byte getType() {
		return TLSHandshake.CERTIFICATE;
	}

	@Override
	public String toString() {
		return "ServerCertificate";
	}
	
	@Override
	public String getStringValue() {
		StringBuilder cert = new StringBuilder();
		cert.append(CERTIFICATE_INFO + NL);
		cert.append(DATA_INFO + NL);
		cert.append(VERSION_INFO + versionNumber + NL);
		cert.append(SERIAL_NUMBER_INFO + serialNumber + NL);
		cert.append(ISSUER_INFO + issuer + NL);
		cert.append(VALIDITY_INFO + NL);
		cert.append(VALID_NOT_BEFORE_INFO + notValidBefore.toString() + NL);
		cert.append(VALID_NOT_AFTER_INFO + notValidAfter.toString() + NL);
        cert.append(SUBJECT_INFO + subject + NL);
        cert.append(PUBLIC_KEY_ALG_INFO + key.getName() + NL);
        cert.append(PUBLIC_KEY_INFO + " (" + key.getPublicKey().bitLength() + " bit):" + NL);
        cert.append(MODULUS_INFO + " (" + key.getPublicModulus().bitLength() + " bit):" + NL);
        cert.append("" + key.getPublicModulus() + NL);
        cert.append(EXPONENT_INFO + key.getPublicKey() + NL);
        String signature = ca.getSignature(cert.toString());
        cert.append(SIGNATURE_ALG_INFO + ca.getSignatureAlgorithm() + NL);
        cert.append(signature);
		return cert.toString();
	}

}
