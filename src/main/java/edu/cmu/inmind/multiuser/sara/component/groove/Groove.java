package edu.cmu.inmind.multiuser.sara.component.groove;
import edu.cmu.inmind.multiuser.sara.component.groove.bson.BSON;

/**
 * Created by yoichimatsuyama on 4/14/17.
 */
public class Groove {
    public BSON generateBson(String text){
        BSON bson = new BSON();
        bson.setSpeech(text);
        return bson;
    }
}
