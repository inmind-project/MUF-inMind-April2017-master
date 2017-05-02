package edu.cmu.inmind.multiuser.socialreasoner.control;

import edu.cmu.inmind.multiuser.socialreasoner.control.controllers.*;
import edu.cmu.inmind.multiuser.socialreasoner.control.reasoners.*;
import edu.cmu.inmind.multiuser.socialreasoner.control.util.Utils;
import edu.cmu.inmind.multiuser.socialreasoner.model.history.UserCSHistory;
import edu.cmu.inmind.multiuser.socialreasoner.model.intent.SystemIntent;
import edu.cmu.inmind.multiuser.socialreasoner.model.*;
import edu.cmu.inmind.multiuser.socialreasoner.model.blackboard.Blackboard;
import edu.cmu.inmind.multiuser.socialreasoner.model.history.SocialHistory;

import java.io.*;
import java.util.*;

/**Questions
 * Created by oscarr on 4/22/16.`
 */
public class MainController{
    public static boolean verbose = false;
    public static boolean isBeginningConversation = true;
    public static boolean isNonVerbalWindowON = true;
    private static int smileCount;
    private static int gazeCount;
    private static int noSmileCount;
    private static int noGazeCount;
    private BehaviorNetworkController bnController;
    private SocialReasoner socialReasoner;
    public static UserCSHistory userCSHistory;
    public static SocialHistory socialHistory;

    private static boolean isFirstNonVerbal = true;
    public static boolean noInputFlag = true;
    public static Blackboard blackboard;
    public static String[] conversationalStrategies = new String[7];
    public static String behavior;
    public static boolean pause;
    public static MainController mainController;
    public static boolean stop = false;
    public static Queue<SystemIntent> intentsQueue ;
    public static Properties properties = new Properties();
    public static int numberOfTurnsThreshold = 10;
    public static int currentTurn = 0;
    public static String outputResults = "";


    //flags
    public static boolean useWoZFlag = false;
    public static boolean flagStart = true;
    public static boolean flagStop = false;
    public static boolean flagReset = false;
    public static boolean flagResetTR = false;
    public static boolean useVHTConnnector;
    public static boolean useTianjinEmulator;
    public static boolean useFakeNLU;
    public static boolean useTRNotWoZ;
    public static boolean useFSM;
    public static boolean useSRPlot;
    public static boolean useManualMode;
    public static boolean useDummyGoals;

    // preconditions
    public static String rapportDelta;
    public static String rapportLevel; // = Constants.HIGH_RAPPORT;
    public static double rapportScore = 2; // = 6;
    public static String userConvStrategy; // = Constants.VIOLATION_SOCIAL_NORM;
    public static String smile;
    public static String eyeGaze;

    //delays
    public static long delayMainLoop;
    public static long delayUserIntent;
    private static long delaySystemIntent;

    private boolean isFirstTime = true;
    private boolean isProcessingIntent;
    private SystemIntent previousIntent;
    private String jsonResults;
    public static SystemIntent trIntent;
    private static String availableSharedExp;
    private static Double percSmileWindow;
    private static Double percGazeWindow;
    private String pathToAnnotatedLog;
    private String pathToExcelOutput;

    public MainController(){
        System.out.println("Controller instanciated");

        loadProperties();
        intentsQueue = new LinkedList<>();
        checkStart();
    }

    public SocialReasoner getSocialReasoner(){
        return socialReasoner;
    }

    public double getRapportScore(){
        return rapportScore;
    }

    public void setUserConvStrategy(String cs){
        userConvStrategy = cs;
    }

    public void setRapportScore(double rapport){
        rapportScore = rapport;
    }

