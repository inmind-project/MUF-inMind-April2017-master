package edu.cmu.inmind.multiuser.sara.component;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.model.*;
import edu.cmu.inmind.multiuser.controller.blackboard.Blackboard;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.common.Constants;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.log.Loggable;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StateType;
import edu.cmu.inmind.multiuser.socialreasoner.control.SocialReasonerController;
import edu.cmu.inmind.multiuser.socialreasoner.control.util.Utils;
import edu.cmu.inmind.multiuser.socialreasoner.model.intent.SystemIntent;
//import edu.cmu.lti.rapport.pipline.csc.ConversationalStrategyDistribution;

/**
 * Created by oscarr on 3/7/17.
 */

@StateType( state = Constants.STATEFULL)
@BlackboardSubscription( messages = {SaraCons.MSG_DM, SaraCons.MSG_RPT, SaraCons.MSG_CSC, SaraCons.MSG_NVB,
        SaraCons.MSG_UM})
public class SocialReasonerComponent extends PluggableComponent {

    private final SocialReasonerController socialController = new SocialReasonerController();
    String systemStrategy = "";
    private SROutput sendToNLG;
    private double rapport=4.0;
    private String userCS="";
    private boolean isSmiling;
    private boolean isGazing;

    @Loggable
    @Override
    public void execute() {
        Log4J.info(this, "SocialReasonerComponent: " + hashCode());
    }


    private void updateRapport(BlackboardEvent blackboardEvent){
        RapportOutput rapportOutput = null;
        try {
            rapportOutput = Utils.fromJson(
                    (String) blackboardEvent.getElement(), RapportOutput.class );
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        if(rapportOutput!=null) {

            rapport = rapportOutput.getRapportScore();
            socialController.setRapportScore(rapport);
            socialController.addContinousStates(null);

            Log4J.info(this, "RapporEstimator : " + rapport);
        }
        else
        {
            Log4J.error(this, "RapporEstimator : RapportOutput is null");
        }
    }

    private void updateStrategy(BlackboardEvent event){
        CSCOutput csc = null;
        try {
            csc = (CSCOutput) event.getElement();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        if(csc!=null) {
            userCS = csc.getBest().getName();
            Log4J.info(this, "User's strategy updated : " + userCS);
            socialController.setUserConvStrategy(userCS);
            socialController.addContinousStates(null);
        }
         else
        {
            Log4J.error(this, "CSC: CSCOutput is null");
        }
    }

    private void updateNVB(BlackboardEvent blackboardEvent){
        NonVerbalOutput nvbOutput = null;
        try {
            nvbOutput = (NonVerbalOutput) blackboardEvent.getElement();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        if(nvbOutput!=null) {
            isGazing = nvbOutput.isGazeAtPartner();
            isSmiling = nvbOutput.isSmiling();

            socialController.setNonVerbals(isSmiling, isGazing);
            socialController.addContinousStates(null);
        }
        else
        {
            Log4J.error(this, "NVB : NVBOutput is null");
        }
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

    private SROutput selectStrategy(BlackboardEvent event){

        long time = System.nanoTime();
        SROutput srOutput =null;
                DMOutput dmOutput = null;
        try {
            dmOutput = (DMOutput) event.getElement();
                    //blackboard.get(SaraCons.MSG_DM);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        if(dmOutput!=null) {
            Log4J.info(this, "dmOutput : " + dmOutput.toString());
             srOutput = new SROutput(dmOutput);
            // temporary: fix this while fixing incremental system
            srOutput.setRapport(rapport);

            if (dmOutput.getAction() != null) {
                SystemIntent systemIntent = new SystemIntent();
                systemIntent.setIntent(dmOutput.getAction());
                //systemIntent.setRecommendationResults( Utils.toJson(dmOutput.getRecommendation()));
                socialController.addSystemIntent(systemIntent);
                systemStrategy = socialController.getConvStrategyFormatted();
                srOutput.setStrategy(systemStrategy);
                srOutput.setStates(socialController.getSocialReasoner().getNetwork().getState());

                //System.out.println("---------------- System Strategy : " + systemStrategy);
                Log4J.info(this, "Input: " + dmOutput.getAction() + ", Output: " + srOutput.getStrategy() + "\n");
            } else {
                System.out.println("null");
            }
        }
        else
        {
            Log4J.error(this, "SROutput : SROutput is null");
        }

        return srOutput;
    }

    /*
     * If the blackboard model is modified externally, does SR have to do anything? this is useful when running multiple
     * processes in parallel rather than sequentially.
     */
    @Loggable
    @Override
    public void onEvent(Blackboard blackboard, BlackboardEvent event) throws Throwable {
        //TODO: add code here
        //...
        Log4J.info(this, "SocialReasonerComponent. These objects have been updated at the blackboard: "
                + event.toString());
        if(blackboard==null)
        {
            Log4J.info(this, "blackboard is null");
        }
        if (event.getId().equals(SaraCons.MSG_NVB)) {
            updateNVB(event);
        }
        if (event.getId().equals(SaraCons.MSG_RPT)) {
            updateRapport(event);
        }
        if (event.getId().equals(SaraCons.MSG_CSC)) {
            updateStrategy(event);
        }
        if (event.getId().equals(SaraCons.MSG_UM)) {
            updateUserModel(((UserModel) event.getElement()));
        }
        if (event.getId().equals(SaraCons.MSG_DM)) {
            //if(blackboard!=null)
                sendToNLG = selectStrategy( event);

            blackboard.post(this, SaraCons.MSG_SR, sendToNLG);
        }

    }

    @Override
    public void shutDown() {
        super.shutDown();
        // TODO: add code to release resources
    }
}