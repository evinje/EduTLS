package common;

import java.util.Observable;


public class Log extends Observable {
	private static volatile Log INSTANCE = null;
	
	public static Log get() { 
	if(INSTANCE == null) 
		INSTANCE = new Log();
	return INSTANCE;
	}
	
	private Log() {
		// empty constructor
	}
	
	public void add(LogEvent e) {
		setChanged();
		notifyObservers(e);
	}
	
	public void add(String title, String details) {
		add(new LogEvent(title, details));
	}

}