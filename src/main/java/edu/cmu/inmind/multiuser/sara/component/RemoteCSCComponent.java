package edu.cmu.inmind.multiuser.sara.component;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.model.*;
import edu.cmu.inmind.multiuser.controller.blackboard.Blackboard;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.common.Constants;
import edu.cmu.inmind.multiuser.controller.common.Utils;
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
@BlackboardSubscription( messages = {SaraCons.MSG_ASR, SaraCons.MSG_ASR_CSC_RESPONSE} )
public class RemoteCSCComponent extends PluggableComponent {

    protected static Blackboard blackboard;

    private static Map<String, RemoteCSCComponent> components;
    static {
        components = new HashMap<>();
    }
    static void sendToBBforSession(String sessionID, String msgId, ActiveDMOutput dmoutput) {
        RemoteCSCComponent csc = components.get(sessionID);
        RemoteCSCComponent.blackboard.post(csc, msgId, "query");
    }

    @Override
    public void startUp(){
        super.startUp();
        Log4J.info(this, "CSCComponent: startup has finished.");
    }

    @Override
    public void postCreate() {
        components.put(getSessionId(), this);
        RemoteCSCComponent.blackboard = getBlackBoard(getSessionId());

    }

    @Override
    public void execute() {
//        Log4J.info(this, "CSCComponent: " + hashCode());
//        SaraOutput saraOutput = null;
//        try {
//            saraOutput = extractAndProcess();
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }
//        //update the blackboard
//        RemoteCSCComponent.blackboard.post(this, SaraCons.MSG_NLU, saraOutput );
    }

//    private SaraOutput extractAndProcess() {
//        SaraInput saraInput = null;
//        try {
//            saraInput = (SaraInput) RemoteCSCComponent.blackboard.get(SaraCons.MSG_ASR);
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }
//        SaraOutput saraOutput = new SaraOutput();
//        saraOutput.setUserIntent( new UserIntent( "user-intent", new ArrayList<>() ) );
//        Log4J.info(this, "Input: " + saraInput + ", Output: " + saraOutput + "\n");
//        return saraOutput;
//    }

    @Override
    public void onEvent(Blackboard blackboard, BlackboardEvent blackboardEvent) throws Throwable {
        // print full event
        Log4J.debug(this, "received " + blackboardEvent.toString());
        if(blackboard!=null) {
            //Log4J.info(this, "blackboard is not null");
            RemoteCSCComponent.blackboard = blackboard;
        }
        // store user's latest utterance
        // let's forward the ASR message to DialoguePython:
        /** should be set to true if we expect a response from python */
        if (blackboardEvent.getId().equals(SaraCons.MSG_ASR_CSC_RESPONSE)) {
                new Thread(() ->
                        processCSCResult(blackboardEvent.getElement().toString()),
                        "CSC process thread").start();
        } else if (blackboardEvent.getId().equals(SaraCons.MSG_ASR)) {
            Log4J.debug(this, "sending on " + blackboardEvent.toString());
            RemoteCSCComponent.blackboard.post(this, SaraCons.MSG_ASR_CSC, blackboardEvent.getElement());

        } else {
            Log4J.error(this, "got a message I could not understand: " + blackboardEvent.toString());
        }
    }

    private void processCSCResult(String message) {
        Log4J.debug(RemoteCSCComponent.this, "I've received python response: " + message);
        // store user's utterance (for NLG)
        CSCOutput cscOutput = Utils.fromJson(message, CSCOutput.class);
        // post to Blackboard
        RemoteCSCComponent.blackboard.post(RemoteCSCComponent.this, SaraCons.MSG_CSC, cscOutput);
    }

    /** the smart kind of DMOutput that is able to load recommendations JIT */
    public static class ActiveDMOutput extends DMOutput {

        String sessionID;

        /** Fills in underspecified Recommmendation variable if necessary. */
        private void fillInRecommendationTitle() {
            // query for value
            sendToBBforSession(sessionID, SaraCons.MSG_QUERY, ActiveDMOutput.this);
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
