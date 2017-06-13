package edu.cmu.inmind.multiuser.sara.component.beat;
import com.google.gson.Gson;
import edu.cmu.inmind.multiuser.sara.component.beat.bson.*;

import java.util.List;
/**
 * Created by yoichimatsuyama on 4/14/17.
 */
public class BSON {

    private String speech;
    private List<Word> words = null;
    private List<edu.cmu.inmind.multiuser.sara.component.beat.bson.Behavior> behaviors = null;

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

    public List<edu.cmu.inmind.multiuser.sara.component.beat.bson.Behavior> getBehaviors() {
        return behaviors;
    }

    public void setBehaviors(List<edu.cmu.inmind.multiuser.sara.component.beat.bson.Behavior> behaviors) {
        this.behaviors = behaviors;
    }

    static public BSON string2BSON(String str){
        Gson gson = new Gson();
        BSON bson = gson.fromJson(str, BSON.class);
        return bson;
    }
}
