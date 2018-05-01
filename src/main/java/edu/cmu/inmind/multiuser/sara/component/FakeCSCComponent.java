package edu.cmu.inmind.multiuser.sara.component;

import static edu.cmu.inmind.multiuser.common.model.ConversationalStrategy.ASN;
import static edu.cmu.inmind.multiuser.common.model.ConversationalStrategy.QESD;
import static edu.cmu.inmind.multiuser.common.model.ConversationalStrategy.SD;
import static edu.cmu.inmind.multiuser.common.model.ConversationalStrategy.SE;
import static edu.cmu.inmind.multiuser.common.model.ConversationalStrategy.VSN;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.model.CSCOutput;
import edu.cmu.inmind.multiuser.common.model.ConversationalStrategy;
import edu.cmu.inmind.multiuser.common.model.Strategy;
import edu.cmu.inmind.multiuser.controller.blackboard.Blackboard;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.common.Constants;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StateType;

@StateType( state = Constants.STATELESS)
@BlackboardSubscription( messages = {SaraCons.MSG_ASR} )
public class FakeCSCComponent extends PluggableComponent {

    @Override
    public void startUp(){
        super.startUp();
	    Log4J.info(this, "CSCComponent: startup has finished.");
    }


    @Override
    public void execute() {
        Log4J.info(this, "CSCComponent: " + hashCode());

    }

    public void postCreate(){
        String[] msgSubscriptions = {"MSG_ASR"};

    }

    public void onEvent(Blackboard blackboard,BlackboardEvent blackboardEvent) throws Throwable
    {
        CSCOutput cscOutput = new CSCOutput();
        Random r = new Random();

        List<Strategy> strategyList = new ArrayList<Strategy>();

        for (ConversationalStrategy cs : EnumSet.of(SD, SE, QESD, VSN, ASN)) {
            strategyList.add(new Strategy(cs.name(), r.nextDouble()));
        }
        cscOutput.setUserStrategies(strategyList);

        for(Strategy s : cscOutput.getUserStrategies()){
            System.out.println(s.getName() + " " + s.getScore());
        }

        blackboard.post(this, SaraCons.MSG_CSC, cscOutput);
    }

}
