package tls;

public interface IApplication {
	public static enum STATUS { SESSION_TIMEOUT, INCOMING_CONNECTION };
	public void getMessage(byte[] message);
	public void getStatus(STATUS status, String message, String details);
}
