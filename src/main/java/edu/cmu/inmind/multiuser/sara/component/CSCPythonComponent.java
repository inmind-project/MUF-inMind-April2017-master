package edu.cmu.inmind.multiuser.sara.component;

import edu.cmu.inmind.multiuser.common.Constants;
import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.model.ASROutput;
import edu.cmu.inmind.multiuser.common.model.CSCOutput;
import edu.cmu.inmind.multiuser.common.model.Strategy;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StateType;
import org.zeromq.ZMQ;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by oscarr on 3/7/17.
 */
@StateType( state = Constants.STATELESS)
@BlackboardSubscription( messages = {SaraCons.MSG_ASR} )
public class CSCPythonComponent extends PluggableComponent {
    private ZMQ.Context context;
    private ZMQ.Socket requester;
    private String cscServerIpHost = "localhost"; //host name of CSC server

    @Override
    public void startUp(){
        super.startUp();
        // Context of ZMQ
        context = ZMQ.context(1);
        //  Socket to talk to server
        requester = context.socket(ZMQ.REQ);
        requester.connect("tcp://" + cscServerIpHost + ":7000");
        Log4J.info(this, "Connected to CSC server.");
	    Log4J.info(this, "Startup has finished.");
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
        Object input = blackboard().get("MSG_ASR");
        CSCOutput cscOutput = new CSCOutput();
        String cscResult;

        if (input instanceof ASROutput) {
            String asr = ((ASROutput) input).getUtterance();
            // send message to CSC server
            requester.send(asr, 0);
            // get response from CSC server
            cscResult = requester.recvStr(0);
            Log4J.info(this, "CSC result: " + cscResult + " input: " + asr);
        } else {
            throw new IllegalArgumentException("I only eat ASROutput");
        }

        List<Strategy> strategyList = new ArrayList<Strategy>();

        Strategy strategySD = new Strategy();
        strategySD.setName("SD");
        if(cscResult.equals("SD")) {
            strategySD.setScore(1.0);
        }else{
            strategySD.setScore(0.0);
        }
        strategyList.add(strategySD);

        Strategy strategySE = new Strategy();
        strategySE.setName("SE");
        if(cscResult.equals("SE")) {
            strategySE.setScore(1.0);
        }else{
            strategySE.setScore(0.0);
        }
        strategyList.add(strategySE);

        Strategy strategyPR = new Strategy();
        strategyPR.setName("PR");
        if(cscResult.equals("PR")) {
            strategyPR.setScore(1.0);
        }else{
            strategyPR.setScore(0.0);
        }
        strategyList.add(strategyPR);

        Strategy strategyQESD = new Strategy();
        strategyQESD.setName("QESD");
        if(cscResult.equals("QESD")) {
            strategyQESD .setScore(1.0);
        }else{
            strategyQESD .setScore(0.0);
        }
        strategyList.add(strategyQESD);

        Strategy strategyVSN = new Strategy();
        strategyVSN.setName("VSN");
        if(cscResult.equals("VSN")) {
            strategyVSN.setScore(1.0);
        }else{
            strategyVSN.setScore(0.0);
        }
        strategyList.add(strategyVSN);

        Strategy strategyASN = new Strategy();
        strategyASN.setName("ASN");
        if(cscResult.equals("ASN")) {
            strategyASN.setScore(1.0);
        }else{
            strategyASN.setScore(0.0);
        }
        strategyList.add(strategyASN);

        Strategy strategyIN = new Strategy();
        strategyIN.setName("IN");
        if(cscResult.equals("IN")) {
            strategyIN.setScore(1.0);
        }else{
            strategyIN.setScore(0.0);
        }
        strategyList.add(strategyIN);


        cscOutput.setUserStrategies(strategyList);

        String str = "";
        for(Strategy s : cscOutput.getUserStrategies()){
            str += s.getName() + " " + s.getScore() + " ";
        }
        Log4J.info(this,str);

        blackboard().post(this, SaraCons.MSG_CSC, cscOutput);
    }

    @Override
    public void shutDown(){

    }

}
