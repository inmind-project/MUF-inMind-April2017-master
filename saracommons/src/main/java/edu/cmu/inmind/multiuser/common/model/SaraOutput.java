package edu.cmu.inmind.multiuser.common.model;

/**
 * Created by oscarr on 3/3/17.
 */

import java.util.ArrayList;

public class SaraOutput {
    private VerbalOutput verbal;
    private NonVerbalOutput nonVerbal;
    private SocialIntent socialIntent;
    private String status = "";
    private UserIntent userIntent ;
    private String systemIntent;

    public SaraOutput() {
        verbal = new VerbalOutput("", "");
        nonVerbal = new NonVerbalOutput();
        socialIntent = new SocialIntent(0, "", "");
        userIntent = new UserIntent("", new ArrayList<String>());
    }

    @Override
    public String toString() {
        return "Component: " + this.getClass().toString() + " VerbalOutput: " + verbal.toString()
                + " NonVerbalOutput: " + nonVerbal.toString() + " SocialIntent: "+ socialIntent.toString()
                + " UserIntent: " + userIntent.toString() + " status: " + status + " systemIntent: " + systemIntent;
    }

    public VerbalOutput getVerbal() {
        return this.verbal;
    }

    public void setVerbal(VerbalOutput verbal) {
        this.verbal = verbal;
    }

    public NonVerbalOutput getNonVerbal() {
        return this.nonVerbal;
    }

    public void setNonVerbal(NonVerbalOutput nonVerbal) {
        this.nonVerbal = nonVerbal;
    }

    public SocialIntent getSocialIntent() {
        return this.socialIntent;
    }

    public void setSocialIntent(SocialIntent socialIntent) {
        this.socialIntent = socialIntent;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UserIntent getUserIntent() {
        return this.userIntent;
    }

    public void setUserIntent(UserIntent userIntent) {
        this.userIntent = userIntent;
    }

    public String getSystemIntent() {
        return this.systemIntent;
    }

    public void setSystemIntent(String systemIntent) {
        this.systemIntent = systemIntent;
    }
}
