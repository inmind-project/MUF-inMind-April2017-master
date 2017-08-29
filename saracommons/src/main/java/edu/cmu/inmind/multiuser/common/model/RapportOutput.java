package edu.cmu.inmind.multiuser.common.model;

/**
 * Created by fpecune on 4/15/2017.
 */
public class RapportOutput {
    private String userStrategy;
    private double rapportScore;

    public String getUserStrategy(){
        return userStrategy;
    }

    public void setUserStrategy(String strategy){
        this.userStrategy = strategy;
    }

    public double getRapportScore(){
        return rapportScore;
    }

    public void setRapportScore(double rapport){
        this.rapportScore = rapport;
    }

    public String toString(){
        return "Component: " + this.getClass().toString() + " User Strategy: " + userStrategy + " rapport: " + rapportScore;
    }


}
