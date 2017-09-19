package edu.cmu.inmind.multiuser.sara.component;

import edu.cmu.inmind.multiuser.common.Constants;
import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.model.*;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StateType;
import edu.cmu.lti.rapport.pipline.csc.ConversationalStrategy;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import static edu.cmu.lti.rapport.pipline.csc.ConversationalStrategy.*;

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

    public void onEvent(BlackboardEvent blackboardEvent) throws Exception
    {
        CSCOutput cscOutput = new CSCOutput();
        Random r = new Random();

        List<Strategy> strategyList = new ArrayList<Strategy>();

        for (ConversationalStrategy cs : EnumSet.of(SD, SE, Praise, QESD, VSN, ASN)) {
            strategyList.add(new Strategy(cs.shortName(), r.nextDouble()));
        }
        cscOutput.setUserStrategies(strategyList);

        for(Strategy s : cscOutput.getUserStrategies()){
            System.out.println(s.getName() + " " + s.getScore());
        }

        blackboard().post(this, SaraCons.MSG_CSC, cscOutput);
    }

}
