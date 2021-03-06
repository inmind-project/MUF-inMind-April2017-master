package edu.cmu.inmind.multiuser.sara.component;

import java.util.Collections;
import java.util.List;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.model.DMOutput;
import edu.cmu.inmind.multiuser.common.model.Recommendation;
import edu.cmu.inmind.multiuser.common.model.Rexplanation;
import edu.cmu.inmind.multiuser.common.model.SaraInput;
import edu.cmu.inmind.multiuser.common.model.SaraOutput;
import edu.cmu.inmind.multiuser.common.model.UserFrame;
import edu.cmu.inmind.multiuser.controller.blackboard.Blackboard;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.common.Constants;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StateType;

/**
 * Created by oscarr on 3/7/17.
 */

//@BlackboardSubscription(messages = {SaraCons.MSG_NLU, SaraCons.MSG_START_SESSION})
@StateType( state = Constants.STATEFULL)
@BlackboardSubscription(messages = {SaraCons.MSG_NLU, SaraCons.MSG_DIALOGUE_RESPONSE, SaraCons.MSG_START_DM})
public class FakeTaskReasonerComponent extends PluggableComponent {

    private SaraInput saraInput;
    private SaraOutput saraOutput;
    private int cpt;

    @Override
    public void startUp() {
        super.startUp();
        cpt = 1;
        // TODO: add code to initialize this component
    }

    @Override
    public void execute() {
        Log4J.info(this, "FakeTaskReasonerComponent: " + hashCode());

    }

    private DMOutput sendToSR() {
        DMOutput dmOutput = new DMOutput();

        if (cpt == 0) {
            dmOutput.setAction("greeting");
            cpt++;
        } else if (cpt == 1) {
            dmOutput.setAction("ask_genres");
            cpt++;
        } else if (cpt == 2) {
            dmOutput.setAction("ask_directors");
            cpt++;
        } else if (cpt == 3) {
            dmOutput.setAction("ask_actors");
            cpt++;
        } else if (cpt == 4) {
            setFakeMovie(dmOutput, "Jack Reacher");
            dmOutput.setAction("recommend");
            cpt++;
        } else if (cpt == 5) {
            setFakeMovie(dmOutput, "Mission Impossible");
            dmOutput.setAction("recommend");
            cpt++;
        } else if (cpt == 6) {
            setFakeMovie(dmOutput, "Edge of Tomorrow");
            dmOutput.setAction("recommend");
            cpt++;
        } else if (cpt == 7) {
            dmOutput.setAction("goodbye");
        }
        dmOutput.setFrame(new UserFrame());

        Log4J.info(this, "TaskReasoner: MSG_DM " + dmOutput.toString());
        return dmOutput;
    }

    private void setFakeMovie(DMOutput output, String movieTitle){
        Recommendation reco = new Recommendation();
        Rexplanation movie = new Rexplanation(movieTitle, Collections.singletonList("because duh"));
        List<Rexplanation> list = Collections.singletonList(movie);
        reco.setRexplanations(list);
        output.setRecommendation(reco);
    }

    /**
     * If the blackboard model is modified externally, does TR have to do anything? this is useful when running multiple
     * processes in parallel rather than sequentially.
     */
    @Override
    public void onEvent(Blackboard blackboard, BlackboardEvent event) throws Throwable

    {
        //TODO: add code here
        //...
        Log4J.info(this, "FakeTaskReasonerComponent. These objects have been updated at the blackboard: " + event.toString());
        //Log4J.info(this, "Input: " + saraInput.getASRinput() + " Output: " + saraOutput.getSystemIntent() );
        if (event.getId().equals(SaraCons.MSG_START_SESSION)) {
		    System.out.println("Fake DM is initiating dialogue");
	    }
	    blackboard.post(this, SaraCons.MSG_DM, sendToSR());
    }

    @Override
    public void shutDown() {
        super.shutDown();
        // TODO: add code to release resources
    }
}
