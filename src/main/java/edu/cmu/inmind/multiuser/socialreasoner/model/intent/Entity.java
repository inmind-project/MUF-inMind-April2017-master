package edu.cmu.inmind.multiuser.socialreasoner.model.intent;

/**
 * Created by yoichimatsuyama on 4/13/17.
 */
public class Entity {
    String entity;
    float polarity;
    String value;
    String id;
    int start;
    int end;

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public float getPolarity() {
        return polarity;
    }

    public void setPolarity(float polarity) {
        this.polarity = polarity;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public int getStart(){
        return start;
    }

    public void setStart(int start){
        this.start = start;
    }

    public int getEnd(){
        return end;
    }

    public void setEnd(int end){
        this.end = end;
    }


    @Override
    public String toString() {
        return "Entity{" +
                "entity='" + entity + '\'' +
                ", polarity=" + polarity +
                ", value='" + value + '\'' +
                ", id='" + id + '\'' +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}
