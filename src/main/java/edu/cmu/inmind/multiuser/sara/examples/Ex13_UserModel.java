package edu.cmu.inmind.multiuser.sara.examples;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.controller.plugin.PluginModule;
import edu.cmu.inmind.multiuser.sara.component.NLU_DMComponent;
import edu.cmu.inmind.multiuser.sara.component.UserModelComponent;
import edu.cmu.inmind.multiuser.sara.orchestrator.SaraOrchestratorEx13;

/**
 * Created by oscarr on 4/10/17.
 *
 * This example illustrates how easy is to create a User Model. Take a look at
 * UserModelComponent. Basically this component is subscribed to NLU messages
 * and when it receives a new update, it extracts the user intent, and then
 * the corresponding entities (this is useful, for instance, for extracting
 * preferences and interests from entities).
 * Note: you will have to uncomment some code on NLU_DMComponent.onEvent() in
 * order to run this example.
 */
public class Ex13_UserModel extends MainClass {

    public static void main(String args[]) {
        new Ex13_UserModel().execute();
    }

    @Override
    protected PluginModule[] createModules() {
        return new PluginModule[]{
                new PluginModule.Builder(SaraOrchestratorEx13.class)
                        .addPlugin(NLU_DMComponent.class, SaraCons.ID_NLU)
                        .addPlugin(UserModelComponent.class, SaraCons.ID_UM)
                        .build()
        };
    }
}
