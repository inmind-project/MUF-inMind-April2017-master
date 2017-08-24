package edu.cmu.inmind.multiuser.sara.component;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import edu.cmu.inmind.multiuser.common.Constants;
import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.model.SROutput;
import edu.cmu.inmind.multiuser.common.model.UserModel;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StateType;
import edu.cmu.inmind.multiuser.controller.session.Session;
import edu.cmu.inmind.multiuser.sara.repo.UserModelRepository;
import edu.cmu.inmind.multiuser.socialreasoner.control.util.Utils;

import java.util.Map;
import java.util.function.Consumer;

/**
 * ** Work in progress **
 *
 * This component keeps track of all relevant data about the user & interactions we have had with her.
 * This includes both a semantic memory (e.g. user preferences) and a episodic memory (e.g. rapport)
 *
 * This data is persisted across sessions
 *
 * Furthermore, this component will support replacing the current model with a preset user profile
 */
@StateType(state = Constants.STATEFULL)
@BlackboardSubscription(messages = {SaraCons.MSG_SR, SaraCons.MSG_START_SESSION})
public class UserModelComponent extends PluggableComponent {

    private Map<String, Consumer<BlackboardEvent>> delegationMap;
    private UserModelRepository repository;
    private UserModel userModel;

    @Override
    protected void startUp() {
        super.startUp();
        Log4J.info(this, "Initializing user model component");
        // TODO: Add additional data from various events (SR, DM, etc.)
        delegationMap = ImmutableMap.of(
                SaraCons.MSG_SR, this::handleMsgSR,
                SaraCons.MSG_START_SESSION, this::onStartSession
        );
        repository = createRepo();
        userModel = repository.readModel()
                .orElseGet(() -> new UserModel(getSessionId()));

        Log4J.info(this, "Loaded user model: " + Utils.toJson(userModel));

        blackboard().post(this, SaraCons.MSG_USER_MODEL_LOADED, userModel);
    }

    @Override
    public void onEvent(BlackboardEvent event) {
        if (delegationMap.containsKey(event.getId())) {
            delegationMap.get(event.getId()).accept(event);
            Log4J.info(this,event.toString());
        } else {
            Log4J.error(this, "Received unrecognized event: " + event.getId());
        }
    }

    private void onStartSession(BlackboardEvent event)
    {
        // TODO: Check event to see if we should clear the user history
    }

    private void handleMsgSR(BlackboardEvent event)
    {
        final SROutput srOutput = (SROutput) event.getElement();
        userModel.updateBehaviorNetworkStates(srOutput.getStates());
        userModel.setUserFrame(srOutput.getUserFrame());
        // TODO: Only write in shutDown once the app is updated to properly send REQUEST_DISCONNECT
        Log4J.info(this, "Writing user model: " + Utils.toJson(userModel));
        repository.writeModel(userModel);
    }

    @Override
    public void shutDown(){
        Log4J.info(this, "shutting down UserModelComponent");
        repository.writeModel(userModel);
        super.shutDown();
    }

    @VisibleForTesting
    UserModelRepository createRepo() {
        try {
            final Session session = getSession();
            return new UserModelRepository(session.getConfig().getPathLogs(), session.getId());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        // The session should always be set at this point
        throw new IllegalStateException("Tried to start UserModelComponent with uninitialized session");
    }
}
