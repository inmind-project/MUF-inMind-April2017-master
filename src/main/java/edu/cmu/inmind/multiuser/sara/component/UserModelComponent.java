package edu.cmu.inmind.multiuser.sara.component;

import com.google.common.annotations.VisibleForTesting;
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
@BlackboardSubscription(messages = {SaraCons.MSG_DM, SaraCons.MSG_SR})
public class UserModelComponent extends PluggableComponent {

    @SuppressWarnings("NullableProblems") // Initialized in #startUp()
    @NotNull private UserModelRepository repository;
    @SuppressWarnings("NullableProblems") // Initialized in #startUp()
    @NotNull private UserModel userModel;

    @Override
    public void startUp() {
        super.startUp();
        Log4J.info(this, "Reading userModel from disk");

        repository = createRepo();
        userModel = repository.readModel()
                .orElseGet(() -> new UserModel(getSessionId()));

        // TODO: Update other components when user model loaded
    }

    @Override
    public void onEvent(@NotNull BlackboardEvent event) {
        // TODO: Add additional data from various events (SR, DM, etc.)
        switch(event.getId()) {
            case SaraCons.MSG_SR: {
                final SROutput srOutput = (SROutput) event.getElement();
                userModel.updateBehaviorNetworkStates(srOutput.getStates());
                break;
            }
            default: {
                break;
            }
        }
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
