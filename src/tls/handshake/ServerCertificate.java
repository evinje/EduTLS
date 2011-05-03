package tls.handshake;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import tls.TLSEngine;
import tls.TLSHandshake;
import common.Tools;
import crypto.IKeyExchange;

public class ServerCertificate implements IHandshakeMessage {
	private static String NL = "";
	private static int serialNumberCounter = 0;
	private static int versionNumber = 3;
	private static Object lock = new Object();
	private String issuer = "CN=EduTLSv2, O=NTNU, L=Trondheim, S=SorTrondelag, C=NO";
	private String subject = "";
	private Date notValidBefore;
	private Date notValidAfter;
	private int serialNumber;
	private IKeyExchange key;
	
	public ServerCertificate(byte[] certificate) {
		int subjectSize = certificate[0];
		byte[] subjectByte = new byte[subjectSize];
		Tools.byteCopy(certificate, subjectByte, 1);
		subject = new String(subjectByte);
	}
	
	public ServerCertificate(String commonName, ServerHello serverHello) {
		this.subject = "CN=" + commonName;
		this.key = serverHello.getChosenCipherSuite().getKeyExchange();
		Calendar calendar = new GregorianCalendar();
		notValidBefore = calendar.getTime();
		calendar.add(Calendar.MONTH, 1);
		notValidAfter = calendar.getTime();
		synchronized(lock) {
			serialNumber = serialNumberCounter++;
		}
	}
	
	/*
	 Certificate:
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
      X509v3 extensions:
          X509v3 Basic Constraints: critical
              CA:TRUE
  Signature Algorithm: md5WithRSAEncryption
      07:fa:4c:69:5c:fb:95:cc:46:ee:85:83:4d:21:30:8e:ca:d9:
      a8:6f:49:1a:e6:da:51:e3:60:70:6c:84:61:11:a1:1a:c8:48:
      3e:59:43:7d:4f:95:3d:a1:8b:b7:0b:62:98:7a:75:8a:dd:88:
      4e:4e:9e:40:db:a8:cc:32:74:b9:6f:0d:c6:e3:b3:44:0b:d9:
      8a:6f:9a:29:9b:99:18:28:3b:d1:e3:40:28:9a:5a:3c:d5:b5:
      e7:20:1b:8b:ca:a4:ab:8d:e9:51:d9:e2:4c:2c:59:a9:da:b9:
      b2:75:1b:f6:42:f2:ef:c7:f2:18:f9:89:bc:a3:ff:8a:23:2e:
      70:47
		 */

	@Override
	public byte[] getByte() {
		StringBuilder cert = new StringBuilder();
		cert.append("Certificate:" + NL);
		cert.append("Data:" + NL);
		cert.append("Version:" + versionNumber + NL);
		cert.append("Serial Number:" + serialNumber + NL);
		cert.append("Signature Algorithm:" + NL);
		cert.append("Issuer:" + issuer + NL);
		cert.append("Validity:" + NL);
		cert.append("Not Before:" + notValidBefore.toString() + NL);
		cert.append("Not After:" + notValidAfter.toString() + NL);
        cert.append("Subject:" + subject + NL);
        cert.append("Public Key Algorithm:" + key.getAlgorithm() + NL);
        cert.append("RSA Public Key: (" + key.getPublicKey().bitLength() + " bit)" + NL);
        cert.append("Modulus (" + key.getPublicModulus().bitLength() + " bit):" + NL);
        cert.append("" + key.getPublicModulus() + NL);
        cert.append("Exponent: " + key.getPublicKey() + NL);
        cert.append("Signature Algorithm: " + NL);
        cert.append("");
		return cert.toString().getBytes(TLSEngine.ENCODING);
	}

	@Override
	public byte getType() {
		return TLSHandshake.CERTIFICATE;
	}

	@Override
	public String getString() {
		return "ServerCertificate";
	}

}
