package edu.cmu.inmind.multiuser.sara.component.nlg;

import edu.cmu.inmind.multiuser.common.model.SROutput;

/**
 * Created by yoichimatsuyama on 4/12/17.
 */
public interface SentenceGenerator {
    public String generate(SROutput intent);
}
