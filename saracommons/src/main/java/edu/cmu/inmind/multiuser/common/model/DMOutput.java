package edu.cmu.inmind.multiuser.common.model;

/**
 * Created by yoichimatsuyama on 4/11/17.
 */
public class DMOutput {
    String action;
    protected Recommendation recommendation;
    UserFrame frame;
    String utterance;

    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }

    public Recommendation getRecommendation() {
        return recommendation;
    }
    public void setRecommendation(Recommendation recommendation) {
        this.recommendation = recommendation;
    }

    /**
     *  @return true if contains recommendation, else false
     */
    public boolean hasFullContent() {
        return recommendation != null &&  recommendation.rexplanations != null;
    }
    public boolean isRecommendation() {
        return action.equals("recommend");
    }


    public UserFrame getUserFrame() {
        return frame;
    }
    public void setFrame(UserFrame frame) {
        this.frame = frame;
    }

    public String getUtterance() {
        return utterance;
    }
    public void setUtterance(String utterance) {
        this.utterance = utterance;
    }

    public String toString(){
        return "Component: " + this.getClass().toString() + " System Action: " + action;
    }
}
