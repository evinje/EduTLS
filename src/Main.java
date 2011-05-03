import java.util.Observable;
import java.util.Observer;

import common.LogEvent;
import common.Tools;


public class Main implements Observer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new server.Listener(new gui.EduTLS());
	}

	@Override
	public void update(Observable o, Object arg) {
		LogEvent le = (LogEvent)arg;
		Tools.print(le.toString() + le.getDetails());
	}

}
