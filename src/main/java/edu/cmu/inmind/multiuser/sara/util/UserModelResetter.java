package edu.cmu.inmind.multiuser.sara.util;

import java.util.Map;
import java.util.function.Consumer;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.cmu.inmind.multiuser.common.model.UserFrame;
import edu.cmu.inmind.multiuser.common.model.UserModel;

/**
 * When a client connects, it can send a reset option as part of the START_SESSION message.
 * This class handles resetting the user model based on the supplied option, clearing the episodic memory,
 * semantic memory, or both.
 */
public class UserModelResetter {
    @VisibleForTesting
    public static final class ResetOptions {
        public static final String EPISODIC = "RESET_USER_RAPPORT_HISTORY";
        public static final String SEMANTIC = "RESET_USER_CONTENT_HISTORY";
        public static final String ALL = "RESET_USER_ALL_HISTORY";
    }

    private static final Consumer<UserModel> resetEpisodicState = model -> {
        model.updateBehaviorNetworkStates(ImmutableList.of());
        model.setRapport(UserModel.RAPPORT_UNDEFINED);
    };
    private static final Consumer<UserModel> resetSemanticState = model -> {
        if (model.getUserFrame() != null) {
            model.setUserFrame(new UserFrame());
        }
    };

    private static final Map<String, Consumer<UserModel>> RESETTERS = ImmutableMap.of(
            ResetOptions.EPISODIC, resetEpisodicState,
            ResetOptions.SEMANTIC, resetSemanticState,
            ResetOptions.ALL, resetEpisodicState.andThen(resetSemanticState)
    );

    private UserModelResetter() {}

    public static void reset(UserModel model, String resetOption) {
        if (RESETTERS.containsKey(resetOption)) {
            RESETTERS.get(resetOption).accept(model);
        }
    }
}
