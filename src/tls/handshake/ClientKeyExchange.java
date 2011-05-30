package tls.handshake;

import tls.TLSHandshake;

import common.LogEvent;
import common.Tools;

import crypto.IKeyExchange;

/*
 * Client Key Exchange Message

   When this message will be sent:
      This message is always sent by the client.  It MUST immediately
      follow the client certificate message, if it is sent.  Otherwise,
      it MUST be the first message sent by the client after it receives
      the ServerHelloDone message.

   Meaning of this message:
      With this message, the premaster secret is set, either by direct
      transmission of the RSA-encrypted secret or by the transmission of
      Diffie-Hellman parameters that will allow each side to agree upon
      the same premaster secret.

      When the client is using an ephemeral Diffie-Hellman exponent,
      then this message contains the client's Diffie-Hellman public
      value.  If the client is sending a certificate containing a static
      DH exponent (i.e., it is doing fixed_dh client authentication),
      then this message MUST be sent but MUST be empty.

   Structure of this message:
      The choice of messages depends on which key exchange method has
      been selected.  See Section 7.4.3 for the KeyExchangeAlgorithm
      definition.

      struct {
          select (KeyExchangeAlgorithm) {
              case rsa:
                  EncryptedPreMasterSecret;
              case dhe_dss:
              case dhe_rsa:
              case dh_dss:
              case dh_rsa:
              case dh_anon:
                  ClientDiffieHellmanPublic;
          } exchange_keys;
      } ClientKeyExchange;

 */
public class ClientKeyExchange implements IHandshakeMessage {
	IKeyExchange keyExchange;
	byte[] preMasterSecret;
	
	public ClientKeyExchange(IKeyExchange keyExchange) {
			this.keyExchange = keyExchange;
	}
	
	public ClientKeyExchange(byte[] preMasterSecret) {
		this.preMasterSecret = preMasterSecret;
	}
	
	@Override
	public byte[] getByte() {
		if(preMasterSecret != null)
			return preMasterSecret;
		if(keyExchange.requireServerKeyExchange())
			return keyExchange.getPublicKey().toByteArray();
		Tools.printerr("returning null clientkeyexchange");
		return null;
	}

	@Override
	public byte getType() {
		return TLSHandshake.CLIENT_KEY_EXCHANGE;
	}

	@Override
	public String toString() {
		return "ClientKeyExchange";
	}

	@Override
	public String getStringValue() {
		if(keyExchange != null) {
			String tmp = "Algorithm: " + keyExchange.getName() + LogEvent.NEWLINE;
			tmp += "Public Key: " + keyExchange.getPublicKey() + LogEvent.NEWLINE;
			tmp += "Modulus: " + keyExchange.getPublicModulus();
			return tmp;
		}
		return "Pre-master secret: " + Tools.byteArrayToString(preMasterSecret);
	}

}
