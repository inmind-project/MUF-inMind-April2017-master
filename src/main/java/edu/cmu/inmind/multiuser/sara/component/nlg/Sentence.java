package edu.cmu.inmind.multiuser.sara.component.nlg;

/**
 * Created by yoichimatsuyama on 4/12/17.
 */
public class Sentence {
    String phase;
    String intent;
    String strategy;
    String sentence;

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }
}
