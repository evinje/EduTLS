package common;

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
	
	public String getDetails() {
		if(details.toString().length()==0)
			return "<No details about this event>";
		return details.toString();
	}
	
	public void setDetails(String details) {
		this.details.append(details + NEWLINE);
//		Tools.print("New detail update for " + title + " : " + details);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(time/1000);
		while(sb.length() < 8)
			sb.insert(0," ");
		return "[" + sb.toString() + "] " + title;
	}
}
