package edu.cmu.inmind.multiuser.sara.component;

import edu.cmu.inmind.multiuser.common.Constants;
import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.Utils;
import edu.cmu.inmind.multiuser.common.model.*;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.communication.*;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StateType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by oscarr on 3/7/17.
 */
@StateType( state = Constants.STATEFULL)
@BlackboardSubscription( messages = {SaraCons.MSG_ASR, SaraCons.MSG_START_DM, SaraCons.MSG_UM} )
@ConnectRemoteService(remoteService = SaraCons.NLU_DM_SERVICE)
public class NLU_DMComponent extends PluggableComponent {
    private static final String SESSION_MANAGER_SERVICE = "session-manager";

    private static Map<String,NLU_DMComponent> selfMap;
    static {
        selfMap = new HashMap<>();
    }

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
        Log4J.info(this, "postCreate is called, sessionID is " + getSessionId() + " in object " + this.hashCode());
        String[] msgSubscriptions = { SaraCons.MSG_ASR };
        ZMsgWrapper msgWrapper = new ZMsgWrapper();
        /*commController = new ClientCommController(pythonDialogueAddress, getSessionId(),
                Utils.getProperty("dialogAddress"),
                Constants.REQUEST_CONNECT, msgWrapper, msgSubscriptions);*/
        selfMap.put(getSessionId(), this);
    }

    private SaraOutput extractAndProcess() {
        SaraInput saraInput = (SaraInput) blackboard().get(SaraCons.MSG_ASR);
        SaraOutput saraOutput = new SaraOutput();
        //
        saraOutput.setUserIntent( new UserIntent( "user-intent", new ArrayList<>() ) );
        Log4J.info(this, "Input: " + saraInput + ", Output: " + saraOutput + "\n");
        return saraOutput;
    }

    static void send(String sessionID, Object message, boolean shouldProcessReply) {
        NLU_DMComponent.getCCC(sessionID).send(new SessionMessage(SaraCons.MSG_ASR, Utils.toJson(message)));
    }


    @Override
    public void onEvent(BlackboardEvent blackboardEvent) throws Exception {
        // print full event
        Log4J.debug(this, "received " + blackboardEvent.toString());
        // store user's latest utterance
        final String utterance = blackboardEvent.toString().contains("confidence:") ? blackboardEvent.toString().split("Utterance: ")[1].split(" confidence:")[0] : "";
        // let's forward the ASR message to DialoguePython:
        /** should be set to true if we expect a response from python */
        boolean receiveRequest = false;
        if (blackboardEvent.getId().equals(SaraCons.MSG_START_DM)){
            ASROutput startDMMessage = new ASROutput(SaraCons.MSG_START_DM, 1.0);
            Log4J.debug(this, "about to send initial greeting ...");
    	    send(getSessionId(), startDMMessage, true);
            receiveRequest = true;
            Log4J.debug(this, "Sent Initial Greeting");
        } else if (blackboardEvent.getId().equals(SaraCons.MSG_UM)) {
            final UserModel userModel = (UserModel) blackboardEvent.getElement();
            Log4J.info(this, "Received user model");
            if (userModel.getUserFrame() != null) {
                Log4J.info(this, "Sending user frame to python: " + Utils.toJson(userModel.getUserFrame()));
                // no reply for user model stuff
                send(getSessionId(), userModel.getUserFrame(), false);
            } else {
                Log4J.info(this, "User frame was empty");
            }
        } else {
            Log4J.debug(this, "sending on " + blackboardEvent.toString() );
            send(getSessionId(), blackboardEvent.getElement(), true);
            receiveRequest = true;
        }
        // here we receive the response from DialoguePython:
        if (receiveRequest) {
            final int receiveRequestNumber = ++receiveCounter;
            Log4J.debug(this, "receive request " + receiveRequestNumber);
            NLU_DMComponent.getCCC(getSessionId()).receive(message -> {
                Log4J.debug(NLU_DMComponent.this, "I've received for request: " + receiveRequestNumber);
                Log4J.debug(NLU_DMComponent.this, "I've received: " + message);
                // store user's utterance (for NLG)
                ActiveDMOutput dmOutput = Utils.fromJson(message, ActiveDMOutput.class);
                dmOutput.setUtterance(utterance);
                if (dmOutput.plainGetRecommendation() != null)
                    dmOutput.getRecommendation().setRexplanations(null);
                dmOutput.sessionID = getSessionId();
                // post to Blackboard
                blackboard().post(NLU_DMComponent.this, SaraCons.MSG_DM, dmOutput);
            });
        }
    }

    public static NLU_DMComponent getCCC(String sessionID) {
        return selfMap.get(sessionID);
    }

    static int receiveCounter = 0;

    /** the smart kind of DMOutput that is able to load recommendations JIT */
    public static class ActiveDMOutput extends DMOutput {

        String sessionID;

        /** Fills in underspecified Recommmendation variable if necessary. */
        private void fillInRecommendationTitle() {
            // query for value
            // commController.send(sessionID, new ASROutput(SaraCons.MSG_QUERY, 1.0));
            NLU_DMComponent.send(sessionID, new ASROutput(SaraCons.MSG_QUERY, 1.0), true);
            final int receiveRequestNumber = ++receiveCounter;
            Log4J.debug(ActiveDMOutput.this, "receive request " + receiveRequestNumber);
            Log4J.info(ActiveDMOutput.this, "sent recommendation title request");
            NLU_DMComponent.getCCC(sessionID).receive(message -> {
                Log4J.debug(this, "I've received for request: " + receiveRequestNumber);
                // set recommendation to newly found value
                recommendation.setRexplanations(Utils.fromJson(message, DMOutput.class).getRecommendation().getRexplanations());
                Log4J.info(ActiveDMOutput.this, "received recommendation specification: " + message);
            });
            // wait for DialoguePython to send the value
            while (!recommendation.hasContent()) {
                try {
                    Log4J.debug(ActiveDMOutput.this, "waiting for recommendation specification");
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Log4J.warn(ActiveDMOutput.this, e.toString());
                }
            }
        }

        @Override public synchronized Recommendation getRecommendation() {
            if (recommendation != null && !recommendation.hasContent()) {
                // System.err.println(recommendation.toString());
                fillInRecommendationTitle();
                // System.err.println(recommendation.toString());
            }
            // Recommendation object now contains value
            return recommendation;
        }

        private Recommendation plainGetRecommendation() {
            return super.getRecommendation();
        }

    }

}
