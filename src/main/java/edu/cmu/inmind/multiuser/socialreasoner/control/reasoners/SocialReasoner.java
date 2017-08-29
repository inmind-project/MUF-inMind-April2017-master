package edu.cmu.inmind.multiuser.socialreasoner.control.reasoners;

import edu.cmu.inmind.multiuser.socialreasoner.control.SocialReasonerController;
import edu.cmu.inmind.multiuser.socialreasoner.control.bn.BehaviorNetwork;
import edu.cmu.inmind.multiuser.socialreasoner.control.bn.BehaviorNetworkController;
import edu.cmu.inmind.multiuser.socialreasoner.control.util.Utils;
import edu.cmu.inmind.multiuser.socialreasoner.model.intent.SystemIntent;
import edu.cmu.inmind.multiuser.socialreasoner.control.bn.Behavior;
import edu.cmu.inmind.multiuser.socialreasoner.model.blackboard.Blackboard;
import edu.cmu.inmind.multiuser.socialreasoner.model.history.SocialHistory;

import java.util.Arrays;
import java.util.List;

/**
 * Created by oscarr on 6/3/16.
 */
public class SocialReasoner {
    private BehaviorNetwork network;
    private String name;
    private Blackboard blackboard;
    private int cycles = 0;
    private final int maxNumCycles = 8;
    //private Model model;
    //private State current;
    private SocialHistory socialHistory;
    //private TaskReasoner taskReasoner;
    private static SocialReasoner instance;
    private int stepCount;
    private boolean flagSentSROutput = false;

    private SocialReasoner(BehaviorNetworkController bnt, String name){
        this.network = bnt.getNetwork();
        this.name = name;
        this.blackboard = Blackboard.getInstance();
        this.socialHistory = SocialHistory.getInstance();
        initialize();
        blackboard.setStatesString( bnt.getStates(), bnt.getName() );
    }

    public static SocialReasoner getInstance(BehaviorNetworkController bnt, String name){
        if( instance == null ){
            instance = new SocialReasoner( bnt, name);
        }
        return instance;
    }

    public static SocialReasoner getInstance(){
        return instance;
    }

    public String getOutput(){
        return network.getOutput();
    }

    public String getMatches(){
        return network.getMatchesOutput();
    }

    public String getStates(){
        return network.getStatesOutput();
    }

    public void execute(SystemIntent intent){
        try {
            Utils.startCrono();
            network.setState(blackboard.getModel());
            if( SocialReasonerController.verbose ) System.out.println("|-- CURRENT STATE: " + Arrays.toString( blackboard.getModel().toArray() ) );
            boolean isDecisionMade = false;
            while( !isDecisionMade ) {
                if(SocialReasonerController.verbose) {
                    System.out.println("|-- BEHAVIOR NETWORK CYCLE: " + cycles);
                }
                int idx = network.selectBehavior();
                if ((idx >= 0 && cycles > 0) || cycles >= maxNumCycles) {
                    if (idx < 0 && cycles >= maxNumCycles) {
                        network.getHighestActivationUsingNone(); // NONE // previously: network.getHighestActivation();
                    }
                    String behaviorName = network.getNameBehaviorActivated();
                    socialHistory.add(System.currentTimeMillis(), behaviorName, SocialReasonerController.rapportLevel, SocialReasonerController.rapportScore);
                    SocialReasonerController.conversationalStrategies = network.getModuleNamesByHighestActivation();
                    Utils.exchange(SocialReasonerController.conversationalStrategies, behaviorName);

                    // 2 turns: one the user one the system
                    SocialReasonerController.currentTurn = SocialReasonerController.currentTurn + 2;

                    // send results to NLG and printFileName them out on the screen
//                    sendBNActivations();
//                    sendToNLG( intent, SocialReasonerController.conversationalStrategies );
//                    sendToClassifier(SocialReasonerController.conversationalStrategies);
//                    flagSentSROutput = true;

                    isDecisionMade = true;

                    //update state
                    network.execute(cycles);
                    cycles = 0;
                } else {
                    cycles++;
                }
                stepCount++;
                flagSentSROutput = false;
                System.gc();
            }
            Utils.stopCrono("SocialReasoner.execute");
            System.out.println("   |-- SR OUTPUT (SORTED CONVERSATIONAL STRATEGIES): " + Arrays.toString( SocialReasonerController.conversationalStrategies ));
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if( SocialReasonerController.flagResetTR || SocialReasonerController.flagReset ){
                if( SocialReasonerController.flagResetTR ) {
                    SocialReasonerController.flagResetTR = false;
                }
            }
        }
    }


    private void sendToNLG(SystemIntent intent, String[] convStrategies){
        if( convStrategies != null ){
            for( int i = 0; i< convStrategies.length; i++ ){
                convStrategies[i] = convStrategies[i].substring( 0, convStrategies[i].indexOf("_") );
            }
        }
    }

    public void initialize() {
        List<Behavior> modules = network.getModules();
        int size = modules.size();
        String[] names = new String[size + 1];
        for(int i = 0; i < names.length-1; i++) {
            names[i] = modules.get(i).getName();
        }
        names[ size ] = "Activation Threshold";
    }


    public static void reset() {
        instance.network.resetAll();
        instance = null;
    }
}