    public void process(){
        if( !pause ) {
            try {
                while (intentsQueue.size() > 0) {
                    isProcessingIntent = true;
                    trIntent = intentsQueue.poll();
                    setNonVerbalWindow(false);
                    addContinousStates(trIntent);
                    socialReasoner.execute(trIntent);
                    printOutput();
                    resetStates();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                isProcessingIntent = false;
            }
            isBeginningConversation = false;
        }
        checkReset();
    }

    private void resetStates() {
        if(verbose) System.out.println("*** resetStates");
        smile = null;
        eyeGaze = null;
    }

    private void printOutput() {
        String output = removeSufix(trIntent.getIntent() + "\t" + rapportScore  + "\t" + userConvStrategy  + "\t" + smile  + "\t"
                + eyeGaze + "\t" + availableSharedExp + "\t" + getConvStrategyFormatted()  + "\t" + Arrays.toString(conversationalStrategies)
                + "\t Liste intentions " + socialReasoner.getOutput() + "\t" + socialReasoner.getStates() + "\t" + socialReasoner.getMatches()) + "\n";
        if( verbose ) {
            System.out.print(output);
        }
        outputResults += output;

    }

    public String getConvStrategyFormatted() {
        return conversationalStrategies[0].equals( Constants.ACK_SYSTEM_CS )? Constants.ACK_SYSTEM_CS + " -> "
                + conversationalStrategies[1] : conversationalStrategies[0];
    }

    private String removeSufix(String s) {
        return s.replace( "_NONVERBAL", "").replace("_SYSTEM_CS", "").replace("_USER_CS", "");
    }

    private void createSocialReasoner() {
        socialReasoner = SocialReasoner.getInstance(bnController, "ConversationalStrategyBN");
    }

    private void checkStart() {
        // waiting for confirmation to start the reasoning process
        while( !flagStart ){
            Utils.sleep( 100 );
        }
        if( flagStart || isFirstTime ){
            System.out.println("\nRe-starting...");
            intentsQueue = new LinkedList<>();
            MainController.userCSHistory = UserCSHistory.getInstance();

            //singletons
            blackboard = Blackboard.getInstance();
            socialHistory = SocialHistory.getInstance();
            System.out.println("Creating a user model...... Done!!!!!!!!!!!!!!\n\n");

            bnController = new ConversationalStrategyBN();
            blackboard.setModel( bnController.getStatesList() );
            blackboard.subscribe((ConversationalStrategyBN) bnController);
            userConvStrategy = Constants.NONE_USER_CS; //model.getInitialUserConvStrat();
            rapportLevel = calculateRapScore( "2" );//model.getInitialRapportLevel()

            createSocialReasoner();
            isFirstTime = false;
            flagReset = false;
        }
    }

    private void checkReset() {
        if( flagReset ){
            Blackboard.reset();
            //TaskReasoner.reset();
            SocialReasoner.reset();
            SocialHistory.reset();
            UserCSHistory.reset();
            noInputFlag = true;
            conversationalStrategies = new String[7];
            pause = false;
            stop = false;
            intentsQueue.clear();
            intentsQueue = null;
            flagStop = false;
            flagStart = false;
            flagResetTR = false;
            System.out.println("Reseting...");
            reset();
            System.gc();
            mainController.loadProperties();
            outputResults = "";
        }
    }

    private void reset() {
        bnController = null;
        socialReasoner = null;
        noInputFlag = true;
        socialHistory = null;
        conversationalStrategies = new String[7];
        intentsQueue = null;
        currentTurn = 0;
    }

    public static String calculateRapScore(String score) {
        try {
            double scoreTemp = Double.parseDouble(score);
            rapportDelta = scoreTemp == rapportScore? Constants.RAPPORT_MAINTAINED : scoreTemp > rapportScore?
                    Constants.RAPPORT_INCREASED : Constants.RAPPORT_DECREASED;
            rapportScore = scoreTemp;
            rapportLevel = (rapportScore > 4.4? Constants.HIGH_RAPPORT : rapportScore < 3 ? Constants.LOW_RAPPORT
                    : Constants.MEDIUM_RAPPORT);
            return rapportLevel;
        }catch (Exception e){
            return null;
        }
    }

    public static void setNonVerbals(String isSmiling, String whereEyeGaze ){
        if( isNonVerbalWindowON ){
            if( isSmiling.equals("smile") || isSmiling.equals("true") ){
                smileCount++;
            }else{
                noSmileCount++;
            }
            if( whereEyeGaze.equals("gaze_partner") || whereEyeGaze.equals("true") ){
                gazeCount++;
            }else{
                noGazeCount++;
            }
        }
    }

    public static void calculateNonVerbals(){
        if( isFirstNonVerbal ){
            smile = Constants.NOT_SMILE_NONVERBAL;
            eyeGaze = Constants.GAZE_ELSEWHERE_NONVERBAL;
            isFirstNonVerbal = false;
        }else {
            smile = smileCount >= (smileCount + noSmileCount) * percSmileWindow / 100.0 ? Constants.SMILE_NONVERBAL
                    : Constants.NOT_SMILE_NONVERBAL;
            eyeGaze = gazeCount >= (gazeCount + noGazeCount) * percGazeWindow / 100.0 ? Constants.GAZE_PARTNER_NONVERBAL
                    : Constants.GAZE_ELSEWHERE_NONVERBAL;
        }
        if(verbose)
            System.out.println("*** smileCount: " + smileCount + " noSmileCount: " + noSmileCount + " gazeCount: "
                + gazeCount + " noGazeCount: " + noGazeCount + " smile: " + smile + " eyeGaze: " + eyeGaze);
        smileCount = 0;
        gazeCount = 0;
        noSmileCount = 0;
        noGazeCount = 0;
    }

    public static void setNonVerbalWindow( boolean flag ){
        if(verbose) System.out.println("*** set window: " + flag);
        isNonVerbalWindowON = flag;
        if( !isNonVerbalWindowON ){
            calculateNonVerbals();
        }
    }

    public static void setNonVerbals(boolean isSmiling, boolean isGazeAtPartner ){
        smile = isSmiling? Constants.SMILE_NONVERBAL : Constants.NOT_SMILE_NONVERBAL;
        eyeGaze = isGazeAtPartner? Constants.GAZE_PARTNER_NONVERBAL : Constants.GAZE_ELSEWHERE_NONVERBAL;
    }

    public void addContinousStates( SystemIntent intent ) {
        if(intent != null ) {
            addNewIntent(intent);
            previousIntent = intent;
        }
        //We don't need current system CS, just system history CS
        //addSystemCSstates();
        socialHistory.addStates();
        userCSHistory.addStates();
        addUserCSstates();
        addRapportstates();
        addTurnstates();
        addNonVerbals();
    }

    private void addNewIntent(SystemIntent intent) {
        if( previousIntent != null ) {
            blackboard.removeMessages(previousIntent.getIntent() + ":" + previousIntent.getPhase());
        }
        blackboard.setStatesString(intent.getIntent() + ":" + intent.getPhase(), "MainController" );
    }

    private void addUserCSstates() {
        blackboard.removeMessagesContain( "USER_CS");
        blackboard.setStatesString( userConvStrategy, "mainController");
    }

    private void addSystemCSstates() {
        blackboard.removeMessagesContain( "SYSTEM_CS");
        blackboard.setStatesString( conversationalStrategies[0], "mainController");
    }

    private void addRapportstates() {
        blackboard.removeMessagesContain( "RAPPORT");
        blackboard.setStatesString( rapportDelta + ":" + rapportLevel, "mainController");
    }

    private void addTurnstates() {
        blackboard.removeMessagesContain( "NUM_TURNS");
        blackboard.setStatesString( currentTurn <= numberOfTurnsThreshold ? Constants.NUM_TURNS_LOWER_THAN_THRESHOLD
                : Constants.NUM_TURNS_HIGHER_THAN_THRESHOLD, "DMMain");
        if( currentTurn > numberOfTurnsThreshold){
            currentTurn = 0;
        }
    }

    private void addNonVerbals() {
        if( smile != null && eyeGaze != null ){
            blackboard.removeMessagesContain( "NONVERBAL");
            blackboard.setStatesString( smile + ":" + eyeGaze, "RapportEstimator");
        }
    }


    private void loadProperties(){
        InputStream input = null;
        try {
            input = new FileInputStream("config.properties");
            // load a properties file
            properties.load(input);

            // get the property value and print it out
            useVHTConnnector = Boolean.valueOf(properties.getProperty("useVHTConnnector"));
            useTianjinEmulator = Boolean.valueOf(properties.getProperty("useTianjinEmulator"));
            useFakeNLU = Boolean.valueOf(properties.getProperty("useFakeNLU"));
            useTRNotWoZ = Boolean.valueOf(properties.getProperty("useTRNotWoZ"));
            useFSM = Boolean.valueOf(properties.getProperty("useFSM"));
            useSRPlot = Boolean.valueOf(properties.getProperty("useSRPlot"));
            useManualMode = Boolean.valueOf(properties.getProperty("useManualMode"));
            //delayMainLoop = Long.valueOf(properties.getProperty("delayMainLoop"));
            //delayUserIntent = Long.valueOf(properties.getProperty("delayUserIntent"));
            //delaySystemIntent = Long.valueOf(properties.getProperty("delaySystemIntent"));
            useDummyGoals = Boolean.valueOf(properties.getProperty("useDummyGoals"));
            flagStart = Boolean.valueOf(properties.getProperty("shouldStartAutomatically"));
            //numberOfTurnsThreshold = Integer.valueOf(properties.getProperty("numberOfTurnsThreshold"));
            percSmileWindow = Double.valueOf(properties.getProperty("percSmileWindow"));
            percGazeWindow = Double.valueOf(properties.getProperty("percGazeWindow"));
            //verbose = Boolean.valueOf(properties.getProperty("verbose"));
            pathToAnnotatedLog = properties.getProperty("pathToAnnotatedLog");
            pathToExcelOutput = properties.getProperty("pathToExcelOutput");
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //TODO: we still need to receive ACK, QESD and Hedging
    public void calculateUserConvStrategy(String message) {
        String[] values = message.split(" ");
        String agentId = values[0];
        boolean sd = Boolean.parseBoolean( values[2] );
        boolean se = Boolean.parseBoolean( values[5] );
        boolean pr = Boolean.parseBoolean( values[8] );
        boolean vsn = Boolean.parseBoolean( values[11] );
        boolean asn = Boolean.parseBoolean( values[14] );
        userConvStrategy =  sd? Constants.SD_USER_CS : se? Constants.RSE_USER_CS : pr? Constants.PR_USER_CS : vsn?
                Constants.VSN_USER_CS : asn? Constants.ASN_USER_CS : Constants.NOT_PR_USER_CS;
        blackboard.setStatesString( userConvStrategy, "ConversationalStrategyClassifier");

        String winner = "NONE";
        double max = 0;
        for(int i = 3; i < 11; i=i+3) {
            if (values[i] != null && Double.valueOf(values[i]) > max) {
                max = Double.valueOf(values[i]);
                winner = values[i-2];
            }
        }
        userCSHistory.add( System.currentTimeMillis(), winner, rapportLevel, rapportScore);
    }

    public static void setAvailableSE(boolean availableSharedExperiences) {
        if( availableSharedExperiences ) {
            blackboard.setStatesString( Constants.AVAILABLE_SHARED_EXPERIENCES, "DMMain");
            availableSharedExp = "AVAILABLE";
            return;
        }
        availableSharedExp = "NOT_AVAILABLE";
    }

    public void addSystemIntent(String message) {
        try {
            String[] split = message.split(" ");
            boolean isSet = extractTypeOfTRmessage(split[0]);
            if(verbose) System.out.println("message: " + split[0]);
            if( isSet) {
                intentsQueue.add(new SystemIntent(split[2], split[1]));
                jsonResults = split[3];
                process();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean extractTypeOfTRmessage(String message) {
        if( message.contains("start") ) {
            MainController.flagStart = true;
        }else if( message.contains("set") ){
            return true;
        } else if( message.contains( "reset" ) ){
            MainController.flagReset = true;
            return true;
        }else if( message.contains("stop") ){
            MainController.stop = true;
        }
        return false;
    }
}
