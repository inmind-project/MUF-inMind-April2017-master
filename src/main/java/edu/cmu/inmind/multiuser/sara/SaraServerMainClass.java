package edu.cmu.inmind.multiuser.sara;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.controller.ShutdownHook;
import edu.cmu.inmind.multiuser.controller.plugin.PluginModule;
import edu.cmu.inmind.multiuser.controller.resources.Config;
import edu.cmu.inmind.multiuser.sara.component.*;
import edu.cmu.inmind.multiuser.sara.orchestrator.SaraOrchestrator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oscarr on 8/02/17.
 *
 * This example illustrates the whole pipeline:
 * AndroidClient (ASR) -> DialogueSystem (NLU) -> TaskReasoner -> SocialReasoner -> NLG -> AndroidClient
 * This example is pretty similar to EX15_Wholepipeline, however, unlike Ex15_WholePipeline, in this example
 * the master MUF (Sara MUF) calls its slaves MUF's (i.e., Dialogue MUF, etc.). In order to run this example,
 * you have to define the connection information of your slaves MUF's into a json file (e.g., services.json)
 * and set it to the config object.
 */
public class SaraServerMainClass extends MainClassBase {

    public static void main(String args[]) throws Throwable{
        List<ShutdownHook> hooks = new ArrayList<>();
        // You can add hooks that will be executed when the MUF is stopped
        hooks.add( new ShutdownHook() {
            @Override
            public void execute() {
                //TODO: do something
            }
        });
        new SaraServerMainClass().execute( hooks );
    }

    @Override
    protected PluginModule[] createModules() {
        return new PluginModule[]{
                new PluginModule.Builder(SaraOrchestrator.class, FakeCSCComponent.class, SaraCons.ID_CSC)
                        //comment out the line below if you want to use remote DialogueSystem
                        //.addPlugin(FakeNLUComponent.class, SaraCons.ID_NLU)
                        //.addPlugin(FakeTaskReasonerComponent.class, SaraCons.ID_DM)
                        .addPlugin(UserModelComponent.class, SaraCons.ID_UM)
                        .addPlugin(NLU_DMComponent.class, SaraCons.ID_NLU)
                        .addPlugin(NLGComponent.class, SaraCons.ID_NLG)
                        .addPlugin(SocialReasonerComponent.class, SaraCons.ID_SR)
                        //.addPlugin(CSCComponent.class, SaraCons.ID_R5)
                        //.addPlugin(FakeCSCComponent.class, SaraCons.ID_CSC)
                        //.addPlugin(R5StreamComponent.class, SaraCons.ID_R5)
                        .build()
        };
    }

    @Override
    protected Config createConfig() {
        return super.createConfig()
                .setJsonServicesConfig("services.json");
    }
}
