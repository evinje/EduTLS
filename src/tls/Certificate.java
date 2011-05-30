package tls;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import common.Tools;

import crypto.IKeyExchange;
import crypto.IHash;

public class Certificate {
	/*
	 * Certificate:
   Data:
       Version: 3 (0x2)
       Serial Number: 1 (0x1)
       Signature Algorithm: md5WithRSAEncryption
       Issuer: C=ZA, ST=Western Cape, L=Cape Town, O=Thawte Consulting cc,
               OU=Certification Services Division,
               CN=Thawte Server CA/emailAddress=server-certs@thawte.com
       Validity
           Not Before: Aug  1 00:00:00 1996 GMT
           Not After : Dec 31 23:59:59 2020 GMT
       Subject: C=ZA, ST=Western Cape, L=Cape Town, O=Thawte Consulting cc,
                OU=Certification Services Division,
                CN=Thawte Server CA/emailAddress=server-certs@thawte.com
       Subject Public Key Info:
           Public Key Algorithm: rsaEncryption
           RSA Public Key: (1024 bit)
               Modulus (1024 bit):
                   00:d3:a4:50:6e:c8:ff:56:6b:e6:cf:5d:b6:ea:0c:
                   68:75:47:a2:aa:c2:da:84:25:fc:a8:f4:47:51:da:
                   85:b5:20:74:94:86:1e:0f:75:c9:e9:08:61:f5:06:
                   6d:30:6e:15:19:02:e9:52:c0:62:db:4d:99:9e:e2:
                   6a:0c:44:38:cd:fe:be:e3:64:09:70:c5:fe:b1:6b:
                   29:b6:2f:49:c8:3b:d4:27:04:25:10:97:2f:e7:90:
                   6d:c0:28:42:99:d7:4c:43:de:c3:f5:21:6d:54:9f:
                   5d:c3:58:e1:c0:e4:d9:5b:b0:b8:dc:b4:7b:df:36:
                   3a:c2:b5:66:22:12:d6:87:0d
               Exponent: 65537 (0x10001)

	 */
	private static int serialNumberMax = 1;
	private int version = 1;
	private int serialNumber;
	private String issuer = "C=NO,L=Trondheim,O=NTNU,OU=ITEM,CN=EduTLSv2 CA/emailAddress=eivinvi@stud.ntnu.no";
	private String subject;
	private long notValidBefore;
	private long notValidAfter;
	private IKeyExchange publicKey;
	private IHash mac;
	
	public Certificate(String subject, IKeyExchange publicKey, IHash mac) {
		this.serialNumber = serialNumberMax;
		this.notValidBefore = System.currentTimeMillis();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(notValidBefore);
		cal.add(Calendar.YEAR, 1);
		this.notValidAfter = cal.getTimeInMillis();
		this.publicKey = publicKey;
		this.subject = subject;
		this.mac = mac;
		serialNumberMax++;
		// 1297415916451
	}
	
	public Certificate(byte[] cert) throws AlertException {
		if(cert[0] != 48)
			throw new AlertException(AlertException.alert_fatal, AlertException.handshake_failure,"Invalid certificate");
		version = (int)cert[1];
		serialNumber = (int)cert[2]*256+(int)cert[3];
		notValidBefore = Tools.byteArrayToLong(cert, 4);
		notValidAfter = Tools.byteArrayToLong(cert, 12);
		byte[] subjectByteValue = new byte[cert.length-20];
		Tools.byteCopy(cert, subjectByteValue, 20);
		subject = new String(subjectByteValue);
	}
	
	public byte[] getByteValue() throws UnsupportedEncodingException {
		// X509v3 HEADER
		// version 1, sn 2 
		// time 8+8
		byte[] info = new byte[4+8+8+subject.getBytes(TLSEngine.ENCODING).length];
		info[0] = (byte)48;
		info[1] = (byte)version;
		info[2] = (byte)(serialNumber/256);
		info[3] = (byte)(serialNumber%256);
		Tools.longToByteArray(notValidBefore, info, 4);
		Tools.longToByteArray(notValidAfter, info, 12);
		Tools.byteCopy(subject.getBytes(TLSEngine.ENCODING), info, 20);
		return info;
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Certificate:\n  Data:\n");
		str.append("\tVersion: " + version + "\n");
		str.append("\tSerial Number: " + serialNumber + "\n");
		str.append("\tIssuer: " + issuer + "\n");
		str.append("\tValidity: \n");
		str.append("\t\tNot Before: " + getDateFormated(notValidBefore) + "\n");
		str.append("\t\tNot After: " + getDateFormated(notValidAfter) + "\n");
		str.append("\tSubject: " + subject + "\n");
		str.append("\tSubject Public Key Info:" + "\n");
		str.append("\tPublic Key Algorithm: " + publicKey.getName() + "\n");
		str.append("\tRSA Public Key: (" + publicKey.getPublicModulus().bitCount() + " bit) \n");
		str.append("\t\tModulus:" + publicKey.getPublicModulus() + "\n");
		str.append("\t\tExponent: " + publicKey.getPublicKey());
		return str.toString();
	}
	
	private String getDateFormated(long time) {
		return DateFormat.getInstance().format(new Date(time));
	}
	
	public int getSerialNumber() {
		return serialNumber;
	}
	
	public long getNotValidBefore() {
		return notValidBefore;
	}
	
	public long getNotValidAfter() {
		return notValidAfter;
	}
	
	public String getSubject() {
		return subject;
	}
}
