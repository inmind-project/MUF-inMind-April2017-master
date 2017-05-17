package edu.cmu.inmind.multiuser.sara.component;

import java.io.IOException;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.Utils;
import edu.cmu.inmind.multiuser.common.model.NonVerbalOutput;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StatefulComponent;
import edu.cmu.inmind.multiuser.openface.Event;
import edu.cmu.inmind.multiuser.openface.eventDetector.EventDetector;
import edu.cmu.inmind.multiuser.openface.eventDetector.RuleBasedEventDetector;
import edu.cmu.inmind.multiuser.openface.input.OpenFaceInput;
import edu.cmu.inmind.multiuser.openface.input.ProcessInput;
import edu.cmu.inmind.multiuser.openface.output.EventOutput;
import edu.cmu.inmind.multiuser.openface.output.VHTOutput;

@BlackboardSubscription(messages={"MSG_START_SESSION"})
@StatefulComponent
public class OpenFaceComponent extends PluggableComponent {

	@Override
	public void execute() {

	} // we do the heavy lifting from an event, rather than in execute()

	@Override
	public void onEvent(BlackboardEvent event) {
		if (event.getId().equals("MSG_START_SESSION")) {
			runOpenFace();
		}
	}

	public void runOpenFace(){
		String url = Utils.getProperty("streamingURL") + getSessionId();
		startupAndReturn(url);
	}

	private void startupAndReturn(final String fileOrURL) {
		new Thread(() -> {
			try {
				OpenFaceInput ofi = new ProcessInput(fileOrURL);
				EventDetector ed = new RuleBasedEventDetector(ofi);
//					ed.addListener(new VHTOutput());
				ed.addListener(new MUFOutput());
				ed.run();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}, "OpenFace Thread for session " + getSessionId()).start();
	}

	/** send stuff towards MUF from our blackboard */
	private class MUFOutput implements EventOutput {
		@Override
		public void nextEvent(Event e) {
			if (e != null) {
				NonVerbalOutput nvb = new NonVerbalOutput();
				nvb.setSmiling(e.getSmile());
				nvb.setGazeAtPartner(e.getGaze());
				OpenFaceComponent.this.blackboard().post(OpenFaceComponent.this, SaraCons.MSG_NVB, nvb);
			}
		}
	}
	
}
