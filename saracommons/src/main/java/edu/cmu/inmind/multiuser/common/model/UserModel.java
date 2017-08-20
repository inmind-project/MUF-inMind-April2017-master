package edu.cmu.inmind.multiuser.common.model;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents data about the user that should be persisted between sessions
 */
public class UserModel {
    private final List<String> behaviorNetworkStates = new ArrayList<>();
    @Nullable private UserFrame userFrame;

    @NotNull private final String id;

    public UserModel(@NotNull String id) {
        this.id = id;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public List<String> getBehaviorNetworkStates() {
        return behaviorNetworkStates;
    }

    public void updateBehaviorNetworkStates(@Nullable final List<String> behaviorNetworkStates) {
        this.behaviorNetworkStates.clear();
        this.behaviorNetworkStates.addAll(behaviorNetworkStates);
    }

    @Nullable
    public UserFrame getUserFrame() {
        return userFrame;
    }

    public void setUserFrame(@Nullable final UserFrame userFrame) {
        this.userFrame = userFrame;
    }
}
