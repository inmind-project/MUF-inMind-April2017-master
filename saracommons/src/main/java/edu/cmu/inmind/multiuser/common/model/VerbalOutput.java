package edu.cmu.inmind.multiuser.common.model;

/**
 * Created by oscarr on 3/3/17.
 */
public class VerbalOutput {
    private String utterance;
    private String convStrategy;

    public VerbalOutput(String utterance) {
        this.utterance = utterance;
    }

    public VerbalOutput(String utterance, String convStrategy) {
        this.utterance = utterance;
        this.convStrategy = convStrategy;
    }

    public String getUtterance() {
        return this.utterance;
    }

    public void setUtterance(String utterance) {
        this.utterance = utterance;
    }

    public String getConvStrategy() {
        return this.convStrategy;
    }

    public void setConvStrategy(String convStrategy) {
        this.convStrategy = convStrategy;
    }

    @Override
    public String toString() {
        return "Component: " + this.getClass().toString() + " utterance: " + utterance
                + " convStrategy: " + convStrategy;
    }
}
