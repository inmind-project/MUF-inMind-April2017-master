package edu.cmu.inmind.multiuser.sara.component;

import edu.cmu.inmind.multiuser.common.Constants;
import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.model.*;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.communication.ClientCommController;
import edu.cmu.inmind.multiuser.controller.communication.SessionMessage;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StateType;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by oscarr on 3/7/17.
 */
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

        Strategy strategySD = new Strategy();
        strategySD.setName("SD");
        strategySD.setScore(r.nextDouble());
        strategyList.add(strategySD);

        Strategy strategySE = new Strategy();
        strategySE.setName("SE");
        strategySE.setScore(r.nextDouble());
        strategyList.add(strategySE);

        Strategy strategyPR = new Strategy();
        strategyPR.setName("PR");
        strategyPR.setScore(r.nextDouble());
        strategyList.add(strategyPR);

        Strategy strategyQESD = new Strategy();
        strategyQESD.setName("QESD");
        strategyQESD.setScore(r.nextDouble());
        strategyList.add(strategyQESD);

        Strategy strategyVSN = new Strategy();
        strategyVSN.setName("VSN");
        strategyVSN.setScore(r.nextDouble());
        strategyList.add(strategyVSN);

        Strategy strategyASN = new Strategy();
        strategyASN.setName("ASN");
        strategyASN.setScore(r.nextDouble());
        strategyList.add(strategyASN);

        cscOutput.setUserStrategies(strategyList);

        for(Strategy s : cscOutput.getUserStrategies()){
            System.out.println(s.getName() + " " + s.getScore());
        }

        blackboard().post(this, SaraCons.MSG_CSC, cscOutput);
    }

    @Override
    public void shutDown(){

    }

}
