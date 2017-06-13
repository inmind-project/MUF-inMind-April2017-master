package edu.cmu.inmind.multiuser.common.model;

import edu.cmu.lti.rapport.pipline.csc.ConversationalStrategyDistribution;

import java.util.EnumMap;
import java.util.List;

/**
 * Created by fpecune on 4/15/2017.
 */
public class CSCOutput {
    private ConversationalStrategyDistribution userConversationalStrategies;

    public String toString(){
        return "User strategies: " + userConversationalStrategies;
    }

    public void setStrategyScores(ConversationalStrategyDistribution strategies){
        this.userConversationalStrategies = strategies;
    }

    public ConversationalStrategyDistribution getUserStrategies(){
        return this.userConversationalStrategies;
    }

}
