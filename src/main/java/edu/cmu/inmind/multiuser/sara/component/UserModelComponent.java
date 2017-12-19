package edu.cmu.inmind.multiuser.sara.component;

import com.google.common.annotations.VisibleForTesting;
import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.model.SROutput;
import edu.cmu.inmind.multiuser.common.model.UserModel;
import edu.cmu.inmind.multiuser.controller.blackboard.Blackboard;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.common.Constants;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StateType;
import edu.cmu.inmind.multiuser.controller.session.Session;
import edu.cmu.inmind.multiuser.sara.repo.UserModelRepository;
import edu.cmu.inmind.multiuser.sara.util.UserModelResetter;
import edu.cmu.inmind.multiuser.socialreasoner.control.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * This component keeps track of all relevant data about the user & interactions we have had with her.
 * This includes both a semantic memory (e.g. user preferences) and a episodic memory (e.g. rapport)
 * <p>
 * This data is persisted across sessions
 */
@StateType(state = Constants.STATEFULL)
@BlackboardSubscription(messages = {SaraCons.MSG_SR, SaraCons.MSG_START_SESSION, SaraCons.MSG_START_DM})
public class UserModelComponent extends PluggableComponent {

    private UserModelRepository repository;
    private UserModel userModel;

    @Override
    public void onEvent(Blackboard blackboard, BlackboardEvent event) throws Throwable {
        final String eventId = event.getId();
        if (SaraCons.MSG_START_SESSION.equals(eventId)) {
            onStartSession(event, blackboard);
        } else if (SaraCons.MSG_START_DM.equals(eventId) && userModel == null) {
            // The conversation is starting. Make sure we're initialized
            // Temporary work-around for issue where START_SESSION isn't sent
            initializeUserModel(blackboard, "");
        } else if (userModel != null) {
            switch (eventId) {
                case SaraCons.MSG_SR: {
                    handleMsgSR((SROutput) event.getElement());
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

    private void onStartSession(BlackboardEvent event, Blackboard blackboard) {
        final String resetOption = event.getElement() instanceof String ? (String) event.getElement() : "";
        initializeUserModel(blackboard, resetOption);
    }

    private void initializeUserModel(final Blackboard blackboard, final String resetOption) {
        Log4J.info(this, "Initializing user model component");

        repository = createRepo();

        final Optional<UserModel> userModelOpt = repository.readModel();
        userModelOpt.ifPresent(model -> UserModelResetter.reset(model, resetOption));
        userModel = userModelOpt.orElseGet(() -> new UserModel(getSessionId()));

        Log4J.info(this, "Loaded user model: " + Utils.toJson(userModel));

        blackboard.post(this, SaraCons.MSG_UM, userModel);
    }

    /**
     * The social reasoner is where the task and social pipelines converge so it contains all relevant information.
     * Extract and store it in the user model.
     */
    private void handleMsgSR(SROutput srOutput) {
        // Sometimes the dialog manager will bypass the social reasoner.
        // In this case, it outputs a fake SROutput that doesn't contain social reasoner states
        if (srOutput.getStates() != null) {
            final List<String> states = new ArrayList<>(srOutput.getStates());
            // The social reasoner states include the current stage of the dialog. We do not want to store this
            // across interactions as it is not expected to stay the same. Remove it from the list
            states.remove(srOutput.getDMOutput().getAction());
            userModel.updateBehaviorNetworkStates(states);
            userModel.setRapport(srOutput.getRapport());
        }
        userModel.setUserFrame(srOutput.getDMOutput().getUserFrame());
        repository.writeModel(userModel);
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

}