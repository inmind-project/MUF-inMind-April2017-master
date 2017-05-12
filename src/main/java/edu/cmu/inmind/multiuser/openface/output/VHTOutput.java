package edu.cmu.inmind.multiuser.openface.output;

import edu.cmu.inmind.multiuser.common.Utils;
import edu.cmu.inmind.multiuser.openface.Event;
import edu.usc.ict.vhmsg.VHMsg;


public class VHTOutput implements EventOutput {

	private VHMsg sender;

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
