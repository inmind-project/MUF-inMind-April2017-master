package edu.cmu.inmind.multiuser.common.model;

/**
 * Created by fpecune on 5/18/2017.
 */
public class Strategy{
    private String name;
    private double score;

    public Strategy() {}
    public Strategy(String name, double score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }
    public double getScore(){
        return score;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setScore(double score){
        this.score = score;
    }
}