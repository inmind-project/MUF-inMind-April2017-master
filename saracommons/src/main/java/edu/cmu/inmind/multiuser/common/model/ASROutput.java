package edu.cmu.inmind.multiuser.common.model;

/**
 * Created by oscarr on 3/3/17.
 */
public class ASROutput {

    private String utterance;
    private double confidence;

    public ASROutput(String utterance, double confidence) {
        this.utterance = utterance;
        this.confidence = confidence;
    }

    public String getUtterance() {
        return this.utterance;
    }

    public void setUtterance(String utterance) {
        this.utterance = utterance;
    }

    public double getConfidence() {
        return this.confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    @Override
    public String toString()
    {
        return "Component: " + this.getClass().toString() + " Utterance: " + utterance + " confidence: " + confidence;
    }
}
