package edu.cmu.inmind.multiuser.sara;

import edu.cmu.inmind.multiuser.common.Constants;
import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.Utils;
import edu.cmu.inmind.multiuser.controller.MultiuserFramework;
import edu.cmu.inmind.multiuser.controller.MultiuserFrameworkContainer;
import edu.cmu.inmind.multiuser.controller.ShutdownHook;
import edu.cmu.inmind.multiuser.controller.log.MessageLog;
import edu.cmu.inmind.multiuser.controller.plugin.PluginModule;
import edu.cmu.inmind.multiuser.controller.resources.Config;
import edu.cmu.inmind.multiuser.sara.log.ExceptionLogger;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Created by oscarr on 3/20/17.
 * Main class for MUF 2.8+
 */
public class MainClassBase {
    protected MultiuserFramework muf;

    protected void execute() throws Throwable{
         execute( null );
    }

    protected void execute(List<ShutdownHook> hooks) throws Throwable{
        // starting the Multiuser framework
        muf = MultiuserFrameworkContainer.startFramework(
                createModules(), createConfig(), null );
        if( hooks != null ){
            for( ShutdownHook hook : hooks ){
                muf.addShutDownHook( hook );
            }
        }

        // just in case you force the system to close or an unexpected error happen.
        Runtime.getRuntime().addShutdownHook(new Thread("ShutdownThread") {
            public void run() {
                MultiuserFrameworkContainer.stopFramework( muf );
            }
        });

        // you can use a loop like this in order to gracefully shutdown the system.
        String command = "";
        while (!command.equals("shutdown")) {
            System.err.println("Type \"shutdown\" to stop:");
            Scanner scanner = new Scanner(System.in);
            command = scanner.nextLine();
            if (command.equals(SaraCons.SHUTDOWN)) {
                MultiuserFrameworkContainer.stopFramework( muf );
            }
        }
    }

    protected PluginModule[] createModules() {
        //TODO
        return null;
    }

    protected Config createConfig() {
        return new Config.Builder()
                // you can add values directly like this:
                .setSessionManagerPort(5555)
                .setDefaultNumOfPoolInstances(10)
                        // or you can refer to values in your config.properties file:
                .setPathLogs(Utils.getProperty("pathLogs"))
                .setSessionTimeout(5, TimeUnit.DAYS) // dirty workaround for broken close-session
                .setServerAddress("127.0.0.1") //use IP instead of 'localhost'
                .setExceptionTraceLevel( Constants.SHOW_ALL_EXCEPTIONS)
                .setExceptionLogger( getExceptionLogger() )// MUF Exceptions/NON_MUF Exceptions
                .build();
    }

    protected MessageLog getExceptionLogger(){
        MessageLog log =  new ExceptionLogger();
        log.setId( String.valueOf(System.currentTimeMillis() ) );
        log.setPath( Utils.getProperty("pathExceptionLog") );
        return log;
    }

}
