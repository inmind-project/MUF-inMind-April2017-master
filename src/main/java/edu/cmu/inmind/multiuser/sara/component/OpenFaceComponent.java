package edu.cmu.inmind.multiuser.sara.component;

import java.io.IOException;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.model.NonVerbalOutput;
import edu.cmu.inmind.multiuser.controller.blackboard.Blackboard;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StatefulComponent;
import edu.cmu.inmind.multiuser.openface.Event;
import edu.cmu.inmind.multiuser.openface.eventDetector.EventDetector;
import edu.cmu.inmind.multiuser.openface.eventDetector.RuleBasedEventDetector;
import edu.cmu.inmind.multiuser.openface.input.OpenFaceInput;
import edu.cmu.inmind.multiuser.openface.input.ProcessInput;
import edu.cmu.inmind.multiuser.openface.output.ConsoleOutput;
import edu.cmu.inmind.multiuser.openface.output.EventOutput;
import edu.cmu.inmind.multiuser.openface.output.VHTOutput;

@BlackboardSubscription(messages={"MSG_START_SESSION"})
@StatefulComponent
public class OpenFaceComponent extends PluggableComponent {

	@Override
	public void execute() {

	} // we do the heavy lifting from an event, rather than in execute()

	public void runOpenFace(){
		System.out.println("Hello from OpenFace");
		String sessionID = getSessionId();
		//sessionID = "54201342a4cfb96d"; // FIXME: Timo's phone hardwired for now
		String url = "rtsp://34.203.204.136:8554/live/myStream" + sessionID;
		startupAndReturn(url);
	}

	@Override
	public void onEvent(BlackboardEvent event) {
		if (event.getId().equals("MSG_START_SESSION")) {
			//System.out.println(" ###################### Message from OpenFace");
			runOpenFace();
		}

	}

	private void startupAndReturn(final String fileOrURL) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					OpenFaceInput ofi = new ProcessInput(fileOrURL);
					EventDetector ed = new RuleBasedEventDetector(ofi);
					ed.addListener(new VHTOutput());
					ed.addListener(new MUFOutput());
					ed.run();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, "OpenFace Thread").start();
	}

	/** send stuff towards MUF from our blackboard */
	private class MUFOutput implements EventOutput {
		@Override
		public void nextEvent(Event e) {
			if (e != null) {
				NonVerbalOutput nvb = new NonVerbalOutput();
				nvb.setSmiling(e.getSmile());
				nvb.setGazeAtPartner(e.getGaze());
                //System.out.println(" ###################### is Smiling?" + e.getSmile());
				OpenFaceComponent.this.blackboard().post(OpenFaceComponent.this, SaraCons.MSG_NVB, nvb);
			}
		}
	}
	
}
