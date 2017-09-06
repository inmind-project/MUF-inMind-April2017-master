package edu.cmu.inmind.multiuser.common.model;

import java.util.List;

/**
 * Created by yoichimatsuyama on 4/16/17.
 */
public class Recommendation {
    List<Rexplanation> rexplanations;

    public List<Rexplanation> getRexplanations() {
        return rexplanations;
    }

    public void setRexplanations(List<Rexplanation> rexplanations) {
        this.rexplanations = rexplanations;
    }

    /**
     *  @return true if contains recommendation, else false
     */
    public boolean hasContent() {
        return rexplanations != null;
    }

    public String getTitle() {
        assert rexplanations.size() > 0;
        assert rexplanations.get(0).getRecommendation() != null;
        return rexplanations.get(0).getRecommendation();
    }

    @Override
    public String toString() {
        return "Recommendation{" +
                "rexplanations=" + rexplanations +
                '}';
    }
}
