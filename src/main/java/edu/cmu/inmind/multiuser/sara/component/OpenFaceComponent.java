package edu.cmu.inmind.multiuser.sara.component;

import java.io.IOException;

import edu.cmu.inmind.multiuser.common.Constants;
import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.Utils;
import edu.cmu.inmind.multiuser.common.model.NonVerbalOutput;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.communication.SessionMessage;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StatefulComponent;
import edu.cmu.inmind.multiuser.openface.Event;
import edu.cmu.inmind.multiuser.openface.eventDetector.EventDetector;
import edu.cmu.inmind.multiuser.openface.eventDetector.RuleBasedEventDetector;
import edu.cmu.inmind.multiuser.openface.input.OpenFaceInput;
import edu.cmu.inmind.multiuser.openface.input.ProcessInput;
import edu.cmu.inmind.multiuser.openface.output.EventOutput;
import edu.cmu.inmind.multiuser.openface.output.VHTOutput;

@BlackboardSubscription(messages={SaraCons.R5STREAM_DISCONNECTED,
		SaraCons.R5STREAM_CLOSE, SaraCons.R5STREAM_TIMEOUT, SaraCons.R5STREAM_ERROR, SaraCons.R5STREAM_STARTED})
@StatefulComponent
public class OpenFaceComponent extends PluggableComponent {

	Thread openFaceThread;

	@Override
	public void startUp(){
		super.startUp();
//        postCreate();
//        postCreateblackboard().post(this, SaraCons.MSG_ASR, "Hello");
		//blackboard().post(this, SaraCons.MSG_ASR, "Hello");
		Log4J.info(this, "OpenFace Component: startup has finished.");
	}

	@Override
	public void execute() {

	} // we do the heavy lifting from an event, rather than in execute()

	@Override
	public void onEvent(BlackboardEvent event) {
		Log4J.debug(this, "received " + event.toString());
		if( event.getId().equals(SaraCons.R5STREAM_STARTED))
		{
			runOpenFace();
		}
		else if(event.getId().equals(SaraCons.R5STREAM_DISCONNECTED) ||
				event.getId().equals(SaraCons.R5STREAM_CLOSE) ||
				event.getId().equals(SaraCons.R5STREAM_TIMEOUT) ||
				event.getId().equals(SaraCons.R5STREAM_ERROR))
		{
			stopOpenFace();
		}
	}

	public void runOpenFace(){
		String url = Utils.getProperty("streamingURL") + getSessionId();
		startupAndReturn(url);
	}

	private void startupAndReturn(final String fileOrURL) {
		openFaceThread = new Thread(() -> {
			try {
				OpenFaceInput ofi = new ProcessInput(fileOrURL);
				EventDetector ed = new RuleBasedEventDetector(ofi);
//					ed.addListener(new VHTOutput());
				ed.addListener(new MUFOutput());
				ed.run();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}, "OpenFace Thread for session " + getSessionId());
		openFaceThread.start();
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

	private void stopOpenFace()
	{
		if(openFaceThread.isAlive())
		{
			openFaceThread.interrupt();
			openFaceThread = null;
		}
	}

	@Override
	public void shutDown() {
		stopOpenFace();
	}


}
