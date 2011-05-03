package tls;

import java.util.ArrayList;

public class ConnectionStates {
	ArrayList<State> states;
	
	public ConnectionStates() {
		states = new ArrayList<State>();
	}
	
	public boolean stateExist(String host) {
		for(State state : states) {
			if(state.getPeerHost().equalsIgnoreCase(host))
				return true;
		}
		return false;
	}
	
	public State getState(String host) {
		for(State state : states) {
			if(state.getPeerHost().equalsIgnoreCase(host))
				return state;
		}
		return null;
	}
	
	public void addState(State state) {
//		if(stateExist(state.getPeerHost()))
//			states.remove(state);
		states.add(state);
	}

}
