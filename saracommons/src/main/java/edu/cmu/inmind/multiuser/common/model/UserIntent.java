package edu.cmu.inmind.multiuser.common.model;

import java.util.List;

/**
 * Created by oscarr on 3/16/17.
 */
public class UserIntent {
    private String userIntent;
    private List<String> entitities;

    public UserIntent(String userIntent, List<String> entitities) {
        this.userIntent = userIntent;
        this.entitities = entitities;
    }

    public String getUserIntent() {
        return this.userIntent;
    }

    public void setUserIntent(String userIntent) {
        this.userIntent = userIntent;
    }

    public List<String> getEntitities() {
        return this.entitities;
    }

    public void setEntitities(List<String> entitities) {
        this.entitities = entitities;
    }


    @Override
    public String toString() {
        return "Component: " + this.getClass().toString() + " userIntent: "+ userIntent +
                " entities: " + entitities.toString();
    }
}
