package edu.cmu.inmind.multiuser.sara.component;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.model.NonVerbalOutput;
import edu.cmu.inmind.multiuser.common.model.RapportOutput;
import edu.cmu.inmind.multiuser.common.model.SaraInput;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StatelessComponent;
import edu.cmu.inmind.multiuser.rapportestimator.temporal_association_rule.RapportClient;
import edu.cmu.inmind.multiuser.rapportestimator.vhmsg.main.VhmsgSender;
import edu.usc.ict.vhmsg.MessageEvent;
import edu.usc.ict.vhmsg.MessageListener;
import edu.usc.ict.vhmsg.VHMsg;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by oscarr on 3/7/17.
 */
@StatelessComponent
@BlackboardSubscription( messages = {SaraCons.MSG_ASR, SaraCons.MSG_NVB})
public class RapportEstimator extends PluggableComponent implements MessageListener {

    private RapportClient rapportClient;
    //RapportClient
    public static RapportClient client;


    // Last conversational strategy used by user or system
    public static String last_user_strategy;
    public static String last_system_strategy;
    public static boolean user_smile;
    public static double last_temporal_rule_result;
    public static double potential_rapport_score;
    private double rapportScore = 4;

    // Handcrafted rule addition and substraction
    public static double addition;
    public static double substract;

    // Turn information
    public static int Turn_number = 0;

    // for subscription
    public static VHMsg vhmsgSubscriber;

    // for sending
    private VHMsg rapportEstSender;
    private final static int agentId = 0;
    public static ServerSocket listener;
    public static Socket socket;

    public final static int port = 2003;
    public static double global_rapport_score;

    @Override
    public void startUp(){
        super.startUp();

        //Create a new thread for the social reasoner
        //rapportClient = new RapportClient();
        VHMsg vhmsgSubscriber = new VHMsg();
        vhmsgSubscriber.openConnection();
        vhmsgSubscriber.enableImmediateMethod();
        vhmsgSubscriber.addMessageListener(this);
        vhmsgSubscriber.subscribeMessage("vrStrategyRecog");
        vhmsgSubscriber.subscribeMessage("vrMultisense");
        vhmsgSubscriber.subscribeMessage("vrSocialReasoner");
        vhmsgSubscriber.subscribeMessage("vrRapportEst");

        // TODO: add code to initialize this component
    }

    @Override
    public void execute() {
        Log4J.info(this, "SocialReasonerComponent: " + hashCode());

        extractAndProcess();
    }

    public void updateNVB() {
        NonVerbalOutput nvbOutput = (NonVerbalOutput) blackboard().get(SaraCons.MSG_NVB);
        vhmsgSubscriber.sendMessage("vrMultisense 0 " + nvbOutput.isSmiling() + " 0.939988519996405 " + nvbOutput.isGazeAtPartner() + " false neutral 1.0 true");
    }

    private void extractAndProcess() {
        Object input = blackboard().get(SaraCons.MSG_ASR);
        SaraInput saraInput;
        if (input instanceof String) {
            saraInput = new SaraInput();
            saraInput.setASRinput((String) input);
        } else if (input instanceof SaraInput) {
            saraInput = (SaraInput) input;
        } else {
            throw new IllegalArgumentException("I only eat String and SaraInput");
        }

        VhmsgSender sender = new VhmsgSender("vrASR");
        //sender.sendMessage(0 + " " + saraInput.getASRinput());
        sender.sendMessage(0 + " " + saraInput.getASRinput());


        // do some fancy processing
        // ....
        //saraOutput.setSocialIntent(new SocialIntent( 5.5, "high-rapport", "SD") );
        Log4J.info(this, "Input: " + saraInput.getASRinput() + "\n");

    }

    /**
     * If the blackboard model is modified externally, does SR have to do anything? this is useful when running multiple
     * processes in parallel rather than sequentially.
     */
    @Override
    public void onEvent(BlackboardEvent event) {
        //TODO: add code here
        //...
        Log4J.info(this, "RapportComponent. These objects have been updated at the blackboard: " + event.toString());
        if (event.getId()==SaraCons.MSG_NVB) {
            //System.out.println(" ###################### Message from OpenFace");
            updateNVB();
        }
        if (event.getId()==SaraCons.MSG_ASR) {
            extractAndProcess();
        }


    }

