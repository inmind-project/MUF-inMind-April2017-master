package edu.cmu.inmind.multiuser.socialreasoner.view.ui;

import edu.cmu.inmind.multiuser.socialreasoner.control.MainController;
import edu.cmu.inmind.multiuser.socialreasoner.control.controllers.BehaviorNetworkController;
import edu.cmu.inmind.multiuser.socialreasoner.view.emulators.InputController;

import java.util.ArrayList;

/**
 * Created by oscarr on 6/3/16.
 */
public class Visualizer {
    private CombinedBNXYPlot plot;
    private static Visualizer instance;
    private ArrayList<Double>[] behaviorValues;

    public static Visualizer getInstance(){
        if( instance == null ){
            instance = new Visualizer();
        }
        return instance;
    }

    public void initializePlot( MainController MainController, BehaviorNetworkController bnt1, BehaviorNetworkController bnt2 ){
        plot = CombinedBNXYPlot.startPlot( bnt1.getName(), MainController.useFSM? null : bnt2.getName(), bnt1.getTitle(),
                MainController.useFSM? null : bnt2.getTitle(), bnt1.getSeries(), MainController.useFSM? null : bnt2.getSeries(), MainController.useSRPlot );
        behaviorValues = new ArrayList[bnt1.getSize()];
        for( int i = 0; i < behaviorValues.length; i++ ){
            behaviorValues[i] = new ArrayList<>();
        }
    }

    public void plot(double[] values, int size, String name, double theta, String behActivated){
        for(int i = 0; i < size; i++){
            behaviorValues[i].add(values[i]);
        }
        plot.setDataset(name, behaviorValues, theta, behActivated, values[size] );
    }

    public void printFSMOutput(String output) {
        plot.getOutputPanel().printFSMOutput( output);
    }

    public void printStates(String stateString, String phase, String intent) {
        plot.getOutputPanel().printStates( stateString, phase, intent);
    }

    public InputController getInputController() {
        return plot.getInputController();
    }
}
