package edu.cmu.inmind.multiuser.sara.orchestrator;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.Utils;
import edu.cmu.inmind.multiuser.common.model.ASROutput;
import edu.cmu.inmind.multiuser.common.model.SaraInput;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.communication.SessionMessage;
import edu.cmu.inmind.multiuser.controller.orchestrator.ProcessOrchestratorImpl;
import edu.cmu.inmind.multiuser.controller.session.Session;
import edu.cmu.inmind.multiuser.sara.component.groove.bson.BSON;

/**
 * Created by oscarr on 3/3/17.
 */
@BlackboardSubscription( messages = {SaraCons.MSG_NLG})
public class SaraOrchestratorEx15 extends ProcessOrchestratorImpl {
    private BSON response = new BSON();

    @Override
    public void initialize(Session session) throws Exception{
        super.initialize( session );

    }

    @Override
    public void process(String message) {
        super.process(message);

        SessionMessage inputMessage = Utils.fromJson(message, SessionMessage.class);
        blackboard.post( this, inputMessage.getMessageId(), Utils.fromJson(inputMessage.getPayload(),
                ASROutput.class));
    }

    @Override
    /**
     * This method will be called when the system has a response to send out, that is (in our example).
     */
    public void onEvent(BlackboardEvent event){
        response = (BSON) blackboard.get(SaraCons.MSG_NLG);
        sendResponse( new SessionMessage(SaraCons.MSG_NLG, Utils.toJson(response) ) );
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
