package edu.cmu.inmind.multiuser.sara.component;

import beat.BEAT;
import beat.BeatCallback;
import beat.bson.BSON;
import edu.cmu.inmind.multiuser.common.Constants;
import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.Utils;
import edu.cmu.inmind.multiuser.common.model.SROutput;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.log.Loggable;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StateType;
import edu.cmu.inmind.multiuser.sara.component.nlg.SentenceGeneratorTemplate;

import java.io.FileNotFoundException;

/**
 * Created by oscarr on 3/7/17.
 */
@StateType( state = Constants.STATELESS)
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
        try {
            gen = new SentenceGeneratorTemplate();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            throw new RuntimeException("could not start SentenceGenerator. Exiting.");
        }
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
        String sentence = gen.generate(srOutput);
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
    public void onEvent(BlackboardEvent event) throws Exception
    {
        //TODO: add code here
        if(event.getId().equals(SaraCons.MSG_SR)) {
            extractAndProcess();
        }
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
        Log4J.info(this, "BSON to Android: " + Utils.toJson(bson));
    }
}
