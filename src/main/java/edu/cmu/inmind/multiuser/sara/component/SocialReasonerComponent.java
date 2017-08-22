package edu.cmu.inmind.multiuser.sara.component;

import edu.cmu.inmind.multiuser.common.Constants;
import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.model.*;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.log.Loggable;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StateType;
import edu.cmu.inmind.multiuser.socialreasoner.control.MainController;
import edu.cmu.inmind.multiuser.socialreasoner.control.util.Utils;
import edu.cmu.inmind.multiuser.socialreasoner.model.SocialReasonerOutput;
import edu.cmu.inmind.multiuser.socialreasoner.model.intent.SystemIntent;
//import edu.cmu.lti.rapport.pipline.csc.ConversationalStrategyDistribution;

/**
 * Created by oscarr on 3/7/17.
 */

@StateType( state = Constants.STATEFULL)
@BlackboardSubscription( messages = {SaraCons.MSG_DM, SaraCons.MSG_RPT, SaraCons.MSG_CSC, SaraCons.MSG_NVB,
        SaraCons.MSG_USER_MODEL_LOADED})
public class SocialReasonerComponent extends PluggableComponent {

    private MainController socialController;
    String systemStrategy = "";
    private SROutput sendToNLG;
    private int rapport=4;
    private String userCS="";
    private boolean isSmiling;
    private boolean isGazing;

    @Loggable
    @Override
    public void startUp(){
        super.startUp();
        //Create a new thread for the social reasoner
        socialController = new MainController();
    }

    @Loggable
    @Override
    public void execute() {
        Log4J.info(this, "SocialReasonerComponent: " + hashCode());

    }


    private void updateRapport(){
        RapportOutput rapportOutput = edu.cmu.inmind.multiuser.common.Utils.fromJson(
                (String) blackboard().get(SaraCons.MSG_RPT), RapportOutput.class );

        rapport = (int)rapportOutput.getRapportScore();
        socialController.setRapportScore(rapport);
        socialController.addContinousStates(null);

        Log4J.info(this,"RapporEstimator : " + rapport );
    }

    private void updateStrategy(){
        CSCOutput csc = (CSCOutput) blackboard().get(SaraCons.MSG_CSC);
        userCS = csc.getBest().getName();
        Log4J.info(this,"User's strategy updated : " + userCS );
        socialController.setUserConvStrategy(userCS);
        socialController.addContinousStates(null);
    }

    private void updateNVB(){
        NonVerbalOutput nvbOutput = (NonVerbalOutput) blackboard().get(SaraCons.MSG_NVB);
        isGazing = nvbOutput.isGazeAtPartner();
        isSmiling = nvbOutput.isSmiling();

        socialController.setNonVerbals(isSmiling, isGazing);
        socialController.addContinousStates(null);
     }

    private void updateUserModel(final UserModel model) {
        Log4J.info(this, "Received user model");
        if (!model.getBehaviorNetworkStates().isEmpty()) {
            Log4J.info(this, "Updating states: " + Utils.toJson(model.getBehaviorNetworkStates()));
            socialController.getSocialReasoner().getNetwork().updateState(model.getBehaviorNetworkStates());
        } else {
            Log4J.info(this, "States were empty");
        }
    }

    private SROutput selectStrategy(){
        long time = System.nanoTime();
        SROutput srOutput = new SROutput();

        DMOutput dmOutput = (DMOutput) blackboard().get(SaraCons.MSG_DM);
        Log4J.info(this,"dmOutput : "+dmOutput.toString() );
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
            srOutput.setStates(socialController.getSocialReasoner().getNetwork().getState());

            SocialReasonerOutput output = new SocialReasonerOutput();
            output.setActivations(socialController.getSocialReasoner().getNetwork().getOnlyActivations());
            output.setNames(socialController.getSocialReasoner().getNetwork().getModuleNames());
            output.setThreshold(socialController.getSocialReasoner().getNetwork().getTheta());

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
    @Loggable
    @Override
    public void onEvent(BlackboardEvent event) {
        //TODO: add code here
        //...
        Log4J.info(this, "SocialReasonerComponent. These objects have been updated at the blackboard: "
                + event.toString());

        if (event.getId().equals(SaraCons.MSG_NVB)) {
            updateNVB();
        }
        if (event.getId().equals(SaraCons.MSG_RPT)) {
            updateRapport();
        }
        if (event.getId().equals(SaraCons.MSG_CSC)) {
            updateStrategy();
        }
        if (event.getId().equals(SaraCons.MSG_USER_MODEL_LOADED)) {
            updateUserModel(((UserModel) event.getElement()));
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
