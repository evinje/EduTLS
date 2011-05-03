package common;

/*
 * A LogEvent consists of a title and a description
 * It also includes a timestamp of when the object
 * was created.
 * 
 * @author 	Eivind Vinje
 * 
 */
public class LogEvent {
	public static long APP_START = System.currentTimeMillis();
	public static String NEWLINE = System.getProperty("line.separator");
	private String title;
	private StringBuilder details;
	private double time;
	
	/**
	 * Creates a log event
	 * @param title	The title of the log event
	 * @param details	A detailed description of the event
	 * @returns	Nothing, it is a constructor
	 */
	public LogEvent(String title, String details) {
		this.title = title;
		if(details.length()>0)
			this.details = new StringBuilder(details + NEWLINE);
		else
			this.details = new StringBuilder();
		this.time = Math.abs(System.currentTimeMillis() - APP_START);
	}
	
	/**
	 * @returns	String Details of the event
	 */
	public String getDetails() {
		if(details.toString().length()==0)
			return "<No details about this event>";
		return details.toString();
	}
	
	/**
	 * 
	 * @param details	Append this text to the log event details
	 * @returns	Nothing
	 */
	public void addDetails(String details) {
		this.details.append(details + NEWLINE);
//		Tools.print("New detail update for " + title + " : " + details);
	}
	
	/**
	 * The toString method returns the time, in seconds,
	 * between startup of the application and the time-
	 * stamp of this log event
	 * 
	 * Creates a log event
	 * @returns	String Returns the timestamp and title of the log event
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(time/1000);
		while(sb.length() < 8)
			sb.insert(0," ");
		return "[" + sb.toString() + "] " + title;
	}
}
