package edu.cmu.inmind.multiuser.sara.component;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.model.DMOutput;
import edu.cmu.inmind.multiuser.common.model.NonVerbalOutput;
import edu.cmu.inmind.multiuser.common.model.RapportOutput;
import edu.cmu.inmind.multiuser.common.model.SROutput;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StatefulComponent;
import edu.cmu.inmind.multiuser.rapportestimator.vhmsg.main.VhmsgSender;
import edu.cmu.inmind.multiuser.socialreasoner.control.MainController;
import edu.cmu.inmind.multiuser.socialreasoner.control.util.Utils;
import edu.cmu.inmind.multiuser.socialreasoner.model.SocialReasonerOutput;
import edu.cmu.inmind.multiuser.socialreasoner.model.intent.SystemIntent;

/**
 * Created by oscarr on 3/7/17.
 */
@StatefulComponent
@BlackboardSubscription( messages = {SaraCons.MSG_DM, SaraCons.MSG_RPT, SaraCons.MSG_NVB})
public class SocialReasonerComponent extends PluggableComponent {

    private MainController socialController;
    String systemStrategy = "";
    private SROutput sendToNLG;
    private int rapport=4;
    private String userCS="";
    private boolean isSmiling;
    private boolean isGazing;

    @Override
    public void startUp(){
        super.startUp();

        //Create a new thread for the social reasoner
        socialController = new MainController();
        // TODO: add code to initialize this component
    }

    @Override
    public void execute() {
        Log4J.info(this, "SocialReasonerComponent: " + hashCode());

    }


    private void updateRapport(){
        RapportOutput rapportOutput = (RapportOutput) blackboard().get(SaraCons.MSG_RPT);
        rapport = (int)rapportOutput.getRapportScore();
        userCS = rapportOutput.getUserStrategy();
        socialController.setRapportScore(rapport);
        socialController.setUserConvStrategy(userCS);
        socialController.addContinousStates(null);

        //System.out.println("---------------- Updated Rapport Score : " + rapportOutput.getRapportScore());
        //System.out.println("---------------- Updated User Strategy : " + rapportOutput.getUserStrategy());

    }

    private void updateNVB(){
        NonVerbalOutput nvbOutput = (NonVerbalOutput) blackboard().get(SaraCons.MSG_NVB);
        isGazing = nvbOutput.isGazeAtPartner();
        isSmiling = nvbOutput.isSmiling();

        socialController.setNonVerbals(isSmiling, isGazing);
        socialController.addContinousStates(null);
        //System.out.println("---------------- User is Smiling : " + isSmiling);
        //System.out.println("---------------- User is Gazing  : " + isGazing);
    }

    private SROutput selectStrategy(){
        long time = System.nanoTime();
        SROutput srOutput = new SROutput();

        DMOutput dmOutput = (DMOutput) blackboard().get(SaraCons.MSG_DM);
        srOutput.setAction(dmOutput.getAction());
        srOutput.setEntities(dmOutput.getEntities());
        srOutput.setRecommendation(dmOutput.getRecommendation());
        srOutput.setUserFrame(dmOutput.getUserFrame());
        srOutput.setRapport(rapport);

        if (dmOutput.getAction()!=null && socialController!=null) {
            SystemIntent systemIntent =  new SystemIntent( );
            systemIntent.setIntent(dmOutput.getAction());
            systemIntent.setEntities(dmOutput.getEntities());
            systemIntent.setRecommendationResults( Utils.toJson(dmOutput.getRecommendation()));
            socialController.addSystemIntent( systemIntent );
            systemStrategy = socialController.getConvStrategyFormatted();
            srOutput.setStrategy(systemStrategy);

//            VhmsgSender sender = new VhmsgSender("vrSocialReasonerScore");

            SocialReasonerOutput output = new SocialReasonerOutput();
            output.setActivations(socialController.getSocialReasoner().getNetwork().getOnlyActivations());
            output.setNames(socialController.getSocialReasoner().getNetwork().getModuleNames());
            output.setThreshold(socialController.getSocialReasoner().getNetwork().getTheta());

            String json = Utils.toJson(output);

//            sender.sendMessage("0 " + json);
            systemStrategy = socialController.getConvStrategyFormatted();
            srOutput.setStrategy(systemStrategy);
            //System.out.println("---------------- System Strategy : " + systemStrategy);
            Log4J.info(this, "Input: " + dmOutput.getAction() + ", Output: " + srOutput.getStrategy() + "\n");
        } else {
            System.out.println("null");
        }

        return srOutput;
    }

    /*
     * If the blackboard model is modified externally, does SR have to do anything? this is useful when running multiple
     * processes in parallel rather than sequentially.
     */
    @Override
    public void onEvent(BlackboardEvent event) {
        //TODO: add code here
        //...
        //Log4J.info(this, "SocialReasonerComponent. These objects have been updated at the blackboard: " + event.toString());

        if (event.getId()==SaraCons.MSG_NVB) {
            //System.out.println(" ###################### Message from OpenFace");
            updateNVB();
        }
        if (event.getId()==SaraCons.MSG_RPT) {
            //updateRapport();
        }
        if (event.getId().equals(SaraCons.MSG_NVB)) {
            updateNVB();
        }
        if (event.getId().equals(SaraCons.MSG_RPT)) {
            updateRapport();
        }
        if (event.getId().equals(SaraCons.MSG_DM)) {
            sendToNLG = selectStrategy();
            blackboard().post(this, SaraCons.MSG_SR, sendToNLG);
        }

    }

    @Override
    public void shutDown() {
        super.shutDown();
        // TODO: add code to release resources
    }
}
