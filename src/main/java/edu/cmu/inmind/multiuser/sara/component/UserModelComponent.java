package edu.cmu.inmind.multiuser.sara.component;

import edu.cmu.inmind.multiuser.common.Constants;
import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.Utils;
import edu.cmu.inmind.multiuser.common.model.SaraOutput;
import edu.cmu.inmind.multiuser.common.model.UserIntent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StateType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by oscarr on 3/16/17.
 */
@StateType( state = Constants.STATEFULL)
@BlackboardSubscription( messages = {SaraCons.MSG_DM})
public class UserModelComponent extends PluggableComponent {

    //private final static String PATH = "C:\\Users\\fpecune\\Desktop\\Logs";
    private final static String FILE_NAME = "MyUserModel.json";

    @Override
    public void startUp(){
        super.startUp();
        // TODO: uncomment
        //userModel = Utils.readObjectFromJsonFile( PATH + FILE_NAME, HashMap.class);
    }

    @Override
    public void execute() {
        //TODO: what should it go here? maybe store the user model in a DB? you may want to use an ORM such as Hybernate
    }

    @Override
    public void onEvent(BlackboardEvent event) {
        System.out.println("toto-------------------");
        System.out.println();
    }

    @Override
    public void shutDown() {
        super.shutDown();
        // TODO: add code to release resources
        // you can store the User Model on disk (DB, File, Json, etc).
        //Utils.writeObjectToJsonFile( userModel, PATH, FILE_NAME);
    }
}
