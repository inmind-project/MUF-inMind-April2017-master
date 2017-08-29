package edu.cmu.inmind.multiuser.openface.output;

import edu.cmu.inmind.multiuser.openface.Event;

public class ConsoleOutput implements EventOutput {

	@Override
	public void nextEvent(Event e) {
		System.err.println(e);
	}

}
