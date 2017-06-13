package edu.cmu.inmind.multiuser.sara.component;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.model.ASROutput;
import edu.cmu.inmind.multiuser.common.model.CSCOutput;
import edu.cmu.inmind.multiuser.common.model.ConversationalStrategy;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StatelessComponent;
import edu.cmu.lti.rapport.pipline.csc.ConversationalStrategyDistribution;
import edu.cmu.lti.rapport.pipline.csc.MultiClassifier;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Random;

/**
 * Created by fpecune on 5/22/2017.
 */

    @StatelessComponent
    @BlackboardSubscription( messages = {SaraCons.MSG_ASR} )
    public class CSCComponent extends PluggableComponent {

        MultiClassifier csc;

        @Override
        public void startUp(){
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

        public void postCreate(){
            String[] msgSubscriptions = {"MSG_ASR"};

        }

        public void onEvent(BlackboardEvent blackboardEvent) {

            Log4J.info(this, "CSCComponent: starting classifying.");

            Object input = blackboard().get("MSG_ASR");

            if (input instanceof ASROutput) {
                csc.setNewASRResult(((ASROutput) input).getUtterance());
            } else {
                throw new IllegalArgumentException("I only eat ASROutput");
            }

            Log4J.info(this, "CSCComponent: Utterance set." + ((ASROutput) input).getUtterance());


            ConversationalStrategyDistribution strategyList = new ConversationalStrategyDistribution();
            strategyList = csc.computeConversationalStrategy();
            Log4J.info(this, "Classifier results received" + strategyList.toString());

            CSCOutput cscOutput = new CSCOutput();
            cscOutput.setStrategyScores(strategyList);
            Log4J.info(this, "CSCComponent: done classifying." + cscOutput.toString());

            blackboard().post(this, SaraCons.MSG_CSC, cscOutput);
            Log4J.info(this, "Object sent");
        }

        @Override
        public void shutDown(){

        }

    }
