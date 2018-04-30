package edu.cmu.inmind.multiuser.sara.component;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.model.SROutput;
import edu.cmu.inmind.multiuser.controller.blackboard.Blackboard;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.common.Constants;
import edu.cmu.inmind.multiuser.controller.common.Utils;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.log.Loggable;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StateType;
import edu.cmu.inmind.multiuser.sara.component.beat.BEAT;
import edu.cmu.inmind.multiuser.sara.component.beat.BeatCallback;
import edu.cmu.inmind.multiuser.sara.component.beat.bson.BSON;
import edu.cmu.inmind.multiuser.sara.component.nlg.SentenceGeneratorTemplate;

import java.util.List;

/**
 * Created by oscarr on 3/7/17.
 */
@StateType( state = Constants.STATEFULL)
@BlackboardSubscription( messages = {SaraCons.MSG_SR})
public class NLGComponent extends PluggableComponent implements BeatCallback {
     SentenceGeneratorTemplate gen;
     Blackboard blackboard;
     BlackboardEvent blackboardEvent;
     BEAT beat;

    public NLGComponent() {
        try {
            beat = new BEAT();
            beat.setCallback(this);
            gen = new SentenceGeneratorTemplate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("could not start SentenceGenerator (or BEAT). Exiting.");
        }
    }

    @Override
    public void startUp(){
        super.startUp();
        Log4J.debug(this, "startup");
    }

    @Override
    public void execute() {
        Log4J.info(this, "NLGComponent: " + hashCode());
        extractAndProcess();
    }

    @Loggable
    private void extractAndProcess() {
        SROutput srOutput = null;
        try {
            srOutput = (SROutput) blackboardEvent.getElement();
        }catch (Throwable t)
        {
            t.printStackTrace();
        }
        /**
         * generation
         */
        if(srOutput!=null) {
            Log4J.info(this, "NLG srOutput: " + srOutput.toString());
            List<String> sentences = gen.generateAsList(srOutput);
            Log4J.info(this, "NLG generated: " + sentences);
            for (String sentence : sentences) {
                Log4J.info(this, "for sentence pattern: " + sentence);
                sentence = gen.replacePatterns(sentence, srOutput);
                assert sentence != null : "I haven't been able to fill in any pattern. Duh.";
                Log4J.info(this, "replaced pattern to: " + sentence);
                /**
                 * send sentence to BEAT
                 */
                beat.getBsonCompiler().setPlainText(sentence);
                beat.startProcess(sentence);
                Log4J.info(this, "NLG sentence: " + sentence);
            }
        }
        else
        {
            Log4J.error(this, "SROutput value is NULL. ");
        }
    }

    /**
     * If the blackboard model is modified externally, does NLGComponent have to do anything? this is useful when running multiple
     * processes in parallel rather than sequentially.
     */
    @Override
    public void onEvent(Blackboard blackboard, BlackboardEvent event) throws Exception
    {
            //Log4J.info(this, "blackboard is not null");
            this.blackboard = blackboard;
            this.blackboardEvent = event;
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
        Log4J.info(this, "SessionID: " + this.getSessionId() + "Output: " +bson.getSpeech());
        this.blackboard.post(this, SaraCons.MSG_NLG, bson);
        Log4J.info(this, "BSON to Android: " + Utils.toJson(bson));
    }
}
