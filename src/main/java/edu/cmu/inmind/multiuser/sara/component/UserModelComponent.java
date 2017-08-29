package edu.cmu.inmind.multiuser.sara.component;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.cmu.inmind.multiuser.common.Constants;
import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.model.SROutput;
import edu.cmu.inmind.multiuser.common.model.UserFrame;
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
import java.util.Optional;
import java.util.function.Consumer;

/**
 * ** Work in progress **
 *
 * This component keeps track of all relevant data about the user & interactions we have had with her.
 * This includes both a semantic memory (e.g. user preferences) and a episodic memory (e.g. rapport)
 *
 * This data is persisted across sessions
 */
@StateType(state = Constants.STATEFULL)
@BlackboardSubscription(messages = {SaraCons.MSG_SR, SaraCons.MSG_START_SESSION})
public class UserModelComponent extends PluggableComponent {

    @VisibleForTesting
    static final class ResetOptions {
        static final String EPISODIC = "RESET_USER_RAPPORT_HISTORY";
        static final String SEMANTIC = "RESET_USER_CONTENT_HISTORY";
        static final String ALL = "RESET_USER_ALL_HISTORY";
    }

    private static final Consumer<UserModel> resetEpisodicState = UserModelComponent::resetEpisodicState;
    private static final Consumer<UserModel> resetSemanticState = UserModelComponent::resetSemanticState;
    private static final Consumer<UserModel> resetAll = resetEpisodicState.andThen(resetSemanticState);

    private static final Map<String, Consumer<UserModel>> USER_MODEL_RESETTERS = ImmutableMap.of(
            ResetOptions.EPISODIC, resetEpisodicState,
            ResetOptions.SEMANTIC, resetSemanticState,
            ResetOptions.ALL, resetAll
    );

    private static void resetSemanticState(final UserModel model) {
        if (model.getUserFrame() != null) {
            model.getUserFrame().setFrame(new UserFrame.Frame());
        }
    }

    private static void resetEpisodicState(final UserModel model) {
        model.updateBehaviorNetworkStates(ImmutableList.of());
    }

    private UserModelRepository repository;
    private UserModel userModel;

    @Override
    public void onEvent(BlackboardEvent event) throws Exception {
        final String eventId = event.getId();
        if (SaraCons.MSG_START_SESSION.equals(eventId)) {
            onStartSession(event);
        } else if (userModel != null) {
            switch (eventId) {
                case SaraCons.MSG_SR: {
                    handleMsgSR(event);
                    break;
                }
                default: {
                    Log4J.error(this, "Received unrecognized event: " + eventId);
                    break;
                }
            }
        } else {
            Log4J.error(this, "Received " + eventId + " before initializing user model");
        }
    }

    private void onStartSession(BlackboardEvent event) {
        Log4J.info(this, "Initializing user model component");
        repository = createRepo();

        final String setting = event.getElement() instanceof String ? (String) event.getElement() : "";
        userModel = getUserModel(setting, repository).orElseGet(() -> new UserModel(getSessionId()));

        Log4J.info(this, "Loaded user model: " + Utils.toJson(userModel));

        blackboard().post(this, SaraCons.MSG_USER_MODEL_LOADED, userModel);
    }

    private void handleMsgSR(BlackboardEvent event)
    {
        final SROutput srOutput = (SROutput) event.getElement();
        userModel.updateBehaviorNetworkStates(srOutput.getStates());
        userModel.setUserFrame(srOutput.getUserFrame());
    }

    @Override
    public void shutDown() {
        if (userModel != null) {
            Log4J.info(this, "Writing user model: " + Utils.toJson(userModel));
            repository.writeModel(userModel);
        }
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

    private static Optional<UserModel> getUserModel(final String setting, final UserModelRepository repository) {
        final Optional<UserModel> result = repository.readModel();
        result.ifPresent(USER_MODEL_RESETTERS.getOrDefault(setting, __ -> {}));
        return result;
    }
}
