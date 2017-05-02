package edu.cmu.inmind.multiuser.socialreasoner.control.reasoners;

import edu.cmu.inmind.multiuser.socialreasoner.control.MainController;
import edu.cmu.inmind.multiuser.socialreasoner.control.bn.BehaviorNetworkPlus;
import edu.cmu.inmind.multiuser.socialreasoner.control.bn.BehaviorPlus;
import edu.cmu.inmind.multiuser.socialreasoner.control.controllers.BehaviorNetworkController;
import edu.cmu.inmind.multiuser.socialreasoner.control.util.Utils;
import edu.cmu.inmind.multiuser.socialreasoner.control.vht.VHTConnector;
import edu.cmu.inmind.multiuser.socialreasoner.model.SocialReasonerOutput;
import edu.cmu.inmind.multiuser.socialreasoner.model.blackboard.Blackboard;
import edu.cmu.inmind.multiuser.socialreasoner.model.history.SocialHistory;
import edu.cmu.inmind.multiuser.socialreasoner.model.intent.SystemIntent;
import edu.cmu.inmind.multiuser.socialreasoner.model.messages.SROutputMessage;
import edu.cmu.inmind.multiuser.socialreasoner.view.ui.Visualizer;

import java.util.Arrays;
import java.util.List;

/**
 * Created by oscarr on 6/3/16.
 */
public class SocialReasoner {
    private BehaviorNetworkController controller;
    private BehaviorNetworkPlus network;
    private String name;
    private Blackboard blackboard;
    private int cycles = 0;
    private final int maxNumCycles = 8;
    //private Model model;
    //private State current;
    private VHTConnector vhtConnector;
    private SocialHistory socialHistory;
    private Visualizer visualizer;
    //private TaskReasoner taskReasoner;
    private static SocialReasoner instance;
    private int stepCount;
    private boolean flagSentSROutput = false;

    private SocialReasoner(BehaviorNetworkController bnt, String name){
        this.controller = bnt;
        this.network = bnt.getNetwork();
        this.name = name;
        this.blackboard = Blackboard.getInstance();
        this.vhtConnector = VHTConnector.getInstance();
        this.socialHistory = SocialHistory.getInstance();
        this.visualizer = Visualizer.getInstance();
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

    public BehaviorNetworkPlus getNetwork(){
        return this.network;
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
            network.setState(blackboard.getModel());
            if(MainController.verbose) System.out.println("*** States: " + Arrays.toString( blackboard.getModel().toArray() ) );
            boolean isDecsionMade = false, usingHighestActivation = false;
            while( !isDecsionMade ) {
                if(MainController.verbose) {
                    System.out.println("cycle: " + cycles);
                }
                int idx = network.selectBehavior();
                if ((idx >= 0 && cycles > 0) || cycles >= maxNumCycles) {
                    if (idx < 0 && cycles >= maxNumCycles) {
                        network.getHighestActivationUsingNone(); // NONE // previously: network.getHighestActivation();
                        usingHighestActivation = true;
                    }
                    String behaviorName = network.getNameBehaviorActivated();
                    socialHistory.add(System.currentTimeMillis(), behaviorName, MainController.rapportLevel, MainController.rapportScore);
                    MainController.conversationalStrategies = network.getModuleNamesByHighestActivation();
                    if( !usingHighestActivation ){
                        Utils.exchange(MainController.conversationalStrategies, behaviorName);
                    }
                    MainController.currentTurn++;

                    // send results to NLG and print them out on the screen
                    if (vhtConnector != null) {
                        sendBNActivations();
                        sendToNLG( intent, MainController.conversationalStrategies );
                        sendToClassifier(MainController.conversationalStrategies);
                        flagSentSROutput = true;
                    }
                    isDecsionMade = true;
                    if (MainController.useSRPlot) {
                        //visualizer.printFSMOutput(output);
                        //visualizer.printStates(network.getStateString(), current.phase, current.name);
                        //Utils.sleep( 3000 );
                    }

                    //update state
                    network.execute(cycles);
                    cycles = 0;
                } else {
                    cycles++;
                }
                if (MainController.useSRPlot) {
                    visualizer.plot(network.getActivations(), network.getModules().size(), name, network.getTheta(),
                            network.getNameBehaviorActivated());
                }
                if (vhtConnector != null && !flagSentSROutput && stepCount % 20 == 0) {
                    sendBNActivations();
                }
                stepCount++;
                flagSentSROutput = false;
                System.gc();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if( MainController.flagResetTR || MainController.flagReset ){
                if( MainController.flagResetTR ) {
                    MainController.flagResetTR = false;
                }
            }
        }
    }

    private void sendBNActivations() {
        SocialReasonerOutput output = new SocialReasonerOutput();
        output.setActivations(network.getOnlyActivations());
        output.setNames(network.getModuleNames());
        output.setThreshold(network.getTheta());
        vhtConnector.sendActivations(output);
    }

    private void sendToNLG(SystemIntent intent, String[] convStrategies){
        if( convStrategies != null ){
            for( int i = 0; i< convStrategies.length; i++ ){
                convStrategies[i] = convStrategies[i].substring( 0, convStrategies[i].indexOf("_") );
            }
        }
        SROutputMessage srOutputMessage = new SROutputMessage();
        srOutputMessage.addIntent(intent, convStrategies);
        srOutputMessage.setPhase(intent.getPhase());
        srOutputMessage.setRapport(MainController.rapportScore);

        //json = json.replace("}],\"rapport\"", ",\"conversational_strategies\":" + jsonConvStrat + "}],\"rapport\"");
        vhtConnector.sendToNLG(Utils.toJson(srOutputMessage) );
    }



    private void sendToClassifier(String[] convStrategies){
        vhtConnector.sendToClassifier( convStrategies[0] );
    }

    public void initialize() {
        List<BehaviorPlus> modules = network.getModules();
        int size = modules.size();
        String[] names = new String[size + 1];
        for(int i = 0; i < names.length-1; i++) {
            names[i] = modules.get(i).getName();
        }
        names[ size ] = "Activation Threshold";
    }

//    public void setModel(Model model) {
//        this.model = model;
//        current = model.current;
//        taskReasoner = TaskReasoner.getInstance();
//    }

    public static void reset() {
        instance.network.resetAll();
        instance = null;
    }
}
