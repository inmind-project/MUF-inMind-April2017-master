package edu.cmu.inmind.multiuser.common.model;

import java.util.List;

/**
 * Created by fpecune on 4/15/2017.
 */
public class CSCOutput {
    private List<Strategy> userStrategies;

    public String toString(){
        String message = "";
        for(Strategy s : userStrategies){
            message += userStrategies.get(0).getName() + " " + userStrategies.get(0).getScore();
            message += " ";
        }
        return "Component: " + this.getClass().toString() + message;
    }

    public Strategy getBest(){

        Strategy best = null;
        double score = -1.0;
        for(Strategy s : userStrategies){
            if (s.getScore() > score) {
                best = s;
                score = s.getScore();
            }
        }

        return best;
    }

    public void setUserStrategies(List<Strategy> strategies){
        this.userStrategies = strategies;
    }

    public List<Strategy> getUserStrategies(){
        return this.userStrategies;
    }

}
