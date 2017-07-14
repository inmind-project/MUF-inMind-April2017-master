package edu.cmu.inmind.multiuser.sara.component;

import edu.cmu.inmind.multiuser.common.Constants;
import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.Utils;
import edu.cmu.inmind.multiuser.common.model.*;
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
@BlackboardSubscription( messages = {SaraCons.MSG_ASR, SaraCons.MSG_NVB} )
public class RemoteCSCComponent extends PluggableComponent {
    private ClientCommController commController;
    private final String pythonAddress = Utils.getProperty("pythonDialogueAddress");

    @Override
    public void startUp(){
        super.startUp();
        postCreate();
        //postCreateblackboard().post(this, SaraCons.MSG_ASR, "Hello");
        //blackboard().post(this, SaraCons.MSG_ASR, "Hello");
        Log4J.info(this, "NLU_DMComponent: startup has finished.");
    }

    @Override
    public void execute() {
        Log4J.info(this, "RemoteCSCComponent: " + hashCode());

        //CSCOutput cscOutput = extractAndProcess();

        //update the blackboard
        //blackboard().post(this, SaraCons.MSG_CSC, cscOutput );
    }

    public void postCreate(){
        Log4J.info(this, "postCreate is called, sessionID is " + getSessionId());
        String[] msgSubscriptions = { SaraCons.MSG_ASR, SaraCons.MSG_NVB };
        ZMsgWrapper msgWrapper = new ZMsgWrapper();
        commController = new ClientCommController(pythonAddress, getSessionId(),
                Utils.getProperty("dialogAddress"),
                Constants.REQUEST_CONNECT, msgWrapper, msgSubscriptions);
    }

//    private CSCOutput cscOutput = extractAndProcess();
//    extractAndProcess() {
//        SaraInput saraInput = (SaraInput) blackboard().get(SaraCons.MSG_ASR);
//        CSCOutput cscOutput = new CSCOutput();
//        //
//        saraOutput.setUserIntent( new UserIntent( "user-intent", new ArrayList<>() ) );
//        Log4J.info(this, "Input: " + saraInput + ", Output: " + saraOutput + "\n");
//        return cscOutput;
//    }

    @Override
    public void onEvent(BlackboardEvent blackboardEvent) {
        Log4J.debug(this, "received " + blackboardEvent.toString());
        // let's forward the ASR message to DialoguePython:
        if (blackboardEvent.getId().equals(SaraCons.MSG_START_DM)){
            ASROutput startDMMessage = new ASROutput(SaraCons.MSG_START_DM, 1.0);
            Log4J.debug(this, "about to send initial greeting ...");
    	    commController.send( getSessionId(), startDMMessage );
            commController.send( getSessionId(), startDMMessage );
            Log4J.debug(this, "Sent Initial Greeting");
        } else {
            Log4J.debug(this, "sending on " + blackboardEvent.toString() );
            commController.send( getSessionId(), blackboardEvent.getElement() );
//	    Log4J.debug(this, "now something was sent");
        }
        // here we receive the response from DialoguePython:
        commController.receive(new ResponseListener() {
            @Override
            public void process(String message) {
		Log4J.debug(RemoteCSCComponent.this, "I've received: " + message);
                blackboard().post( RemoteCSCComponent.this, SaraCons.MSG_DM,
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
