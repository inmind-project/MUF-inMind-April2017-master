package edu.cmu.inmind.multiuser.socialreasoner.control.emulators;

import edu.cmu.inmind.multiuser.socialreasoner.control.SocialReasonerController;

/**
 * Created by oscarr on 2/9/17.
 */
public class DavosEmulator extends TianjinEmulator {
    private static DavosEmulator instance;
    private DavosEmulator(){}

    public static DavosEmulator getInstance() {
        if (instance == null) {
            instance = new DavosEmulator();
        }
        return instance;
    }


    public void loadFile(String filePath){
        super.loadFile( SocialReasonerController.pathToDavosData + filePath);
    }
}
