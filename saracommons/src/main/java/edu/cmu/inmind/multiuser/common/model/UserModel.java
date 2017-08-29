package edu.cmu.inmind.multiuser.common.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents data about the user that should be persisted between sessions
 */
public class UserModel {
    private final List<String> behaviorNetworkStates = new ArrayList<>();
    private UserFrame userFrame;

    private final String id;

    public UserModel(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public List<String> getBehaviorNetworkStates() {
        return behaviorNetworkStates;
    }

    public void updateBehaviorNetworkStates(final List<String> behaviorNetworkStates) {
        this.behaviorNetworkStates.clear();
        this.behaviorNetworkStates.addAll(behaviorNetworkStates);
    }

    public UserFrame getUserFrame() {
        return userFrame;
    }

    public void setUserFrame(final UserFrame userFrame) {
        this.userFrame = userFrame;
    }
}
