package edu.cmu.inmind.multiuser.openface.output;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.Utils;
import edu.cmu.inmind.multiuser.common.model.NonVerbalOutput;
import edu.cmu.inmind.multiuser.controller.blackboard.Blackboard;
import edu.cmu.inmind.multiuser.openface.Event;
import edu.cmu.inmind.multiuser.sara.component.OpenFaceComponent;
import edu.usc.ict.vhmsg.VHMsg;


public class VHTOutput implements EventOutput {

	private VHMsg sender;
	private Blackboard blackboard;
	OpenFaceComponent openFaceComponent;
	
	public VHTOutput(){
		sender = new VHMsg();
		sender.openConnection(Utils.getProperty("VHMSG_SERVER_URL"));
	}
	
	@Override
	public void nextEvent(Event e) {
		if (e != null) {
			sender.sendMessage("vrMultisense 0 " + e.getSmile() + " 0.939988519996405 false false neutral 1.0 true");
		}
	}

}
