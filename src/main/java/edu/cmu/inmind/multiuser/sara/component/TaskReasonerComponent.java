package edu.cmu.inmind.multiuser.sara.component;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.Utils;
import edu.cmu.inmind.multiuser.common.model.DMOutput;
import edu.cmu.inmind.multiuser.common.model.SaraInput;
import edu.cmu.inmind.multiuser.common.model.SaraOutput;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StatefulComponent;

import java.util.Random;

/**
 * Created by oscarr on 3/7/17.
 */
@StatefulComponent
//@BlackboardSubscription(messages = {SaraCons.MSG_NLU})
@BlackboardSubscription(messages = {SaraCons.MSG_NLU, SaraCons.MSG_DIALOGUE_RESPONSE})
public class TaskReasonerComponent extends PluggableComponent {

    SaraInput saraInput;
    SaraOutput saraOutput;

    @Override
    public void startUp() {
        super.startUp();
        // TODO: add code to initialize this component
    }

    @Override
    public void execute() {
        Log4J.info(this, "TaskReasonerComponent: " + hashCode());

    }

    private DMOutput sendToSR() {
        DMOutput dmOutput = new DMOutput();

        Random random = new Random();
        double value = 1 + (5 - 1) * random.nextDouble();

        if (value < 2) {
            dmOutput.setAction("goodbye");
        } else if (value < 3) {
            dmOutput.setAction("greeting");
        } else if (value < 4) {
            dmOutput.setAction("ask_genres");
        } else if (value < 5) {
            dmOutput.setAction("explicit_confirm");
        }

        return dmOutput;
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
        blackboard().post(this, SaraCons.MSG_DM, sendToSR());
    }

    @Override
    public void shutDown() {
        super.shutDown();
        // TODO: add code to release resources
    }
}
