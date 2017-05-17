package edu.cmu.inmind.multiuser.sara.examples;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.common.Utils;
import edu.cmu.inmind.multiuser.controller.MultiuserFramework;
import edu.cmu.inmind.multiuser.controller.plugin.PluginModule;
import edu.cmu.inmind.multiuser.controller.resources.Config;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Created by oscarr on 3/20/17.
 */
public class MainClass {

    /**
     * This method controls the whole app. If shutdown is entered, it will completely stop the system.
     */
    public static void main(String args[]) {
        new MainClass().execute();
    }

    protected void execute() {
        // starting the Multiuser framework
        Config config = createConfig();
        MultiuserFramework.start(createModules(), createConfig());

        // just in case you force the system to close or an unexpected error happen.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                MultiuserFramework.stop();
            }
        });

        // you can use a loop like this in order to gracefully shutdown the system.
        String command = "";
        while (!command.equals("shutdown")) {
            System.err.println("Type \"shutdown\" to stop:");
            Scanner scanner = new Scanner(System.in);
            command = scanner.nextLine();
            if (command.equals(SaraCons.SHUTDOWN)) {
                MultiuserFramework.stop();
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
                .setSessionTimeout(10, TimeUnit.DAYS)
                .setServerAddress("127.0.0.1") //use IP instead of 'localhost'
                .setShouldShowException(true)
                .build();
    }

}
