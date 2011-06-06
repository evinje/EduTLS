package common;

import java.util.ArrayList;
import java.util.Observable;

/*
 * The Log class is a singleton class, used 
 * by various other components of the project
 * to achieve a global logging environment
 */
public class Log extends Observable {
	private static volatile Log INSTANCE = null;
	private ArrayList<LogEvent> events;
	
	public static Log get() { 
	if(INSTANCE == null) 
		INSTANCE = new Log();
	return INSTANCE;
	}
	
	private Log() {
		events = new ArrayList<LogEvent>();
	}
	
	public void add(LogEvent e) {
		events.add(e);
		setChanged();
		notifyObservers(e);
	}
	
	public void add(String title, String details) {
		add(new LogEvent(title, details));
	}
	
	public LogEvent get(int index) {
		if(index < 0 || index > events.size())
			return null;
		return events.get(index);
	}

}