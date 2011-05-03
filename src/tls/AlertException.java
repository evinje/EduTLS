package tls;

import common.Log;
import common.LogEvent;

public class AlertException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3113852097560498542L;
	public static int close_notify = 0;
	public static int unexpected_message = 10;
	public static int bad_record_mac = 20;
	public static int decryption_failed = 21;
	public static int record_overflow = 22;
	public static int decompression_failure = 30;
	public static int handshake_failure = 40;
	public static int bad_certificate = 42;
	public static int unsupported_certificate = 43;
	public static int certificate_revoked = 44;
	public static int certificate_expired = 45;
	public static int certificate_unknown = 46;
	public static int illegal_parameter = 47;
	public static int unknown_ca = 48;
	public static int access_denied = 49;
	public static int decode_error = 50;
	public static int decrypt_error = 51;
	public static int export_restriction = 60;
	public static int protocol_version = 70;
	public static int insufficient_security = 71;
	public static int internal_error = 80;
	public static int user_canceled = 90;
	public static int no_renegotiation = 100;
	
	public static int alert_warning = 1;
	public static int alert_fatal = 2;
	
	private String alertDescription;
	private int alertLevel;
	private int alertCode;
	
	public AlertException() {}

	public AlertException(int level, int code, String description) {
	    super(description);
	    this.alertDescription = description;
	    this.alertLevel = level;
	    this.alertCode = code;
	    Log.get().add(new LogEvent("AlertException!",description));
//	    Tools.print("Oh, AlertException: " + description);
//	    Tools.print(Thread.currentThread().getStackTrace()[2].getClassName() + " " + Thread.currentThread().getStackTrace()[2].getLineNumber());
	    }
	
	public String toString() {
		return alertLevel + " " + alertCode + " - " + alertDescription;
	}
	
	public int getAlertLevel() {
		return alertLevel;
	}
	public int getAlertCode() {
		return alertCode;
	}
	public String getAlertDescription() {
		return alertDescription;
	}
}
