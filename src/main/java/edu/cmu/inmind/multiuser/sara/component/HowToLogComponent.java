package edu.cmu.inmind.multiuser.sara.component;

import edu.cmu.inmind.multiuser.controller.blackboard.Blackboard;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.common.Constants;
import edu.cmu.inmind.multiuser.controller.log.Loggable;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StateType;

/**
 * Created by oscarr on 3/22/17.
 */
@StateType( state = Constants.STATELESS)
public class HowToLogComponent extends PluggableComponent {
    @Override
    public void execute() {
        // this will explicitly add this message to your MessageLogger
        getMessageLogger().add( "any-id", "this-is-an-example-of-how-to-explicitly-log-messages" );
    }

    @Loggable
    public void anotherMethod(String test){
        // MessageLogger will add a new entry: [timestamp anotherMethod "test"]. You don't have to do anything
        // explicitly other than adding the Loggable annotation to this method.
    }

    @Override
    public void onEvent(Blackboard blackboard, BlackboardEvent event) throws Exception
    {
    }
}
