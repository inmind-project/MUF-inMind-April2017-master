package edu.cmu.inmind.multiuser.openface.output;

import edu.cmu.inmind.multiuser.openface.OpenFaceEvent;

public interface EventOutput {
	
	void nextEvent(OpenFaceEvent e);
	
}
