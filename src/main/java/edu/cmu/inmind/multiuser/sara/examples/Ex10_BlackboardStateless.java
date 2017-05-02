package edu.cmu.inmind.multiuser.sara.examples;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.controller.plugin.PluginModule;
import edu.cmu.inmind.multiuser.sara.component.NLU_DMComponent;
import edu.cmu.inmind.multiuser.sara.orchestrator.SaraOrchestratorEx10;

/**
 * Created by oscarr on 4/10/17.
 *
 * By default, the Blackboard keeps a state of all messages that are posted by components,
 * however, you can tell the blackboard to not store the state, just notify the subscribers.
 * Note that you won't be able to get any object from the Blackboard, of course.
 * For this example do not run DialogueSystem, you won't need it.
 */
public class Ex10_BlackboardStateless extends MainClass {

    public static void main(String args[]) {
        new Ex10_BlackboardStateless().execute();
    }

    @Override
    protected PluginModule[] createModules() {
        return new PluginModule[]{
                new PluginModule.Builder(SaraOrchestratorEx10.class)
                        .addPlugin(NLU_DMComponent.class, SaraCons.ID_NLU)
                        .build()
        };
    }
}
