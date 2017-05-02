package edu.cmu.inmind.multiuser.sara.component.groove.bson;
import com.google.gson.Gson;

import java.util.List;
/**
 * Created by yoichimatsuyama on 4/14/17.
 */
public class BSON {

    private String speech;
    private List<Word> words = null;
    private List<Behavior> behaviors = null;

    public String getSpeech() {
        return speech;
    }

    public void setSpeech(String speech) {
        this.speech = speech;
    }

    public List<Word> getWords() {
        return words;
    }

    public void setWords(List<Word> words) {
        this.words = words;
    }

    public List<Behavior> getBehaviors() {
        return behaviors;
    }

    public void setBehaviors(List<Behavior> behaviors) {
        this.behaviors = behaviors;
    }

    static public BSON string2BSON(String str){
        Gson gson = new Gson();
        BSON bson = gson.fromJson(str, BSON.class);
        return bson;
    }
}
