package edu.cmu.inmind.multiuser.sara.component;

import edu.cmu.inmind.multiuser.common.Constants;
import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.Utils;
import edu.cmu.inmind.multiuser.common.model.R5StreamListener;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StateType;

/**
 * Created by sakoju on 6/7/17.
 */
@StateType( state = Constants.STATEFULL)
@BlackboardSubscription(messages = SaraCons.MSG_START_SESSION)
public class R5StreamComponent extends PluggableComponent {
private R5StreamListener r5StreamListener = null;

    @Override
    public void startUp(){
        super.startUp();
//        postCreate();
//        postCreateblackboard().post(this, SaraCons.MSG_ASR, "Hello");
        //blackboard().post(this, SaraCons.MSG_ASR, "Hello");
        r5StreamListener = new R5StreamListener();
        r5StreamListener.setStreamingStatus(SaraCons.MSG_START_STREAMING);
        r5StreamListener.setRed5StreamingUrl(Utils.getProperty("streamingURL"));
        Log4J.info(this, "NLU_DMComponent: startup has finished.");
    }

    @Override
    public void onEvent(BlackboardEvent blackboardEvent) throws Throwable
    {
        if(blackboardEvent.getId().equals(SaraCons.MSG_START_SESSION))
        {
            blackboard().post(R5StreamComponent.this, SaraCons.MSG_START_STREAMING, r5StreamListener);
        }
    }

    @Override
    public void shutDown() {
        super.shutDown();
        // TODO: add code to release resources
    }
}
