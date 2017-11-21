package edu.cmu.inmind.multiuser.openface.output;

import edu.cmu.inmind.multiuser.openface.OpenFaceEvent;

public interface EventOutput {
	
	public void nextEvent(OpenFaceEvent e);
	
}
