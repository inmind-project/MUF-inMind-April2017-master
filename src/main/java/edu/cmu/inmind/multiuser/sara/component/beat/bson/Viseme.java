package edu.cmu.inmind.multiuser.sara.component.beat.bson;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yoichimatsuyama on 4/14/17.
 */
public class Viseme {
    private Float startTime;
    private String symbol;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Float getStartTime() {
        return startTime;
    }

    public void setStartTime(Float startTime) {
        this.startTime = startTime;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}