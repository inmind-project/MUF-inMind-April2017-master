package edu.cmu.inmind.multiuser.sara.component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.model.SROutput;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.log.Loggable;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StatelessComponent;
import edu.cmu.inmind.multiuser.sara.component.beat.BEAT;
import edu.cmu.inmind.multiuser.sara.component.beat.BeatCallback;
import edu.cmu.inmind.multiuser.sara.component.nlg.SentenceGeneratorTemplate;
import beat.bson.BSON;

/**
 * Created by oscarr on 3/7/17.
 */
@StatelessComponent
@BlackboardSubscription( messages = {SaraCons.MSG_SR})
public class NLGComponent extends PluggableComponent implements BeatCallback {
    SentenceGeneratorTemplate gen;
    SROutput srOutput;
    BEAT beat;

    public NLGComponent() {
        try {
            beat = new BEAT();
            beat.setCallback(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startUp(){
        super.startUp();
        // TODO: add code to initialize this component
        gen = new SentenceGeneratorTemplate();
    }

    @Override
    public void execute() {
        Log4J.info(this, "NLGComponent: " + hashCode());
        extractAndProcess();
    }
    @Loggable
    private void extractAndProcess() {
        srOutput = (SROutput) blackboard().get(SaraCons.MSG_SR);
        /**
         * generation
         */
        Log4J.info(this, "NLG srOutput: " + srOutput);
        String sentence = gen.genenete(srOutput);
        /**
         * send sentence to BEAT
         */
        beat.getBsonCompiler().setPlainText(sentence);
        beat.startProcess(sentence);
        Log4J.info(this, "NLG sentence: " + sentence);
    }

    /**
     * If the blackboard model is modified externally, does NLGComponent have to do anything? this is useful when running multiple
     * processes in parallel rather than sequentially.
     */
    @Override
    public void onEvent(BlackboardEvent event) {
        //TODO: add code here
        extractAndProcess();
    }

    @Override
    public void shutDown() {
        super.shutDown();
        // TODO: add code to release resources
    }

    @Override
    public void receiveMessage(String msg) {
        BSON bson = BSON.string2BSON(msg);
        /**
         * update the blackboard
         */
        Log4J.info(this, "Input: " + srOutput.getAction() + " " + srOutput.getStrategy() + " Output: " +bson.getSpeech());
        blackboard().post( this, SaraCons.MSG_NLG, bson );
        Log4J.info(this, "BSON to Android: " + bson);
    }
}
