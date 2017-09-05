package edu.cmu.inmind.multiuser.common.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents data about the user that should be persisted between sessions
 */
public class UserModel {
    public static final double RAPPORT_UNDEFINED = 0.0;

    private final List<String> behaviorNetworkStates = new ArrayList<>();
    private UserFrame userFrame;
    private double rapport = RAPPORT_UNDEFINED;

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
        if (behaviorNetworkStates != null) {
            this.behaviorNetworkStates.clear();
            this.behaviorNetworkStates.addAll(behaviorNetworkStates);
        }
    }

    public UserFrame getUserFrame() {
        return userFrame;
    }

    public void setUserFrame(final UserFrame userFrame) {
        this.userFrame = userFrame;
    }

    public double getRapport() {
        return rapport;
    }

    public void setRapport(final double rapport) {
        this.rapport = rapport;
    }

    /**
     * If a field is not present in a json object, GSON will overwrite it with null even if it is final.
     * Use this method to ensure that the object is in the expected state.
     */
    @SuppressWarnings("ConstantConditions")
    public boolean isValid() {
        return id != null && !id.isEmpty() &&
                 behaviorNetworkStates != null;
    }
}
