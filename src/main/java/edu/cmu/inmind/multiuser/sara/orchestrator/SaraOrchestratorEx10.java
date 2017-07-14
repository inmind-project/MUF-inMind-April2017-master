package edu.cmu.inmind.multiuser.sara.orchestrator;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.Utils;
import edu.cmu.inmind.multiuser.common.model.SaraInput;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.communication.SessionMessage;
import edu.cmu.inmind.multiuser.controller.orchestrator.ProcessOrchestratorImpl;
import edu.cmu.inmind.multiuser.controller.session.Session;

/**
 * Created by oscarr on 3/3/17.
 */
@BlackboardSubscription( messages = {SaraCons.MSG_NLU})
public class SaraOrchestratorEx10 extends ProcessOrchestratorImpl {

    @Override
    public void initialize(Session session) throws Exception{
        try {
            super.initialize( session );
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void process(String message) {
        super.process(message);

        SessionMessage inputMessage = Utils.fromJson(message, SessionMessage.class);
        blackboard.setKeepModel( false );

        // this post won't keep any object in the Blackboard, it just notifies subscribers.
        blackboard.post( this, inputMessage.getMessageId(), Utils.fromJson(inputMessage.getPayload(),
                SaraInput.class));
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
        try {
            super.close();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        //TODO: add some logic when session is closed (e.g., release resources)
    }
}
