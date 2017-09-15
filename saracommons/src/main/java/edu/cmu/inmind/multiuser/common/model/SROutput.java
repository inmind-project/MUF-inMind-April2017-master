package edu.cmu.inmind.multiuser.common.model;

import java.util.List;

/**
 * Created by yoichimatsuyama on 4/11/17.
 */
public class SROutput {

    DMOutput dmOutput;
    /** one of the strategies, hopefully as defined in ConversationalStrategy */
    String strategy;
    /** real value between 1-7 or 0 for undefined */
    double rapport;
    /** list of BehaviorNetwork states */
    List<String> states;

    public SROutput(DMOutput dmOutput) { 
        this.dmOutput = dmOutput;
    }
    public DMOutput getDMOutput() { return dmOutput; }

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
