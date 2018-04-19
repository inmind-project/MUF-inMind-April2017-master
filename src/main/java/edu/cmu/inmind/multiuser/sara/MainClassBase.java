package edu.cmu.inmind.multiuser.sara;

import edu.cmu.inmind.multiuser.controller.common.Constants;
import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.controller.common.Utils;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.log.MessageLog;
import edu.cmu.inmind.multiuser.controller.muf.MUFLifetimeManager;
import edu.cmu.inmind.multiuser.controller.muf.MultiuserController;
import edu.cmu.inmind.multiuser.controller.muf.ShutdownHook;
import edu.cmu.inmind.multiuser.controller.plugin.PluginModule;
import edu.cmu.inmind.multiuser.controller.resources.Config;
import edu.cmu.inmind.multiuser.sara.log.ExceptionLogger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        System.err.println("Type " + SaraCons.SHUTDOWN + " to stop:");
        while (!command.equals(SaraCons.SHUTDOWN)) {
            if(scanner.hasNextLine()) {
                command = scanner.nextLine();
                if (command.equals(SaraCons.SHUTDOWN)) {
                    MUFLifetimeManager.stopFramework(muf);
                }
                System.err.println("Type " + SaraCons.SHUTDOWN + " to stop:");
            } else {
                Utils.sleep(300);
            }
        }
    }

    protected PluginModule[] createModules() {
        //TODO
        return null;
    }

    protected Config createConfig() {
        final String logDir = Utils.getProperty("pathLogs");
        ensureExists(logDir, "Log dir path");
        return new Config.Builder()
                // you can add values directly like this:
                .setSessionManagerPort(Integer.valueOf(Utils.getProperty("SessionManagerPort")))
                .setDefaultNumOfPoolInstances(10)
                .setNumOfSockets(1)
                        // or you can refer to values in your config.properties file:
                .setPathLogs(logDir)
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
        // Ensure that the exception log exists
        final String exLogDir = Utils.getProperty("pathExceptionLog");
        ensureExists(exLogDir,"Exception log dir path");
        log.setPath(exLogDir);
        return log;
    }

    private void ensureExists(final String dir, final String desc) {
        try {
            final Path absDirPath = Files.createDirectories(Paths.get(dir).toAbsolutePath().normalize());
            Log4J.warn(this, String.format("%s \"%s\" did not exist; Created.", desc, absDirPath));
        } catch (FileAlreadyExistsException e) {
            // Do nothing
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
