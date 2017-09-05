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
import edu.cmu.inmind.multiuser.controller.plugin.StateType;

import java.util.ArrayList;

/**
 * Created by oscarr on 3/7/17.
 */
@StateType( state = Constants.STATEFULL)
@BlackboardSubscription( messages = {SaraCons.MSG_ASR, SaraCons.MSG_START_DM, SaraCons.MSG_UM} )
public class NLU_DMComponent extends PluggableComponent {
    private static final String SESSION_MANAGER_SERVICE = "session-manager";
    private ClientCommController commController;
    private final String pythonDialogueAddress = Utils.getProperty("pythonDialogueAddress");

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
        Log4J.info(this, "NLU_DMComponent: " + hashCode());

        SaraOutput saraOutput = extractAndProcess();

        //update the blackboard
        blackboard().post(this, SaraCons.MSG_NLU, saraOutput );
    }

    public void postCreate(){
        Log4J.info(this, "postCreate is called, sessionID is " + getSessionId());
        String[] msgSubscriptions = { SaraCons.MSG_ASR };
        ZMsgWrapper msgWrapper = new ZMsgWrapper();
        /*commController = new ClientCommController(pythonDialogueAddress, getSessionId(),
                Utils.getProperty("dialogAddress"),
                Constants.REQUEST_CONNECT, msgWrapper, msgSubscriptions);*/
        commController =  new ClientCommController.Builder()
                .setServerAddress( pythonDialogueAddress)
                .setServiceName(getSessionId())
                .setClientAddress( Utils.getProperty("dialogAddress") )
                .setRequestType( Constants.REQUEST_CONNECT )
                .setTCPon( true )
                .setMuf( true? null : null ) //when TCP is off, we need to explicitly tell the client who the MUF is
                .setSessionManagerService(SESSION_MANAGER_SERVICE)
                .build();
    }

    private SaraOutput extractAndProcess() {
        SaraInput saraInput = (SaraInput) blackboard().get(SaraCons.MSG_ASR);
        SaraOutput saraOutput = new SaraOutput();
        //
        saraOutput.setUserIntent( new UserIntent( "user-intent", new ArrayList<>() ) );
        Log4J.info(this, "Input: " + saraInput + ", Output: " + saraOutput + "\n");
        return saraOutput;
    }

    @Override
    public void onEvent(BlackboardEvent blackboardEvent) throws Exception {
        // print full event
        Log4J.debug(this, "received " + blackboardEvent.toString());
        // store user's latest utterance
        String tryUtterance;
        try { tryUtterance = blackboardEvent.toString().split("Utterance: ")[1].split(" confidence:")[0]; }
        catch (ArrayIndexOutOfBoundsException aiobe) {
            System.err.println(blackboardEvent.toString());
            aiobe.printStackTrace();
            tryUtterance = "";
            Log4J.warn(this, aiobe.toString());
        }
        final String utterance = tryUtterance;
        // let's forward the ASR message to DialoguePython:
        if (blackboardEvent.getId().equals(SaraCons.MSG_START_DM)){
            ASROutput startDMMessage = new ASROutput(SaraCons.MSG_START_DM, 1.0);
            Log4J.debug(this, "about to send initial greeting ...");
    	    commController.send( getSessionId(), startDMMessage );
           // commController.send( getSessionId(), startDMMessage );
            Log4J.debug(this, "Sent Initial Greeting");
        } else if (blackboardEvent.getId().equals(SaraCons.MSG_UM)) {
            final UserModel userModel = (UserModel) blackboardEvent.getElement();
            Log4J.info(this, "Received user model");
            if (userModel.getUserFrame() != null) {
                Log4J.info(this, "Sending user frame to python: " + Utils.toJson(userModel.getUserFrame()));
                commController.send(getSessionId(), userModel.getUserFrame());
            } else {
                Log4J.info(this, "User frame was empty");
            }
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
                // store user's utterance (for NLG)
                SmartDMOutput dmOutput = Utils.fromJson(message, SmartDMOutput.class);
                dmOutput.setUtterance(utterance);
                // post to Blackboard
                blackboard().post(NLU_DMComponent.this, SaraCons.MSG_DM, dmOutput);
            }
        });
    }

    @Override
    public void shutDown(){
        sendCloseMessage();
        super.shutDown();
    }
    /**
     * We need to send this message to the Python Dialogue System on a separate thread, otherwise
     * we will get some TimeOut exceptions because the communication process takes longer than the
     * shuttingdown process
     */
    private void sendCloseMessage(){
        new Thread("send-message-close-python-dialogue") {
            public void run(){
                SessionMessage sessionMessage = new SessionMessage();
                sessionMessage.setRequestType( Constants.REQUEST_DISCONNECT );
                sessionMessage.setSessionId(getSessionId());
                commController.send( SESSION_MANAGER_SERVICE, sessionMessage );
                commController.close();
            }
        }.start();
    }

    /** the smart kind of DMOutput that is able to load recommendations JIT */
    public class SmartDMOutput extends DMOutput {
        /** Fills in underspecified Recommmendation variable if necessary. */
        public void fillInRecommendationTitle() {
            // query for value
            Log4J.info(SmartDMOutput.this, "sending recommendation title request");
            commController.send(getSessionID(), new ASROutput(SaraCons.MSG_QUERY, 1.0));
            Log4J.info(SmartDMOutput.this, "sent recommendation title request");
            commController.receive(new ResponseListener() {
                @Override
                public void process(String message) {
                    // set recommendation to newly found value
                    System.err.println("XXXX: " + message);
                    recommendation.setRexplanations(Utils.fromJson(message, DMOutput.class).getRecommendation().getRexplanations());
                    System.err.println("done: " + message);
                }
            });
            // wait for DialoguePython to send the value
            while (!recommendation.hasContent()) {
                try {
                    System.out.println("VIVIAN: waiting for specified rec");
                    Thread.sleep(50);
                } catch (Exception e) {
                    System.out.println(e);
                    System.err.println(e);
                }
            }
        }

        @Override public String getRecommendationTitle() {
            Log4J.info(NLU_DMComponent.this, "attempting to get recommendation title");
            if (!recommendation.hasContent()) {
                Log4J.info(NLU_DMComponent.this, "attempting to query for recommendation title");
                System.err.println(recommendation.toString());
                fillInRecommendationTitle();
                System.err.println(recommendation.toString());
            }
            // Recommendation object now contains value
            return recommendation.getTitle();
        }
    }

}
