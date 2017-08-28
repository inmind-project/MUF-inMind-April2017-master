package edu.cmu.inmind.multiuser.socialreasoner.control.emulators;

import edu.cmu.inmind.multiuser.socialreasoner.control.SocialReasonerController;
import edu.cmu.inmind.multiuser.socialreasoner.model.intent.SystemIntent;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by oscarr on 11/29/16.
 */
public class SystemIntentEmulator {
    private List<SystemIntentStep> steps = new ArrayList<>();
    private boolean inputFromConsole = false;
    private Scanner scanner = new Scanner(System.in);
    //private ExcelReaderWriter excelReaderWriter;

//    public boolean createScript(ExcelReaderWriter excelReaderWriter){
//        this.excelReaderWriter = excelReaderWriter;
//        excelReaderWriter.openWorkbook("SocialReasonerTests.xlsx", true);
//        steps = excelReaderWriter.readSheet();
//        System.out.println("Scenario: " + excelReaderWriter.getScenarioName() );
//        return !steps.isEmpty();
//    }

    public SystemIntent execute(){
        SystemIntent scriptResult = null;
        if( inputFromConsole ){
            System.out.println("Enter your input (system_intent phase UserConvStrat smile gaze) :");
            String[] input = scanner.nextLine().split(" ");

        }else{
            if( !steps.isEmpty() ){
                SystemIntentStep scriptStep = steps.get(0);
                SocialReasonerController.userCSHistory.add( );
                SocialReasonerController.calculateRapScore(String.valueOf(scriptStep.getRapportScore()));
                SocialReasonerController.setNonVerbals(scriptStep.isSmiling(), scriptStep.isGazeAtPartner());
                SocialReasonerController.userConvStrategy = scriptStep.getUserCS();
                SocialReasonerController.setAvailableSE(scriptStep.isAvailableSharedExperiences());
                scriptResult = new SystemIntent( scriptStep.getIntent(), scriptStep.getPhase() );
                if( SocialReasonerController.verbose ) {
                    System.out.println("\n\n=============================================================\n" +
                            "Running Emulated Step: " + scriptStep.toString());
                }
                steps.remove( scriptStep );
//                scanner.nextLine();
            }
        }
        return scriptResult;
    }

    public boolean isEmpty() {
        return steps.isEmpty();
    }

//    public boolean checkReset() {
//        return isEmpty() && !excelReaderWriter.checkFinished();
//    }
//
//    public void writeComparison() {
//        excelReaderWriter.writeComparison();
//    }
}
