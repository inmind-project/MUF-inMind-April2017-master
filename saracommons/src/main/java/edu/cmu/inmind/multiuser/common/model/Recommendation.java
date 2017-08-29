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

    @Override
    public String toString() {
        return "Recommendation{" +
                "rexplanations=" + rexplanations +
                '}';
    }
}
