package edu.cmu.inmind.multiuser.sara.component;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.model.*;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StatefulComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oscarr on 3/7/17.
 */
@StatefulComponent
//@BlackboardSubscription(messages = {SaraCons.MSG_NLU, "MSG_START_SESSION"})
@BlackboardSubscription(messages = {SaraCons.MSG_NLU, SaraCons.MSG_DIALOGUE_RESPONSE})
public class TaskReasonerComponent extends PluggableComponent {

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
        Log4J.info(this, "TaskReasonerComponent: " + hashCode());

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

        Log4J.info(this, "TaskReasoner: MSG_DM " + dmOutput.toString());
        return dmOutput;
    }

    private void setFakeMovie(DMOutput output, String movieTitle){
        Recommendation reco = new Recommendation();
        Rexplanation movie = new Rexplanation();
        List<Rexplanation> list = new ArrayList<Rexplanation>();
        list.add(movie);
        movie.setRecommendation(movieTitle);
        reco.setRexplanations(list);
        output.setRecommendation(reco);
    }

    /**
     * If the blackboard model is modified externally, does TR have to do anything? this is useful when running multiple
     * processes in parallel rather than sequentially.
     */
    @Override
    public void onEvent(BlackboardEvent event) {
        //TODO: add code here
        //...
        //Log4J.info(this, "TaskReasonerComponent. These objects have been updated at the blackboard: " + event.toString());
        //Log4J.info(this, "Input: " + saraInput.getASRinput() + " Output: " + saraOutput.getSystemIntent() );
        if (event.getId().equals("MSG_START_SESSION")) {
		System.out.println("Fake DM is initiating dialogue");
	    }
	blackboard().post(this, SaraCons.MSG_DM, sendToSR());
    }

    @Override
    public void shutDown() {
        super.shutDown();
        // TODO: add code to release resources
    }
}
