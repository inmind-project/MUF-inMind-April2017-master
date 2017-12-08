package edu.cmu.inmind.multiuser.sara;

import edu.cmu.inmind.multiuser.controller.common.Constants;
import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.controller.common.Utils;
import edu.cmu.inmind.multiuser.controller.log.MessageLog;
import edu.cmu.inmind.multiuser.controller.muf.MUFLifetimeManager;
import edu.cmu.inmind.multiuser.controller.muf.MultiuserController;
import edu.cmu.inmind.multiuser.controller.muf.ShutdownHook;
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
    protected MultiuserController muf;

    protected void execute() throws Throwable{
         execute( null );
    }

    public void execute(List<ShutdownHook> hooks) throws Throwable{
        // starting the Multiuser framework
        muf = MUFLifetimeManager.startFramework(
                createModules(), createConfig(), null );
        if( hooks != null ){
            for( ShutdownHook hook : hooks ){
                muf.addShutDownHook( hook );
            }
        }

        // just in case you force the system to close or an unexpected error happen.
        Runtime.getRuntime().addShutdownHook(new Thread("ShutdownThread") {
            public void run() {
                MUFLifetimeManager.stopFramework( muf );
            }
        });

        // you can use a loop like this in order to gracefully shutdown the system.
        String command = "";
        Scanner scanner = new Scanner(System.in);
        while (!command.equals("shutdown")) {
            if(scanner.hasNextLine()) {
                command = scanner.nextLine();
                if (command.equals(SaraCons.SHUTDOWN)) {
                    MUFLifetimeManager.stopFramework(muf);
                }
                System.err.println("Type " + SaraCons.SHUTDOWN + " to stop:");
            }
            else
            {
                Utils.sleep(300);
                System.err.println("Type " + SaraCons.SHUTDOWN + " to stop:");
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
                .setSessionManagerPort(Integer.valueOf(Utils.getProperty("SessionManagerPort")))
                .setDefaultNumOfPoolInstances(10)
                .setNumOfSockets(1)
                        // or you can refer to values in your config.properties file:
                .setPathLogs(Utils.getProperty("pathLogs"))
                .setCorePoolSize(1000)
                .setSessionTimeout(5, TimeUnit.DAYS) // dirty workaround for broken close-session
                .setServerAddress("tcp://127.0.0.1") //use IP instead of 'localhost'
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