    @Override
    public void shutDown() {
        super.shutDown();
        // TODO: add code to release resources
    }

    @Override
    public void messageAction(MessageEvent messageEvent) {

        // TODO Auto-generated method stub

        String message = messageEvent.toString();

        String[] tokens = message.split(" ");
        System.out.println(messageEvent);
        String content = "";


        // ###############Begin of User Input###################
        // Conversational Strategy Classifier output
        if (tokens[0].equals("vrStrategyRecog")) {

            // Count Turns of user speak
            Turn_number++;

            // SD =3, SE=6,PRAISE=9,VSN=12, ASN=15
            if (tokens[3].equals("TRUE")) {
                last_user_strategy = "QESD";
                // Rule 2:Encourage user did self-disclosure as early as
                // possible
                if (Turn_number <= 10) {
                    addition = 1 / Turn_number;
                } else {
                    addition = 1 / 10;
                }

                // Rule 1: encourage norm of reciprocity except PR
                if (last_system_strategy.equals("SD")) {
                    addition = 0.5;
                }

            }

            if (tokens[6].equals("TRUE")) {
                last_user_strategy = "SD";
                // Rule 1: encourage norm of reciprocity except PR
                //if (last_system_strategy.equals("SE")) {
                //    addition = 0.5;
                //}

            }

            if (tokens[9].equals("TRUE")) {
                last_user_strategy = "SE";
                // Rule 1: encourage norm of reciprocity except PR
                if (last_system_strategy == "PR") {
                    substract = 0.5;
                }
            }

            if (tokens[12].equals("TRUE")) {
                last_user_strategy = "PR";

                // Rule 1: encourage norm of reciprocity except PR
                if (last_system_strategy == "VSN") {
                    addition = 0.5;
                }

                //Rule 3: encourage user did VSN at the later stage with smile
                if(user_smile&&Turn_number>10){
                    addition=0.5;
                }
                else{
                    substract=0.5;
                }

            }

            if (tokens[15].equals("TRUE")) {
                if (Double.parseDouble(tokens[7])>Double.parseDouble(tokens[16])) {
                    last_user_strategy = "SD";
                } else {
                    last_user_strategy = "VSN";
                }

                if(last_system_strategy=="SD"){
                    substract=0.5;
                }
            }

            if (tokens[18].equals("TRUE")) {
                if (Double.parseDouble(tokens[7])>Double.parseDouble(tokens[19])) {
                    last_user_strategy = "SD";
                } else {
                    last_user_strategy = "ASN";
                }

                if(last_system_strategy=="SD"){
                    substract=0.5;
                }
            }

            else {

                //System.out.println("##################I am in else#############");
            }

        }

        // Multisense output
        if (tokens[0].equals("vrMultisense")) {
            if (tokens[2].equals("true")) {
                user_smile=true;
            }
            else{
                user_smile=false;
            }
        }

        if (tokens[0].equals("vrRapportEst")) {
            rapportScore = Double.parseDouble(tokens[2]);
        }
        // ###############End of User Input###################

        // ###############Begin of System Input###################
        // Social Reaonser output
        if (tokens[0].equals("vrSocialReasoner")) {
            if (tokens[2].equals("SD") || tokens[2].equals("QESD")) {
                last_system_strategy="SD";
            }

            if (tokens[2].equals("RSE")) {
                last_system_strategy="SE";
            }
            if (tokens[2].equals("PR")) {
                last_system_strategy="PR";
            }
            if (tokens[2].equals("VSN")) {
                last_system_strategy="VSN";
            }
            if (tokens[2].equals("ASN")) {
                last_system_strategy="ASN";
            }
            else{
                last_system_strategy="NA";
            }

        }

        potential_rapport_score=global_rapport_score;
        potential_rapport_score+=addition;
        potential_rapport_score-=substract;
        addition=0.0;
        substract=0.0;
        if(potential_rapport_score<=7.0){
            global_rapport_score=potential_rapport_score;
            //client.rapportEstSender.sendMessage(agentId + " " + global_rapport_score);

            RapportOutput rapportOutput = new RapportOutput();
            rapportOutput.setRapportScore(rapportScore);
            rapportOutput.setUserStrategy(last_user_strategy);
            Log4J.info(this, "Output: " + rapportOutput.getRapportScore() + "\n");
            blackboard().post(this, SaraCons.MSG_RPT, rapportOutput);


        }
    }
}
