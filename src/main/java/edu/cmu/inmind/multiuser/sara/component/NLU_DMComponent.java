package edu.cmu.inmind.multiuser.sara.component;

import edu.cmu.inmind.multiuser.common.Constants;
import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.Utils;
import edu.cmu.inmind.multiuser.common.model.DMOutput;
import edu.cmu.inmind.multiuser.common.model.SaraInput;
import edu.cmu.inmind.multiuser.common.model.SaraOutput;
import edu.cmu.inmind.multiuser.common.model.UserIntent;
import edu.cmu.inmind.multiuser.common.model.ASROutput;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.communication.ClientCommController;
import edu.cmu.inmind.multiuser.controller.communication.ResponseListener;
import edu.cmu.inmind.multiuser.controller.communication.SessionMessage;
import edu.cmu.inmind.multiuser.controller.communication.ZMsgWrapper;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StatefulComponent;

import java.util.ArrayList;

/**
 * Created by oscarr on 3/7/17.
 */
@StatefulComponent
@BlackboardSubscription( messages = {SaraCons.MSG_ASR, "MSG_START_SESSION"} )
public class NLU_DMComponent extends PluggableComponent {
    private ClientCommController commController;
    private final String pythonDialogueAddress = Utils.getProperty("pythonDialogueAddress");

    @Override
    public void startUp(){
        super.startUp();
//        postCreate();
//        postCreateblackboard().post(this, SaraCons.MSG_ASR, "Hello");
        //blackboard().post(this, SaraCons.MSG_ASR, "Hello");
        Log4J.info(this, "NLU_DMComponent: startup has finished.");
    }

    @Override
    public void execute() {
        Log4J.info(this, "NLU_DMComponent: " + hashCode());

        SaraOutput saraOutput = extractAndProcess();

        //update the blackboard
        blackboard().post(this, SaraCons.MSG_NLU, saraOutput );
    }

    public void postCreate(){
        Log4J.info(this, "postCreate is called, sessionID is " + getSessionId());
        String[] msgSubscriptions = { SaraCons.MSG_ASR };
        ZMsgWrapper msgWrapper = new ZMsgWrapper();
        commController = new ClientCommController(pythonDialogueAddress, getSessionId(),
                Utils.getProperty("dialogueAddress"), Constants.REQUEST_CONNECT, msgWrapper, msgSubscriptions);
    }

    private SaraOutput extractAndProcess() {
        SaraInput saraInput = (SaraInput) blackboard().get(SaraCons.MSG_ASR);
        SaraOutput saraOutput = new SaraOutput();
        //
        saraOutput.setUserIntent( new UserIntent( "user-intent", new ArrayList<>() ) );
        Log4J.info(this, "Input: " + saraInput + ", Output: " + saraOutput + "\n");
        return saraOutput;
    }

    public void onEvent(BlackboardEvent blackboardEvent) {
        Log4J.debug(this, "received " + blackboardEvent.toString());
        // let's forward the ASR message to DialoguePython:
        if (blackboardEvent.getId().equals("MSG_START_SESSION")){
	    ASROutput initialGreeting = new ASROutput("Hello", 1.0);
	    commController.send( getSessionId(), initialGreeting );
            commController.send( getSessionId(), initialGreeting );
	    Log4J.debug(this, "Sending Initial Greeting");
        } else {
            Log4J.debug(this, "sending on " + blackboardEvent.toString() );
            commController.send( getSessionId(), blackboardEvent.getElement() );
//	    Log4J.debug(this, "now something was sent");
        }
        // here we receive the response from DialoguePython:
        commController.receive(new ResponseListener() {
            @Override
            public void process(String message) {
		Log4J.debug(NLU_DMComponent.this, "I've received: " + message);
                blackboard().post( NLU_DMComponent.this, SaraCons.MSG_DM,
                        Utils.fromJson( message, DMOutput.class ));
            }
        });
    }

    @Override
    public void shutDown(){
        SessionMessage sessionMessage = new SessionMessage();
        sessionMessage.setRequestType( Constants.REQUEST_DISCONNECT );
        commController.send( getSessionId(), sessionMessage );
        commController.close();
        super.shutDown();
    }

}
