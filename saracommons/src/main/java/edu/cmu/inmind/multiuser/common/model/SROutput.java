package edu.cmu.inmind.multiuser.common.model;

import java.util.List;

/**
 * Created by yoichimatsuyama on 4/11/17.
 */
public class SROutput {
    DMOutput dmOutput;
    String strategy;
    double rapport;
    List<String> states;

    public SROutput(DMOutput dmOutput) { 
        this.dmOutput = dmOutput;
    }

    public String getAction() {
        return dmOutput.action;
    }
    public void setAction(String action) {
        dmOutput.action = action;
    }

    public String getStrategy() {
        return strategy;
    }
    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public double getRapport() {
        return rapport;
    }
    public void setRapport(double rapport) {
        this.rapport = rapport;
    }

    public List<Entity> getEntities() {
        return dmOutput.entities;
    }
    public void setEntities(List<Entity> entities) {
        dmOutput.entities = entities;
    }

    public DMOutput getDMOutput() { return dmOutput; }

    public Recommendation getRecommendation() {
        return dmOutput.recommendation;
    }
    public void setRecommendation(Recommendation recommendation) {
        dmOutput.recommendation = recommendation;
    }

    public UserFrame getUserFrame() {
        return dmOutput.frame;
    }
    public void setUserFrame(UserFrame userFrame) {
        dmOutput.frame = userFrame;
    }

    public List<String> getStates() {
        return states;
    }
    public void setStates(final List<String> states) {
        this.states = states;
    }

    public String toString(){
        return "Component: " + this.getClass().toString() + " System Action: " + dmOutput.action + " Strategy:" + strategy;
    }
}
