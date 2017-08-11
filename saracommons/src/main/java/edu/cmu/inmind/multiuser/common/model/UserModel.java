package edu.cmu.inmind.multiuser.common.model;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.List;

/**
 * This class represents data about the user that should be persisted between sessions
 */
public class UserModel {
    @NotNull private final String id;

    @Nullable private List<String> socialReasonerStates;

    public UserModel(@NotNull String id) {
        this.id = id;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @Nullable
    public List<String> getSocialReasonerStates() {
        return socialReasonerStates;
    }

    public void setSocialReasonerStates(@Nullable final List<String> socialReasonerStates) {
        this.socialReasonerStates = socialReasonerStates;
    }
}
