package edu.cmu.inmind.multiuser.socialreasoner.model.intent;

/**
 * Created by oscarr on 11/15/16.
 */
public class SystemIntent {
    private String intent;
    private String phase;
    private String recommendationResults;

    public SystemIntent() {
    }

    public SystemIntent(String intent, String phase) {
        this.intent = intent;
        this.phase = phase;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }


    public String getRecommendationResults() {
        return recommendationResults;
    }

    public void setRecommendationResults(String recommendationResults) {
        this.recommendationResults = recommendationResults;
    }
}
