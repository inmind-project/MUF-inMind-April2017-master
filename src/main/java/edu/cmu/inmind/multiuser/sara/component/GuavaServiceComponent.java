package edu.cmu.inmind.multiuser.sara.component;

import edu.cmu.inmind.multiuser.controller.blackboard.Blackboard;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.common.Constants;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StateType;

/**
 * Created by oscarr on 3/17/17.
 */
@StateType( state = Constants.STATEFULL)
public class GuavaServiceComponent extends PluggableComponent {
    private boolean flagStart = false;
    private int step = 0;
    private String kindOfService;

    public void setFlagStart(boolean flagStart) {
        this.flagStart = flagStart;
    }

    public void setKindOfService(String kindOfService) {
        this.kindOfService = kindOfService;
    }

    @Override
    public void startUp(){
        super.startUp();
        // TODO: add code to initialize this component
    }

    @Override
    public void execute() {
        if( flagStart ) {
            // this checks if Service (AbstractIdleService) is at the RUNNING state
            while( isRunning() ) {
                Log4J.info(this, kindOfService + ": GuavaServiceComponent is in running state. Step: " + step);
                step++;
                if( step == 10 ){
                    // this method will move the component to the TERMINATED state
                    stopAsync();
                }
            }
            Log4J.info(this, kindOfService + ": Exiting GuavaServiceComponent");
        }
    }

    @Override
    public void shutDown() {
        super.shutDown();
        // TODO: add code to release resources
        Log4J.info(this, kindOfService + ": Shutdown");
    }

    @Override
    public void onEvent(Blackboard blackboard, BlackboardEvent event) throws Exception

    {
        //TODO: add code here
        //...
        Log4J.info(this, kindOfService + ". These objects have been updated at the blackboard: "
                + event.toString() );
    }
}
