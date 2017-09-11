package edu.cmu.inmind.multiuser.socialreasoner.control.emulators;


import edu.cmu.inmind.multiuser.socialreasoner.control.SocialReasonerController;
import edu.cmu.inmind.multiuser.socialreasoner.model.Constants;
import edu.cmu.inmind.multiuser.socialreasoner.model.intent.SystemIntent;

import java.util.ArrayList;

/**
 * Created by oscarr on 9/12/16.
 */
public class Emulator{
    private ArrayList<EmulationStep> steps = new ArrayList();
    private boolean stop = false;
    private SocialReasonerController socialReasonerController;
    private int step;

    public void setSocialReasonerController(SocialReasonerController socialReasonerController) {
        this.socialReasonerController = socialReasonerController;
    }

    public Emulator() {
//        // incremental engagement
//        steps.add( new EmulationStep( 1, Constants.ASN_USER_CS, false, false, 1 ) );
//        steps.add( new EmulationStep( 2, Constants.ASN_USER_CS, false, true, 2 ) );
//        steps.add( new EmulationStep( 2, Constants.ASN_USER_CS, false, false, 3 ) );
//        steps.add( new EmulationStep( 3, Constants.ASN_USER_CS, false, true, 4 ) );
//        steps.add( new EmulationStep( 3, Constants.PR_USER_CS, false, false, 5 ) );
//        steps.add( new EmulationStep( 4, Constants.SD_USER_CS, false, true, 6 ) );
//        steps.add( new EmulationStep( 4, Constants.ASN_USER_CS, true, true, 7 ) );
//        steps.add( new EmulationStep( 4, Constants.SD_USER_CS, false, false, 8 ) );
//        steps.add( new EmulationStep( 5, Constants.SD_USER_CS, false, true, 9 ) );
//        steps.add( new EmulationStep( 5, Constants.PR_USER_CS, false, true, 10 ) );
//        steps.add( new EmulationStep( 5, Constants.VSN_USER_CS, true, true, 11 ) );
//        steps.add( new EmulationStep( 5, Constants.SD_USER_CS, false, true, true, 12 ) );
//        steps.add( new EmulationStep( 5, Constants.PR_USER_CS, true, true, 13 ) );
//        steps.add( new EmulationStep( 6, Constants.SD_USER_CS, false, true, 14 ) );
//        steps.add( new EmulationStep( 6, Constants.RSE_USER_CS, false, true, 15 ) );
//        steps.add( new EmulationStep( 6, Constants.VSN_USER_CS, true, true, 16 ) );
//        steps.add( new EmulationStep( 6, Constants.SD_USER_CS, false, true, 17 ) );
//        steps.add( new EmulationStep( 7, Constants.SD_USER_CS, true, true, 18 ) );
//        steps.add( new EmulationStep( 7, Constants.VSN_USER_CS, false, true, 19 ) );
//        steps.add( new EmulationStep( 7, Constants.PR_USER_CS, true, true, 20 ) );

        //flat user
//        steps.add( new EmulationStep( 4, Constants.ASN_USER_CS, false, false, 1 ) );
//        steps.add( new EmulationStep( 4, Constants.ASN_USER_CS, false, true, 2 ) );
//        steps.add( new EmulationStep( 4, Constants.ASN_USER_CS, false, false, 3 ) );
//        steps.add( new EmulationStep( 4, Constants.ASN_USER_CS, false, true, 4 ) );
//        steps.add( new EmulationStep( 4, Constants.ASN_USER_CS, false, false, 5 ) );
//        steps.add( new EmulationStep( 4, Constants.ASN_USER_CS, false, true, 6 ) );
//        steps.add( new EmulationStep( 4, Constants.ASN_USER_CS, false, true, 7 ) );
//        steps.add( new EmulationStep( 4, Constants.ASN_USER_CS, false, false, 8 ) );
//        steps.add( new EmulationStep( 4, Constants.ASN_USER_CS, false, true, 9 ) );
//        steps.add( new EmulationStep( 4, Constants.ASN_USER_CS, false, true, 10 ) );
//        steps.add( new EmulationStep( 4, Constants.ASN_USER_CS, false, true, 11 ) );
//        steps.add( new EmulationStep( 4, Constants.ASN_USER_CS, false, true, 12 ) );
//        steps.add( new EmulationStep( 4, Constants.ASN_USER_CS, false, true, 13 ) );
//        steps.add( new EmulationStep( 4, Constants.ASN_USER_CS, false, true, 14 ) );
//        steps.add( new EmulationStep( 4, Constants.ASN_USER_CS, false, true, 15 ) );
//        steps.add( new EmulationStep( 4, Constants.ASN_USER_CS, false, true, 16 ) );
//        steps.add( new EmulationStep( 4, Constants.ASN_USER_CS, false, true, 17 ) );
//        steps.add( new EmulationStep( 4, Constants.ASN_USER_CS, false, true, 18 ) );
//        steps.add( new EmulationStep(4, Constants.ASN_USER_CS, false, true, 19));
//        steps.add( new EmulationStep( 4, Constants.ASN_USER_CS, false, true, 20 ) );

        // low rapport
        steps.add( new EmulationStep( 1, Constants.ASN_USER_CS, false, false, true, "ask_something") );
        steps.add( new EmulationStep( 5, Constants.ASN_USER_CS, false, false, true, "greeting") );
        steps.add( new EmulationStep( 6, Constants.ASN_USER_CS, false, false, true, "greeting" ) );
        steps.add( new EmulationStep( 6, Constants.ASN_USER_CS, false, false, true, "greeting") );
        steps.add( new EmulationStep( 7, Constants.PR_USER_CS, false, false, true, "greeting") );
        steps.add( new EmulationStep( 7, Constants.ASN_USER_CS, false, false, true, "greeting") );
        steps.add( new EmulationStep( 6, Constants.ASN_USER_CS, false, false, true, "greeting") );
        steps.add( new EmulationStep( 6, Constants.ASN_USER_CS, false, false, true, "greeting") );
        steps.add( new EmulationStep( 5, Constants.ASN_USER_CS, false, false, true, "greeting") );
        steps.add( new EmulationStep( 5, Constants.PR_USER_CS, false, false, true, "greeting") );
        steps.add( new EmulationStep( 4, Constants.ASN_USER_CS, false, false, true, "greeting") );
        steps.add( new EmulationStep( 4, Constants.ASN_USER_CS, false, false, true, "greeting") );
        steps.add( new EmulationStep( 3, Constants.PR_USER_CS, false, false, true, "greeting") );
        steps.add( new EmulationStep( 3, Constants.ASN_USER_CS, false, false, true, "greeting") );
        steps.add( new EmulationStep( 2, Constants.ASN_USER_CS, false, false, true, "greeting") );
        steps.add( new EmulationStep( 2, Constants.VSN_USER_CS, false, false, true, "greeting") );
        steps.add( new EmulationStep( 2, Constants.ASN_USER_CS, false, false, true, "greeting") );
        steps.add( new EmulationStep( 1, Constants.ASN_USER_CS, false, false, true, "greeting") );
        steps.add( new EmulationStep( 1, Constants.ASN_USER_CS, false, false, true, "greeting") );
        steps.add( new EmulationStep( 1, Constants.PR_USER_CS, false, false, true, "greeting") );
    }

