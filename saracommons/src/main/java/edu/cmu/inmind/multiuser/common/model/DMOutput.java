package edu.cmu.inmind.multiuser.common.model;

import java.util.List;

/**
 * Created by yoichimatsuyama on 4/11/17.
 */
public class DMOutput {
    String action;
    List<Entity> entities;
    Recommendation recommendation;
    UserFrame userFrame;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    public Recommendation getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(Recommendation recommendation) {
        this.recommendation = recommendation;
    }

    public UserFrame getUserFrame() {
        return userFrame;
    }

    public void setUserFrame(UserFrame userFrame) {
        this.userFrame = userFrame;
    }

    public String toString(){
        return "Component: " + this.getClass().toString() + " System Action: " + action;
    }
}
