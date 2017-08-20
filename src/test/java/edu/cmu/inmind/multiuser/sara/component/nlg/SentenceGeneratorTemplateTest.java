package edu.cmu.inmind.multiuser.sara.component.nlg;

import edu.cmu.inmind.multiuser.common.Utils;
import edu.cmu.inmind.multiuser.common.model.ConversationalStrategy;
import edu.cmu.inmind.multiuser.common.model.DMOutput;
import edu.cmu.inmind.multiuser.common.model.SROutput;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by timo on 18.08.17.
 */
public class SentenceGeneratorTemplateTest {

    @Test
    public void testDefault() throws FileNotFoundException {
        SentenceGenerator gen = new SentenceGeneratorTemplate();
        doTest(gen);
    }

    @Test public void testTestDB() throws FileNotFoundException {
        SentenceGenerator gen = new SentenceGeneratorTemplate(this.getClass().getResourceAsStream("TestDB.tsv"));
        doTest(gen);
    }

    private void doTest(SentenceGenerator gen) {
        String recommendedMovie = "Silver Linings Playbook (2012)";
        DMOutput dmout = Utils.fromJson("{'action': 'recommend', 'entities': [], 'frame': {'frame': {'actors': {'like': [{'entity': 'actors', 'value': 'jennifer lawrence', 'start': 0, 'end': 17, 'confidence': 1, 'id': 'jennifer_iii_lawrence', 'polarity': 0.0}], 'dislike': []}, 'genres': {'like': [{'entity': 'genres', 'id': 'romance', 'value': 'romantic', 'start': 0, 'end': 8, 'polarity': 0.0}, {'entity': 'genres', 'id': 'comedy', 'value': 'comedy', 'start': 9, 'end': 15, 'polarity': 0.0}], 'dislike': []}, 'directors': {'like': [{'entity': 'directors', 'value': 'david o. russell', 'start': 0, 'end': 13, 'confidence': 0.5, 'id': 'david_o._russell', 'polarity': 0.0}], 'dislike': []}, 'movies': {'like': ['Silver Linings Playbook (2012)'], 'dislike': [], 'history': []}}, 'ask_stack': ['recommend'], 'universals': ['help', 'start_over']}, 'recommendation': {'rexplanations': [{'recommendation': '"+recommendedMovie+"', 'explanations': ['David O. Russell', 'Jennifer (III) Lawrence']}]}}",
                DMOutput.class);
        SROutput srOutput = new SROutput();
        srOutput.setAction(dmout.getAction());
        srOutput.setEntities(dmout.getEntities());
        srOutput.setRecommendation(dmout.getRecommendation());
        srOutput.setUserFrame(dmout.getUserFrame());
        srOutput.setRapport(7);
        srOutput.setStrategy("NONE");
        String nlgout = gen.generate(srOutput);
        System.out.println(nlgout);
        assert(nlgout.contains(recommendedMovie));
        for (ConversationalStrategy cs : ConversationalStrategy.values()) {
            srOutput.setStrategy(cs.name());
            nlgout = gen.generate(srOutput);
            System.out.println(nlgout);
            assert(nlgout.contains(recommendedMovie));
        }
    }

    @Test(expected = FileNotFoundException.class)
    public void testException1() throws FileNotFoundException {
        new SentenceGeneratorTemplate("lets/hope/this/file/does/not/exist");
    }
    @Test(expected = AssertionError.class)
    public void testException2() throws FileNotFoundException {
        new SentenceGeneratorTemplate((InputStream) null);
    }
}
