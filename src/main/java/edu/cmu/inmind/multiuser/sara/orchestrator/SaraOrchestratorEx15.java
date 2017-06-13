package edu.cmu.inmind.multiuser.sara.orchestrator;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.Utils;
import edu.cmu.inmind.multiuser.common.model.ASROutput;
import edu.cmu.inmind.multiuser.common.model.R5StreamListener;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.communication.SessionMessage;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.orchestrator.ProcessOrchestratorImpl;
import edu.cmu.inmind.multiuser.controller.session.Session;
import edu.cmu.inmind.multiuser.sara.component.beat.bson.BSON;
import beat.bson.BSON;

/**
 * Created by oscarr on 3/3/17.
 */
@BlackboardSubscription( messages = {SaraCons.MSG_NLG, SaraCons.MSG_START_STREAMING, SaraCons.R5STREAM_DISCONNECTED,
SaraCons.R5STREAM_CLOSE, SaraCons.R5STREAM_TIMEOUT, SaraCons.R5STREAM_ERROR, SaraCons.R5STREAM_STARTED})
public class SaraOrchestratorEx15 extends ProcessOrchestratorImpl {
    private BSON response = new BSON();
    private R5StreamListener r5StreamListener= new R5StreamListener();

    @Override
    public void initialize(Session session) throws Exception{
        super.initialize( session );

    }

    @Override
    public void process(String message) {
        Log4J.debug(this, "orchestrator received message " + message);
        super.process(message);

        SessionMessage inputMessage = Utils.fromJson(message, SessionMessage.class);
        if( inputMessage.getMessageId().equals("MSG_START_SESSION") ){
            blackboard.post( this, inputMessage.getMessageId(), inputMessage.getPayload() );
        }
        else if( inputMessage.getMessageId().equals(SaraCons.R5STREAM_STARTED) ||
                inputMessage.getMessageId().equals(SaraCons.R5STREAM_DISCONNECTED) ||
                inputMessage.getMessageId().equals(SaraCons.R5STREAM_CLOSE) ||
                inputMessage.getMessageId().equals(SaraCons.R5STREAM_TIMEOUT) ||
                inputMessage.getMessageId().equals(SaraCons.R5STREAM_ERROR))
        {
            blackboard.post( this, inputMessage.getMessageId(), inputMessage.getPayload() );
        }
        else{
            ASROutput asrOutput = Utils.fromJson(inputMessage.getPayload(), ASROutput.class);
            blackboard.post( this, inputMessage.getMessageId(), asrOutput);
            if( inputMessage.getMessageId().equals(SaraCons.MSG_ASR) ){
              Log4J.info( this, "ASR - > utterance: " + asrOutput.getUtterance() + "  confidence: "
                        + asrOutput.getConfidence());
            }
        }
    }

    @Override
    /**
     * This method will be called when the system has a response to send out, that is (in our example).
     */
    public void onEvent(BlackboardEvent event){
        if(event.getId().equals(SaraCons.MSG_NLG)) {
            response = (BSON) blackboard.get(SaraCons.MSG_NLG);
            Log4J.debug(this, "sending out to client: " + response.toString());
            sendResponse(new SessionMessage(SaraCons.MSG_NLG, Utils.toJson(response)));
        }
        else if(event.getId().equals(SaraCons.MSG_START_STREAMING))
        {
            r5StreamListener = (R5StreamListener) blackboard.get(SaraCons.MSG_START_STREAMING);
            Log4J.info(this, "Message from MUF to Start Streaming " + r5StreamListener.toString());
            sendResponse((new SessionMessage(SaraCons.MSG_START_STREAMING, Utils.toJson(r5StreamListener))));
        }

    }


    @Override
    public void start() {
        super.start();
        //TODO: add some logic when session is started (e.g., startUp resources)
    }

    @Override
    public void pause() {
        super.pause();
        //TODO: add some logic when session is paused (e.g., stop temporarily execute execution)
    }

    @Override
    public void resume() {
        super.resume();
        //TODO: add some logic when session is resumed (e.g., resume execute execution)
    }

    @Override
    public void close() throws Exception{
        super.close();
        //TODO: add some logic when session is closed (e.g., release resources)
    }
}
