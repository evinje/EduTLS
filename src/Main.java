import java.util.Observable;
import java.util.Observer;

import common.Log;
import common.LogEvent;
import common.Tools;


public class Main implements Observer {

	/**
	 * @param args Parameters not in use.
	 */
	public static void main(String[] args) {
		new Main();
	}
	
	public Main() {
		// Add this class as an observer for debugging purposes
		Log.get().addObserver(this);
		new server.Listener(new gui.ChatGui());
	}

	@Override
	public void update(Observable o, Object arg) {
		LogEvent le = (LogEvent)arg;
		Tools.print(le.toString() + " " + le.getDetails());
	}

}
