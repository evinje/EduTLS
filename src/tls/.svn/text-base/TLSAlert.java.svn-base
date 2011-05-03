package tls;

public class TLSAlert {
	byte[] content;
	
	public TLSAlert(int alertLevel, int alertCode) {
		content = new byte[] { (byte)alertLevel, (byte)alertCode };
	}
	
	public TLSAlert(byte[] content) throws AlertException {
		if(content.length != 2)
			throw new AlertException(AlertException.alert_fatal, AlertException.unexpected_message, "Wrong length");
		if(content[0] != AlertException.alert_fatal || content[0] != AlertException.alert_warning)
			throw new AlertException(AlertException.alert_fatal, AlertException.illegal_parameter, "Unknown AlertLevel");
		if(content[1] < 0)
			throw new AlertException(AlertException.alert_fatal, AlertException.illegal_parameter, "Negative AlertCode");
		this.content = content;
	}
	
	public int getAlertLevel() {
		return content[0];
	}
	
	public int getAlertCode() {
		return content[1];
	}
	
	public byte[] getContent() {
		return content;
	}
	
	public String toString() {
		return content[0] + "" + content[1];
	}
	
}
