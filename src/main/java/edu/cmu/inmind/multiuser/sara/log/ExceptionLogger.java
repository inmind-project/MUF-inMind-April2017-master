package edu.cmu.inmind.multiuser.sara.log;

import java.io.File;
import java.io.PrintWriter;

import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.log.MessageLog;

/**
 * Created by oscarr on 8/23/17.
 *
 * Replace this class with your implementation to persist the logs (serialization, databases, etc.)
 */
public class ExceptionLogger implements MessageLog {
    private String id;
    private StringBuffer log;
    private String path = "";
    private boolean turnedOn = true;

    @Override
    public void setId(String id) {
        this.id = id;
        log = new StringBuffer();
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public void add(String messageId, String messageContent) {
        Log4J.warn(this, messageId + messageContent);
        if( turnedOn ) {
            log.append(String.format("%-20s %-20s %s", System.currentTimeMillis(), messageId, messageContent));
            log.append("\n");
        }
    }

    @Override
    public void store() throws Throwable{
        if( turnedOn && !log.toString().isEmpty() ) {
            File file = new File(path + id + ".log");
            Log4J.info(this, "storing log file to " + file.toString());
            PrintWriter printWriter = new PrintWriter(file);
            printWriter.write(log.toString());
            printWriter.flush();
            printWriter.close();
        }
    }

    @Override
    public void turnOn(boolean shouldTurnOn) {
        turnedOn = shouldTurnOn;
    }
}

