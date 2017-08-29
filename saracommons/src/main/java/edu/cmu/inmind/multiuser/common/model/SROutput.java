package edu.cmu.inmind.multiuser.common.model;

import java.util.List;

/**
 * Created by yoichimatsuyama on 4/11/17.
 */
public class SROutput {
    String action;
    String strategy;
    int rapport;
    List<Entity> entities;
    Recommendation recommendation;
    UserFrame userFrame;
    List<String> states;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public int getRapport() {
        return rapport;
    }

    public void setRapport(int rapport) {
        this.rapport = rapport;
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

    public List<String> getStates() {
        return states;
    }

    public void setStates(final List<String> states) {
        this.states = states;
    }

    public String toString(){
        return "Component: " + this.getClass().toString() + " System Action: " + action + " Strategy:" + strategy;
    }
}
