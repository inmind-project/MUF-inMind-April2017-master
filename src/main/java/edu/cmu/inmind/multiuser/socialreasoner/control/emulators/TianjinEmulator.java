package edu.cmu.inmind.multiuser.socialreasoner.control.emulators;

import edu.cmu.inmind.multiuser.socialreasoner.control.vht.VHTConnector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by oscarr on 1/4/17.
 */
public class TianjinEmulator {
    private boolean stop = false;
    private StringBuffer stringBuffer;
    private int position = 0;
    private VHTConnector vhtConnector;
    private static TianjinEmulator instance;

    private TianjinEmulator(){}

    public static TianjinEmulator getInstance() {
        if (instance == null) {
            instance = new TianjinEmulator();
        }
        return instance;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public void loadFile(String filePath){
        try {
            File file = new File(filePath);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                int idx = line.indexOf("vr");
                if( idx != -1 ) {
                    stringBuffer.append(line.substring(idx));
                    stringBuffer.append("\n");
                }
            }
            fileReader.close();
            vhtConnector = VHTConnector.getInstance();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessages(){
        while( !stop ){
            int posTemp = stringBuffer.indexOf( "\n", position );
            String message = stringBuffer.substring( position, posTemp );
                position = posTemp + 1;
            vhtConnector.sendMessage(message);
            if( message.startsWith("vrTaskReasoner set ") ){
                stop = true;
            }

//            if( vrTRtriggered && message.startsWith("vrSocialReasonerScore") ){
//                stop = true;
//            }
        }
    }
}
