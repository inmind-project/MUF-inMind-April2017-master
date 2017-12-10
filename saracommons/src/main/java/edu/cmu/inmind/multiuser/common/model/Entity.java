package edu.cmu.inmind.multiuser.common.model;

/**
 * Created by yoichimatsuyama on 4/13/17.
 */
public class Entity {
    String entity;

    public Entity(String entity, float polarity) {
        this.entity = entity;
    }

    public String getEntity() {
        return entity;
    }
    public void setEntity(String entity) {
        this.entity = entity;
    }

}