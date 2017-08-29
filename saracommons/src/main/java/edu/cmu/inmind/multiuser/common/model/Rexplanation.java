package edu.cmu.inmind.multiuser.common.model;

import java.util.List;

/**
 * Created by yoichimatsuyama on 4/13/17.
 */
public class Rexplanation {
    String recommendation;
    List<String> explanations;

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public List<String> getExplanations() {
        return explanations;
    }

    public void setExplanations(List<String> explanations) {
        this.explanations = explanations;
    }

    @Override
    public String toString() {
        return "Rexplanation{" +
                "recommendation='" + recommendation + '\'' +
                ", explanations=" + explanations +
                '}';
    }
}
