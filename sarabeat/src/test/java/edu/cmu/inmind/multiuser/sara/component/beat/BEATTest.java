package edu.cmu.inmind.multiuser.sara.component.beat;

import beat.BEAT;
import beat.Config;
import org.junit.Test;

/**
 * Created by timo on 07.07.17.
 */
public class BEATTest {

    @Test(timeout = 10000)
    public void beatTest() throws Exception {
        Config.XMLDIR = "XMLData/";
        BEAT beat = new BEAT();
        String input = "This is a test sentence.";
        beat.getBsonCompiler().setPlainText(input);
        beat.startProcess(input);
    }
}
