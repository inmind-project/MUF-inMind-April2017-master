package edu.cmu.inmind.multiuser.socialreasoner.model.intent;

import java.util.List;

/**
 * Created by oscarr on 11/15/16.
 */
public class SystemIntent {
    private String intent;
    private String phase;
    private List<Entity> entities;
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

    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }


    public String getRecommendationResults() {
        return recommendationResults;
    }

    public void setRecommendationResults(String recommendationResults) {
        this.recommendationResults = recommendationResults;
    }
}
