package edu.cmu.inmind.multiuser.sara.component;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.Utils;
import edu.cmu.inmind.multiuser.common.model.R5StreamListener;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StatefulComponent;

/**
 * Created by sakoju on 6/7/17.
 */
@StatefulComponent
@BlackboardSubscription(messages = "MSG_START_SESSION")
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
    public void onEvent(BlackboardEvent blackboardEvent)
    {
        if(blackboardEvent.getId().equals("MSG_START_SESSION"))
        {
            blackboard().post(R5StreamComponent.this, SaraCons.MSG_START_STREAMING, r5StreamListener);
        }
    }
}
