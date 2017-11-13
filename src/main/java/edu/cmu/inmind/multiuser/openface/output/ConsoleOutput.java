package edu.cmu.inmind.multiuser.openface.output;

import edu.cmu.inmind.multiuser.openface.OpenFaceEvent;

public class ConsoleOutput implements EventOutput {

	@Override
	public void nextEvent(OpenFaceEvent e) {
		System.err.println(e);
	}

}
