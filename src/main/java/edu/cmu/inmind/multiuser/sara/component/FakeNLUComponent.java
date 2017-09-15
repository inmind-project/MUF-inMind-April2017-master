package edu.cmu.inmind.multiuser.sara.component;

import edu.cmu.inmind.multiuser.common.Constants;
import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.model.SaraOutput;
import edu.cmu.inmind.multiuser.common.model.UserIntent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StateType;

import java.util.ArrayList;

/**
 * Created by oscarr on 3/7/17.
 */
@StateType( state = Constants.STATELESS)
@BlackboardSubscription( messages = {SaraCons.MSG_ASR})
public class FakeNLUComponent extends PluggableComponent {

    @Override
    public void startUp(){
        super.startUp();
        // TODO: add code to initialize this component
    }

    @Override
    public void execute() {
        Log4J.info(this, "FakeNLUComponent: " + hashCode());

    }


    private SaraOutput extractAndProcess() {

        SaraOutput saraOutput = new SaraOutput();
        saraOutput.setUserIntent( new UserIntent( "user-intent", new ArrayList<>() ) );
        //Log4J.info(this, "Input: " + saraInput + ", Output: " + saraOutput + "\n");
        return saraOutput;
    }

    /**
     * If the blackboard model is modified externally, does FakeNLUComponent have to do anything? this is useful when running multiple
     * processes in parallel rather than sequentially.
     */
    @Override
    public void onEvent(BlackboardEvent event) throws Exception
    {
        //TODO: add code here
        //...
        Log4J.info(this, "FakeNLUComponent. These objects have been updated at the blackboard: " + event.toString());

        SaraOutput saraOutput = extractAndProcess();

        //blackboard().post(this, SaraCons.MSG_StartOpenFace, "rtsp://34.203.204.136:8554/live/myStream54201342a4cfb96d");
        blackboard().post(this, SaraCons.MSG_NLU, saraOutput );

        //TODO: uncomment this code to run Ex13_UserModel and Ex15_WholePipeline
//        saraOutput.getUserIntent().setUserIntent("user-interests");
//        List<String> entities = Arrays.asList(new String[]{"robotics", "IA", "cooking"});
//        saraOutput.getUserIntent().setEntitities( entities );
//        blackboard().post(this, SaraCons.MSG_NLU, saraOutput );
    }

    @Override
    public void shutDown() {
        super.shutDown();
        // TODO: add code to release resources
    }
}
