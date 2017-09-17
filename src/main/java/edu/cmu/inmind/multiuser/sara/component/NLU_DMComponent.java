package edu.cmu.inmind.multiuser.sara.component;

import edu.cmu.inmind.multiuser.common.Constants;
import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.Utils;
import edu.cmu.inmind.multiuser.common.model.*;
import edu.cmu.inmind.multiuser.controller.blackboard.Blackboard;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
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
@BlackboardSubscription( messages = {SaraCons.MSG_ASR, SaraCons.MSG_START_DM, SaraCons.MSG_UM,
        SaraCons.MSG_RESPONSE_START_PYTHON, SaraCons.MSG_QUERY_RESPONSE, SaraCons.MSG_ASR_DM_RESPONSE} )
public class NLU_DMComponent extends PluggableComponent {
    private ActiveDMOutput dmOutput;
    private int receiveRequestNumber;

    private Blackboard blackboard;

    private static Map<String, NLU_DMComponent> components;
    static {
        components = new HashMap<>();
    }
    static void sendToBBforSession(String sessionID, String msgId, ActiveDMOutput dmoutput) {
        NLU_DMComponent nluc = components.get(sessionID);
        nluc.dmOutput = dmoutput;
        Log4J.debug(nluc, "the local activedmoutput is " + nluc.dmOutput.hashCode());
            nluc.blackboard.post(nluc, msgId, "");
    }

    @Override
    public void startUp(){
        super.startUp();
        Log4J.info(this, "NLU_DMComponent: startup has finished.");
    }

    @Override
    public void postCreate() {
        components.put(getSessionId(), this);
        this.blackboard = blackboard();
    }

    @Override
    public void execute() {
        Log4J.info(this, "NLU_DMComponent: " + hashCode());
        SaraOutput saraOutput = extractAndProcess();
        //update the blackboard
        blackboard().post(this, SaraCons.MSG_NLU, saraOutput );
    }

    private SaraOutput extractAndProcess() {
        SaraInput saraInput = (SaraInput) blackboard().get(SaraCons.MSG_ASR);
        SaraOutput saraOutput = new SaraOutput();
        saraOutput.setUserIntent( new UserIntent( "user-intent", new ArrayList<>() ) );
        Log4J.info(this, "Input: " + saraInput + ", Output: " + saraOutput + "\n");
        return saraOutput;
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
        if (blackboardEvent.getId().equals(SaraCons.MSG_START_DM)) {
            receiveRequest = processStartDM();
        } else if (blackboardEvent.getId().equals(SaraCons.MSG_UM)) {
	        processUserModel(blackboardEvent.getElement());
        } else if (blackboardEvent.getId().equals(SaraCons.MSG_QUERY_RESPONSE)) {
            processQueryResponse(blackboardEvent.getElement().toString());
        } else if (blackboardEvent.getId().equals(SaraCons.MSG_ASR_DM_RESPONSE)) {
            processPythonResponse(blackboardEvent.getElement().toString(), utterance, receiveRequestNumber);
        } else if (blackboardEvent.getId().equals(SaraCons.MSG_ASR)) {
            Log4J.debug(this, "sending on " + blackboardEvent.toString());
            blackboard().post(this, SaraCons.MSG_ASR_DM, blackboardEvent.getElement());
            receiveRequest = true;
        } else {
            Log4J.error(this, "got a message I could not understand: " + blackboardEvent.toString());
        }
        // here we receive the response from DialoguePython:
        if (receiveRequest) {
            receiveRequestNumber = ++receiveCounter;
            Log4J.debug(this, "receive request " + receiveRequestNumber);
        }
    }

    private boolean processStartDM() {
        Log4J.debug(this, "about to send initial greeting ...");
        blackboard().post(this, SaraCons.MSG_START_DM_PYTHON, null);
        Log4J.debug(this, "Sent Initial Greeting");
        return true;
    }

    private void processUserModel(Object element) {
        final UserModel userModel = (UserModel) element;
        Log4J.info(this, "Received user model");
        if (userModel.getUserFrame() != null) {
            Log4J.info(this, "Sending user frame to python: " + Utils.toJson(userModel.getUserFrame()));
            // no reply for user model stuff
            blackboard().post(this, SaraCons.MSG_USER_FRAME, userModel.getUserFrame());
        } else {
            Log4J.info(this, "User frame was empty");
        }
    }

    private void processPythonResponse(String message, String utterance, int receiveRequestNumber) {
        Log4J.debug(NLU_DMComponent.this, "I've received for request: " + receiveRequestNumber);
        Log4J.debug(NLU_DMComponent.this, "I've received: " + message);
        // store user's utterance (for NLG)
        dmOutput = Utils.fromJson(message, ActiveDMOutput.class);
        dmOutput.setUtterance(utterance);
        /* uncomment the next two lines for incrementality: */
        //if (dmOutput.plainGetRecommendation() != null)
        //    dmOutput.getRecommendation().setRexplanations(null);
        dmOutput.sessionID = getSessionId();
        // post to Blackboard
        blackboard().post(NLU_DMComponent.this, SaraCons.MSG_DM, dmOutput);
    }

    private void processQueryResponse(String message) {
        Log4J.debug(this, "I've received for request: " + receiveRequestNumber);
        // set recommendation to newly found value
        dmOutput.getRecommendation().setRexplanations(Utils.fromJson(message, DMOutput.class).getRecommendation().getRexplanations());
        Log4J.info(this, "received recommendation specification: " + message);
    }

    static int receiveCounter = 0;

    /** the smart kind of DMOutput that is able to load recommendations JIT */
    public static class ActiveDMOutput extends DMOutput {

        String sessionID;

        /** Fills in underspecified Recommmendation variable if necessary. */
        private void fillInRecommendationTitle() {
            // query for value
            sendToBBforSession(sessionID, SaraCons.MSG_QUERY, ActiveDMOutput.this);
            final int receiveRequestNumber = ++receiveCounter;
            Log4J.debug(ActiveDMOutput.this, "receive request " + receiveRequestNumber);
            Log4J.info(ActiveDMOutput.this, "sent recommendation title request in object " + this.hashCode());

            // wait for DialoguePython to send the value
            while (!hasFullContent()) {
                try {
                    Log4J.debug(ActiveDMOutput.this, "waiting for recommendation specification");
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Log4J.warn(ActiveDMOutput.this, e.toString());
                }
            }
        }

        @Override public synchronized Recommendation getRecommendation() {
            
            if (!hasFullContent()) {
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
