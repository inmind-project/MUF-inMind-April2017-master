package edu.cmu.inmind.multiuser.sara.orchestrator;

import beat.bson.BSON;
import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.model.ASROutput;
import edu.cmu.inmind.multiuser.common.model.R5StreamListener;
import edu.cmu.inmind.multiuser.controller.blackboard.Blackboard;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.common.Utils;
import edu.cmu.inmind.multiuser.controller.communication.SessionMessage;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.orchestrator.ProcessOrchestratorImpl;
import edu.cmu.inmind.multiuser.controller.session.Session;

/**
 * Created by oscarr on 3/3/17.
 */
@BlackboardSubscription( messages = {SaraCons.MSG_NLG, SaraCons.MSG_START_STREAMING})
public class SaraOrchestrator extends ProcessOrchestratorImpl {
    private BSON response = new BSON();
    private R5StreamListener r5StreamListener= new R5StreamListener();
    private long time;
    private boolean resetCrono = true;

    @Override
    public void initialize(Session session) throws Exception{
        try {
            super.initialize( session );
            //turn off the logger to make the system faster
            //Log4J.turnOn(false);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }

    @Override
    public void process(String message) throws Throwable {
        Log4J.debug(this, "orchestrator received message " + message);
        if( resetCrono ) {
            time = System.currentTimeMillis();
            resetCrono = false;
        }
        super.process(message);

        if( message != null && !message.isEmpty() ) {
            SessionMessage inputMessage = Utils.fromJson(message, SessionMessage.class);
            if (inputMessage.getMessageId().equals(SaraCons.MSG_START_SESSION)) {
                Log4J.info(this, "MSG_START_SESSION");
                blackboard.post(this, inputMessage.getMessageId(), inputMessage.getPayload());
            } else if (inputMessage.getMessageId().equals(SaraCons.R5STREAM_STARTED) ||
                    inputMessage.getMessageId().equals(SaraCons.R5STREAM_DISCONNECTED) ||
                    inputMessage.getMessageId().equals(SaraCons.R5STREAM_CLOSE) ||
                    inputMessage.getMessageId().equals(SaraCons.R5STREAM_TIMEOUT) ||
                    inputMessage.getMessageId().equals(SaraCons.R5STREAM_ERROR)) {
                blackboard.post(this, inputMessage.getMessageId(), inputMessage.getPayload());
            } else if (inputMessage.getMessageId().equals(SaraCons.MSG_START_DM)) {
                Log4J.info(this, SaraCons.MSG_START_DM);
                blackboard.post(this, inputMessage.getMessageId(), inputMessage.getPayload());
            } else {
                ASROutput asrOutput = Utils.fromJson(inputMessage.getPayload(), ASROutput.class);
                blackboard.post(this, inputMessage.getMessageId(), asrOutput);
                if (inputMessage.getMessageId().equals(SaraCons.MSG_ASR)) {
                    Log4J.info(this, "ASR - > utterance: " + asrOutput.getUtterance() + "  confidence: "
                            + asrOutput.getConfidence());
                }
            }
        }
    }

    @Override
    /**
     * This method will be called when the system has a response to send out, that is (in our example).
     */
    public void onEvent(Blackboard blackboard, BlackboardEvent event) throws Throwable{
        if(event.getId().equals(SaraCons.MSG_NLG)) {
            response = (BSON) event.getElement();
            Log4J.debug(this, "sending out to client: " + Utils.toJson(response));
            sendResponse(new SessionMessage(SaraCons.MSG_NLG, Utils.toJson(response)));
            Log4J.error(this, "TIME FOR PROCESSING WHOLE PIPELINE: " + (System.currentTimeMillis() - time));
            resetCrono = true;
            //sendResponse(new SessionMessage("test", "test"));
        } else if(event.getId().equals(SaraCons.MSG_START_STREAMING)) {
            r5StreamListener = (R5StreamListener) event.getElement();
            Log4J.info(this, "Message from MUF to Start Streaming " + r5StreamListener.toString());
            sendResponse((new SessionMessage(SaraCons.MSG_START_STREAMING, Utils.toJson(r5StreamListener))));
            Log4J.error(this, "TIME FOR PROCESSING WHOLE PIPELINE: " + (System.currentTimeMillis() - time));
            resetCrono = true;
        } else {
            Log4J.info(this, "unexpected mMssage from MUF to Client: " + event.getElement());
        }

    }

    @Override
    public void close() throws Exception{
        try {
            super.close();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
