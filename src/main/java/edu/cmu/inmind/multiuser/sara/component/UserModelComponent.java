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
import org.jetbrains.annotations.NotNull;

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
@BlackboardSubscription(messages = {SaraCons.MSG_DM, SaraCons.MSG_SR, SaraCons.MSG_START_SESSION})
public class UserModelComponent extends PluggableComponent {

    @SuppressWarnings("NullableProblems") // Initialized in #startUp()
    @NotNull private Map<String, Consumer<BlackboardEvent>> delegationMap;
    @SuppressWarnings("NullableProblems") // Initialized in #startUp()
    @NotNull private UserModelRepository repository;

    @SuppressWarnings("NullableProblems") // Initialized when session started
    @NotNull private UserModel userModel;

    @Override
    protected void startUp() {
        super.startUp();
        // TODO: Add additional data from various events (SR, DM, etc.)
        delegationMap = ImmutableMap.of(
                SaraCons.MSG_SR, this::handleMsgSR,
                SaraCons.MSG_START_SESSION, this::onStartSession
        );
        repository = createRepo();
    }

    @Override
    public void onEvent(@NotNull BlackboardEvent event) {
        if (delegationMap.containsKey(event.getId())) {
            delegationMap.get(event.getId()).accept(event);
        } else {
            Log4J.error(this, "Received unrecognized event: " + event.getId());
        }
    }

    private void onStartSession(@NotNull BlackboardEvent event) {
        // TODO: Check event to see if we should clear the user history
        userModel = repository.readModel()
                .orElseGet(() -> new UserModel(getSessionId()));

        blackboard().post(this, SaraCons.MSG_USER_MODEL_LOADED, userModel);
    }

    private void handleMsgSR(@NotNull BlackboardEvent event) {
        final SROutput srOutput = (SROutput) event.getElement();
        userModel.updateBehaviorNetworkStates(srOutput.getStates());
        userModel.setUserFrame(srOutput.getUserFrame());
    }

    @Override
    public void shutDown() {
        super.shutDown();
        Log4J.info(this, "Writing userModel to disk");
        repository.writeModel(userModel);
    }

    @VisibleForTesting
    @NotNull
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