    public EmulationStep execute(){
        EmulationStep scriptResult = null;
        if( !steps.isEmpty() ){
            if( SocialReasonerController.verbose ) System.out.println(
                    String.format("\n\n================================================================================" +
                            "================================================================================\n" +
                            "Running Emulator Step: %s\n|", (++step) ));
            EmulationStep scriptStep = steps.get(0);
            // rapport level and rapport delta
            SocialReasonerController.calculateRapScore(String.valueOf(scriptStep.getRapportScore()));
            // so far, we are only using smile and eye gaze
            SocialReasonerController.setNonVerbals(scriptStep.isSmiling(), scriptStep.isGazeAtPartner());
            // this is UCS classifier's output
            SocialReasonerController.userConvStrategy = scriptStep.getUserCS();
            // just user history, system history is internally done by social reasoenr
            SocialReasonerController.userCSHistory.add( );
            // is there available shared experiences?
            SocialReasonerController.setAvailableSE(scriptStep.isAvailableSharedExperiences());
            scriptResult = scriptStep;
            steps.remove( scriptStep );
            // system intent according to DM or TR (movie recommendation?)
            socialReasonerController.addSystemIntent( new SystemIntent( scriptStep.getSystemIntent(), "phase" ));
        }
        return scriptResult;
    }


}
