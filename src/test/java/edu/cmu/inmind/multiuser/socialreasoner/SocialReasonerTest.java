package edu.cmu.inmind.multiuser.socialreasoner;

import org.junit.Assert;
import org.junit.Test;

import edu.cmu.inmind.multiuser.socialreasoner.control.SocialReasonerController;
import edu.cmu.inmind.multiuser.socialreasoner.model.intent.SystemIntent;

/**
 * Created by timo on 2017-07-18
 */
public class SocialReasonerTest {

    @Test public void testSocialReasonerBasic() {
        // startup social reasoner,
        SocialReasonerController socialController = new SocialReasonerController();
        SocialReasonerController.setNonVerbals(false, false);
        SocialReasonerController.setUserConvStrategy("ASN");
        socialController.setRapportScore(4);
        socialController.addContinousStates(null);
        // test that output strategy is consistent when running the SR multiple times
        SystemIntent si = new SystemIntent();
        SystemIntent systemIntent =  new SystemIntent();
        systemIntent.setIntent("");
        systemIntent.setRecommendationResults("");
        socialController.addSystemIntent(si);
        String strat1 = socialController.getConvStrategyFormatted();
        Assert.assertNotEquals(strat1, "");
        socialController.addSystemIntent(si);
        String strat2 = socialController.getConvStrategyFormatted();
        Assert.assertNotEquals(strat2, "");
        // 2018-04-27: disabled this because it doesn't make sense that the social reasoner always has to output the same strategy given the same context
        // --fpecune, tshore
        //Assert.assertEquals(strat1, strat2);
    }

}
