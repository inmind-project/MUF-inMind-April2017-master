package edu.cmu.inmind.multiuser.sara.component;

import java.io.IOException;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.model.NonVerbalOutput;
import edu.cmu.inmind.multiuser.controller.blackboard.Blackboard;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.common.Constants;
import edu.cmu.inmind.multiuser.controller.common.Utils;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StateType;
import edu.cmu.inmind.multiuser.openface.OpenFaceEvent;
import edu.cmu.inmind.multiuser.openface.eventDetector.EventDetector;
import edu.cmu.inmind.multiuser.openface.eventDetector.RuleBasedEventDetector;
import edu.cmu.inmind.multiuser.openface.input.OpenFaceInput;
import edu.cmu.inmind.multiuser.openface.input.ProcessInput;
import edu.cmu.inmind.multiuser.openface.output.EventOutput;

@BlackboardSubscription(messages={SaraCons.R5STREAM_DISCONNECTED,
		SaraCons.R5STREAM_CLOSE, SaraCons.R5STREAM_TIMEOUT, SaraCons.R5STREAM_ERROR, SaraCons.R5STREAM_STARTED})
@StateType( state = Constants.STATEFULL)
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
	public void onEvent(Blackboard blackboard, BlackboardEvent event) throws Exception
	{
		Log4J.debug(this, "received " + event.toString());
		if( event.getId().equals(SaraCons.R5STREAM_STARTED)) {
			runOpenFace();
		} else if(event.getId().equals(SaraCons.R5STREAM_DISCONNECTED) ||
				event.getId().equals(SaraCons.R5STREAM_CLOSE) ||
				event.getId().equals(SaraCons.R5STREAM_TIMEOUT) ||
				event.getId().equals(SaraCons.R5STREAM_ERROR))
		{
			stopOpenFace();
		} else {
			throw new RuntimeException("I've received an event that I don't like: " + event.toString());
		}
	}

	public void runOpenFace(){
		String url = Utils.getProperty("streamingURL") + getSessionId();
		Log4J.debug(this, "streaming from " + url);
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
		public void nextEvent(OpenFaceEvent e) {
			if (e != null) {
				NonVerbalOutput nvb = new NonVerbalOutput();
				nvb.setSmiling(e.getSmile());
				nvb.setGazeAtPartner(e.getGaze());
				try {
					getBlackBoard(getSessionId()).post(OpenFaceComponent.this, SaraCons.MSG_NVB, nvb);

				} catch (Throwable throwable) {
					throwable.printStackTrace();
				}
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
		super.shutDown();
	}


}
