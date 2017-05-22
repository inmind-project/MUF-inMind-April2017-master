package edu.cmu.inmind.multiuser.common.model;

import java.util.EnumMap;
import java.util.List;

/**
 * Created by fpecune on 4/15/2017.
 */
public class CSCOutput {
    private EnumMap<ConversationalStrategy, Double> userConversationalStrategies;

    public String toString(){
        return "User strategies: " + userConversationalStrategies;
    }

    public void setStrategyScores(EnumMap<ConversationalStrategy, Double> strategies){
        this.userConversationalStrategies = strategies;
    }

    public EnumMap<ConversationalStrategy, Double> getUserStrategies(){
        return this.userConversationalStrategies;
    }

}
