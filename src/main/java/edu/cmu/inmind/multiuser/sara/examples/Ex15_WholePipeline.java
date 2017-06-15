package edu.cmu.inmind.multiuser.sara.examples;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.controller.plugin.PluginModule;
import edu.cmu.inmind.multiuser.sara.component.*;
import edu.cmu.inmind.multiuser.sara.orchestrator.SaraOrchestratorEx15;

/**
 * Created by oscarr on 4/10/17.
 *
 * This example illustrates the whole pipeline:
 * AndroidClient (ASR) -> DialogueSystem (NLU) -> TaskReasoner -> SocialReasoner -> NLG -> AndroidClient
 * Note: this time you will have to run DialogueSystem and uncomment
 */
public class Ex15_WholePipeline extends MainClass {

    public static void main(String args[]) {
        new Ex15_WholePipeline().execute();
    }

    @Override
    protected PluginModule[] createModules() {
        return new PluginModule[]{
                new PluginModule.Builder(SaraOrchestratorEx15.class)
                        //comment out the line below if you want to use remote DialogueSystem
                        //.addPlugin(NLUComponent.class, SaraCons.ID_NLU)
                        .addPlugin(NLU_DMComponent.class, SaraCons.ID_NLU)
                        .addPlugin(RapportEstimator.class, SaraCons.ID_RPT)
                        //.addPlugin(TaskReasonerComponent.class, SaraCons.ID_DM)
                        .addPlugin(SocialReasonerComponent.class, SaraCons.ID_SR)
                        .addPlugin(NLGComponent.class, SaraCons.ID_NLG)
                        .addPlugin(OpenFaceComponent.class, SaraCons.ID_OF)
                        .addPlugin(R5StreamComponent.class, SaraCons.ID_R5)
                        //.addPlugin(UserModelComponent.class, SaraCons.ID_UM)
                        .build()
        };

    }
}
