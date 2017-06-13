package edu.cmu.inmind.multiuser.sara;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.controller.plugin.PluginModule;
import edu.cmu.inmind.multiuser.sara.component.*;
import edu.cmu.inmind.multiuser.sara.examples.MainClass;
import edu.cmu.inmind.multiuser.sara.orchestrator.SaraOrchestratorEx15;

/**
 * Created by oscarr on 5/8/17.
 */
public class MainLauncher extends MainClass {

    public static void main(String args[]) {
        new MainLauncher().execute();
    }

    @Override
    protected PluginModule[] createModules() {
        return new PluginModule[]{
                new PluginModule.Builder(SaraOrchestratorEx15.class)
                        //comment out the line below if you want to use remote DialogueSystem
                        .addPlugin(NLUComponent.class, SaraCons.ID_NLU)
                        //.addPlugin(NLU_DMComponent.class, SaraCons.ID_NLU)
                        //.addPlugin(RapportEstimator.class, SaraCons.ID_RPT)
                        .addPlugin(TaskReasonerComponent.class, SaraCons.ID_DM)
                        .addPlugin(SocialReasonerComponent.class, SaraCons.ID_SR)
                        .addPlugin(NLGComponent.class, SaraCons.ID_NLG)
                        //.addPlugin(OpenFaceComponent.class, SaraCons.ID_OF)
                        //.addPlugin(UserModelComponent.class, SaraCons.ID_UM)
                        .build()
        };

    }
}
