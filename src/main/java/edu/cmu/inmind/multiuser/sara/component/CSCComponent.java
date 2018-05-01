package edu.cmu.inmind.multiuser.sara.component;


import static edu.cmu.lti.rapport.pipline.csc.ConversationalStrategy.ASN;
import static edu.cmu.lti.rapport.pipline.csc.ConversationalStrategy.Praise;
import static edu.cmu.lti.rapport.pipline.csc.ConversationalStrategy.QESD;
import static edu.cmu.lti.rapport.pipline.csc.ConversationalStrategy.SD;
import static edu.cmu.lti.rapport.pipline.csc.ConversationalStrategy.SE;
import static edu.cmu.lti.rapport.pipline.csc.ConversationalStrategy.VSN;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.model.ASROutput;
import edu.cmu.inmind.multiuser.common.model.CSCOutput;
import edu.cmu.inmind.multiuser.common.model.Strategy;
import edu.cmu.inmind.multiuser.controller.blackboard.Blackboard;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.common.Constants;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StateType;
import edu.cmu.lti.rapport.pipline.csc.ConversationalStrategy;
import edu.cmu.lti.rapport.pipline.csc.ConversationalStrategyDistribution;
import edu.cmu.lti.rapport.pipline.csc.MultiClassifier;

/**
 * Created by fpecune on 5/22/2017.
 */

@StateType(state = Constants.STATELESS)
@BlackboardSubscription(messages = {SaraCons.MSG_ASR})
public class CSCComponent extends PluggableComponent {

    MultiClassifier csc;

    @Override
    public void startUp() {
        super.startUp();
        try {
            csc = new MultiClassifier();
            Log4J.info(this, "Classifier started");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void execute() {
        Log4J.info(this, "CSCComponent: " + hashCode());

    }

    public void postCreate() {
        String[] msgSubscriptions = {"MSG_ASR"};

    }

    public void onEvent(Blackboard blackboard, BlackboardEvent blackboardEvent) throws Throwable {


        Log4J.info(this, "CSCComponent: starting classifying.");

        Object input = blackboard.get("MSG_ASR");

        if (input instanceof ASROutput) {
            csc.setNewASRResult(((ASROutput) input).getUtterance());
        } else {
            throw new IllegalArgumentException("I only eat ASROutput");
        }

        Log4J.info(this, "CSCComponent: Utterance set." + ((ASROutput) input).getUtterance());


        ConversationalStrategyDistribution strategyList = new ConversationalStrategyDistribution();
        strategyList = csc.computeConversationalStrategy();
        Log4J.info(this, "Best Strategy Detected : " + strategyList.getBest());
        Log4J.info(this, "Strategy Distribution Detected : " + strategyList.toString());

        CSCOutput cscOutput = new CSCOutput();

        List<Strategy> strategies = new ArrayList<Strategy>();

        for (ConversationalStrategy cs : EnumSet.of(SD, SE, Praise, QESD, VSN, ASN)) {
            strategies.add(new Strategy(cs.shortName(), strategyList.get(cs)));
        }
        cscOutput.setUserStrategies(strategies);

        blackboard.post(this, SaraCons.MSG_CSC, cscOutput);
        Log4J.info(this, "Strategy Distribution Sent: " + cscOutput.toString());
    }

    @Override
    public void shutDown() {
        super.shutDown();
    }

}
