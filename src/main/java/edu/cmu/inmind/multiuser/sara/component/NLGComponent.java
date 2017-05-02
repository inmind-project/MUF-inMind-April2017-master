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
import edu.cmu.inmind.multiuser.sara.component.groove.Groove;
import edu.cmu.inmind.multiuser.sara.component.groove.bson.BSON;
import edu.cmu.inmind.multiuser.sara.component.nlg.SentenceGeneratorTemplate;

/**
 * Created by oscarr on 3/7/17.
 */
@StatelessComponent
@BlackboardSubscription( messages = {SaraCons.MSG_SR})
public class NLGComponent extends PluggableComponent implements BeatCallback{
    Groove groove;
    BEAT beat;
    SentenceGeneratorTemplate gen;
    SROutput srOutput;

    @Override
    public void startUp(){
        super.startUp();
        // TODO: add code to initialize this component
        groove = new Groove();
        beat = new BEAT();
        beat.addMessageListener(this);
        gen = new SentenceGeneratorTemplate();
    }

    @Override
    public void execute() {
        Log4J.info(this, "NLGComponent: " + hashCode());
        extractAndProcess();
    }
    @Loggable
    private void extractAndProcess() {
        //SaraInput saraInput = (SaraInput) blackboard().get(SaraCons.MSG_ASR);
        srOutput = (SROutput) blackboard().get(SaraCons.MSG_SR);
        //System.out.println(blackboard().get(SaraCons.MSG_SR).toString());
        //SROutput srOutput = generateFakeSROutput();
        /**
         * generation
         */
         String sentence = gen.genenete(srOutput);
        beat.sendMessage(srOutput, sentence);

        //BSON bson = groove.generateBson(sentence);

    }

    /**
     * If the blackboard model is modified externally, does NLGComponent have to do anything? this is useful when running multiple
     * processes in parallel rather than sequentially.
     */
    @Override
    public void onEvent(BlackboardEvent event) {
        //TODO: add code here
        //...
        //Log4J.info(this, "NLGComponent. These objects have been updated at the blackboard: " + event.toString());
        extractAndProcess();
    }

    @Override
    public void shutDown() {
        super.shutDown();
        // TODO: add code to release resources
    }

    public SROutput generateFakeSROutput(){
        SROutput srOutput = new SROutput();

        String json_sr = "{\n" +
                "  \"action\": \"recommend\",\n" +
                "  \"strategy\": \"SD\",\n" +
                "  \"rapport\": 4,\n" +
                "  \"entities\": [\n" +
                "    {\n" +
                "      \"entity\": \"genres\",\n" +
                "      \"polarity\": 0.5,\n" +
                "      \"value\": \"drama\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"recommendation\": {\n" +
                "    \"rexplanations\": [\n" +
                "      {\n" +
                "        \"recommendation\": \"Toy Story (1995)\",\n" +
                "        \"explanations\": [\n" +
                "          \"stomhanks\"\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"userFrame\": {\n" +
                "    \"frame\": {\n" +
                "      \"actors\": {\n" +
                "        \"like\": [\n" +
                "          \"tom_cruise\",\n" +
                "          \"tom_hanks\"\n" +
                "        ],\n" +
                "        \"dislike\": [\n" +
                "          \"harrison_ford\",\n" +
                "          \"carrie_fisher\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"genres\": {\n" +
                "        \"like\": [\n" +
                "          \"sci-fi\",\n" +
                "          \"drama\"\n" +
                "        ],\n" +
                "        \"dislike\": [\n" +
                "          \"documentary\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"directors\": {\n" +
                "        \"like\": [\n" +
                "          \"steven_spielberg\",\n" +
                "          \"christopher_nolan\"\n" +
                "        ],\n" +
                "        \"dislike\": [\n" +
                "          \"quentin_tarantino\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"movies\": {\n" +
                "        \"like\": [\n" +
                "          \"toy_story\",\n" +
                "          \"intersteller\"\n" +
                "        ],\n" +
                "        \"dislike\": [\n" +
                "          \"et\"\n" +
                "        ],\n" +
                "        \"history\": [\n" +
                "          \"???\"\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    \"ask_stack\": [\n" +
                "      \"start\",\n" +
                "      \"genres\",\n" +
                "      \"directors\",\n" +
                "      \"actors\",\n" +
                "      \"recommend\"\n" +
                "    ],\n" +
                "    \"universals\": [\n" +
                "      \"help\",\n" +
                "      \"start_over\"\n" +
                "    ]\n" +
                "  }\n" +
                "}";


        Gson gson = new GsonBuilder().create();
        srOutput = gson.fromJson(json_sr, SROutput.class);

        String action = "";

        /*
        Random ran = new Random();
        int rInt = ran.nextInt(9);
        if(rInt == 0) action = "greeting";
        else if(rInt == 1) action = "goodbye";
        else if(rInt == 2) action = "ask_genres";
        else if(rInt == 3) action = "ask_directors";
        else if(rInt == 4) action = "ask_actors";
        else if(rInt == 5) action = "ask_repeat";
        else if(rInt == 6) action = "explicit_confirm";
        else if(rInt == 7) action = "recommend";
        else if(rInt == 8) action = "help";
        srOutput.setAction(action);
        srOutput.setStrategy("SD");
        srOutput.setRapport(4);
        */

        return srOutput;
    }

    @Override
    public void receiveMessage(String msg) {
        BSON bson = BSON.string2BSON(msg);
        /**
         * update the blackboard
         */
        Log4J.info(this, "Input: " + srOutput.getAction() + " " + srOutput.getStrategy() + " Output: " +bson.getSpeech());
        blackboard().post( this, SaraCons.MSG_NLG, bson );
        //System.out.println("BSON: " + bson.getSpeech());
    }
}
