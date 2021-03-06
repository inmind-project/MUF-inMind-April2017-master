package edu.cmu.inmind.multiuser.sara;

import java.io.IOException;
import java.util.Collections;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.controller.plugin.PluginModule;
import edu.cmu.inmind.multiuser.controller.resources.Config;
import edu.cmu.inmind.multiuser.sara.component.NLGComponent;
import edu.cmu.inmind.multiuser.sara.component.NLU_DMComponent;
import edu.cmu.inmind.multiuser.sara.component.RemoteCSCComponent;
import edu.cmu.inmind.multiuser.sara.component.SocialReasonerComponent;
import edu.cmu.inmind.multiuser.sara.component.UserModelComponent;
import edu.cmu.inmind.multiuser.sara.orchestrator.SaraOrchestrator;
import edu.cmu.lti.articulab.movies.muf.DialogOSComponent;

/**
 * Created by oscarr on 8/02/17.
 * <p>
 * This example illustrates the whole pipeline:
 * AndroidClient (ASR) -> DialogueSystem (NLU) -> TaskReasoner -> SocialReasoner -> NLG -> AndroidClient
 * This example is pretty similar to EX15_Wholepipeline, however, unlike Ex15_WholePipeline, in this example
 * the master MUF (Sara MUF) calls its slaves MUF's (i.e., Dialogue MUF, etc.). In order to run this example,
 * you have to define the connection information of your slaves MUF's into a json file (e.g., services.json)
 * and set it to the config object.
 */
public class SaraServerMainClass extends MainClassBase {

	public static void main(String args[]) throws Throwable {
		setLogLevel();
		// ConversationalStrategyUtil.preloadRecipes();

		//List<ShutdownHook> hooks = new ArrayList<>();
		// You can add hooks that will be executed when the MUF is stopped
		//hooks.add(new ShutdownHook() {
		//	@Override
		//	public void execute() {
		//		//do something here
		//	}
		//});
		//new SaraServerMainClass().execute(hooks);
		new SaraServerMainClass().execute(Collections.emptyList());
	}

	private static void setLogLevel() {
		// https://stackoverflow.com/a/41993517/1391325
		if (Boolean.getBoolean("log4j.debug")) {
			Configurator.setLevel(null, Level.DEBUG);
		}
	}

	@Override
	protected PluginModule[] createModules() {
		return new PluginModule[]{
				new PluginModule.Builder(SaraOrchestrator.class, UserModelComponent.class, SaraCons.ID_UM)
						.addPlugin(UserModelComponent.class, SaraCons.ID_UM)

						/*.addPlugin(FakeNLUComponent.class, SaraCons.ID_NLU)
						.addPlugin(FakeTaskReasonerComponent.class, SaraCons.ID_DM)*/
						.addPlugin(NLU_DMComponent.class, SaraCons.ID_NLU)
						.addPlugin(DialogOSComponent.class, SaraCons.ID_DM)
						.addPlugin(NLGComponent.class, SaraCons.ID_NLG)

						//.addPlugin(CSCComponent.class, SaraCons.ID_CSC)
						//.addPlugin(FakeCSCComponent.class, SaraCons.ID_CSC)
						.addPlugin(RemoteCSCComponent.class, SaraCons.ID_CSC)

						.addPlugin(SocialReasonerComponent.class, SaraCons.ID_SR)

						//.addPlugin(R5StreamComponent.class, SaraCons.ID_R5)
						.build()
		};
	}

	@Override
	protected Config createConfig() throws IOException {
		return super.createConfig()
				.setJsonServicesConfig("services.json");
	}
}
