package edu.cmu.inmind.multiuser.sara.component;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.model.CSCOutput;
import edu.cmu.inmind.multiuser.common.model.ConversationalStrategy;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StatelessComponent;

import java.util.EnumMap;
import java.util.Random;

/**
 * Created by fpecune on 5/22/2017.
 */

    @StatelessComponent
    @BlackboardSubscription( messages = {SaraCons.MSG_ASR} )
    public class CSCComponent extends PluggableComponent {

        @Override
        public void startUp(){
            super.startUp();
            //Log4J.info(this, "CSCComponent: startup has finished.");
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

            CSCOutput cscOutput = new CSCOutput();
            Random r = new Random();

            EnumMap<ConversationalStrategy, Double> strategyList = new EnumMap<ConversationalStrategy, Double>(ConversationalStrategy.class);


            strategyList.put(ConversationalStrategy.ASN, r.nextDouble());
            strategyList.put(ConversationalStrategy.PR, r.nextDouble());
            strategyList.put(ConversationalStrategy.QESD, r.nextDouble());
            strategyList.put(ConversationalStrategy.SD, r.nextDouble());
            strategyList.put(ConversationalStrategy.SE, r.nextDouble());
            strategyList.put(ConversationalStrategy.VSN, r.nextDouble());

            cscOutput.setStrategyScores(strategyList);

            Log4J.info(this, "CSCComponent: done classifying." + cscOutput.toString());

            blackboard().post(this, SaraCons.MSG_CSC, cscOutput);
        }

        @Override
        public void shutDown(){

        }

    }
