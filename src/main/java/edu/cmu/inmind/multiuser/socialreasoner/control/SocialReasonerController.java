package edu.cmu.inmind.multiuser.socialreasoner.control;

import edu.cmu.inmind.multiuser.socialreasoner.control.emulators.Emulator;
import edu.cmu.inmind.multiuser.socialreasoner.control.reasoners.SocialReasoner;
import edu.cmu.inmind.multiuser.socialreasoner.control.util.Utils;
import edu.cmu.inmind.multiuser.socialreasoner.model.Constants;
import edu.cmu.inmind.multiuser.socialreasoner.model.intent.SystemIntent;
import edu.cmu.inmind.multiuser.socialreasoner.control.bn.BehaviorNetworkController;
import edu.cmu.inmind.multiuser.socialreasoner.control.bn.ConversationalStrategyBN;
import edu.cmu.inmind.multiuser.socialreasoner.model.history.UserCSHistory;
import edu.cmu.inmind.multiuser.socialreasoner.model.blackboard.Blackboard;
import edu.cmu.inmind.multiuser.socialreasoner.model.history.SocialHistory;


import java.io.*;
import java.util.*;

/**
 * Created by oscarr on 4/22/16.
 */
public class SocialReasonerController {
    public static boolean verbose = true;
    public static boolean isBeginningConversation = true;
    public static boolean isNonVerbalWindowON = true;
    private static int smileCount;
    private static int gazeCount;
    private static int noSmileCount;
    private static int noGazeCount;
    public static String userUtterance = "";
    public static String systemUtterance = "";
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
    public static SocialReasonerController socialReasonerController;
    public static boolean stop = false;
    public static Queue<SystemIntent> intentsQueue;
    public static Properties properties = new Properties();
    public static int numberOfTurnsThreshold = 10;
    public static int currentTurn = 0;
    public static String outputResults = "";
    public static long vectorID = 0L;


    //flags
    public static boolean flagStart = false;
    public static boolean flagStop = false;
    public static boolean flagReset = false;
    public static boolean flagResetTR = false;
    public static boolean useVHTConnnector;
    public static boolean useManualMode;
    public static boolean useDummyGoals;

    // preconditions
    public static String rapportDelta;
    public static String rapportLevel;
    public static double rapportScore = 2.0;
    public static String userConvStrategy;
    public static String smile;
    public static String eyeGaze;

    //delays
    public static long delayMainLoop;
    public static long delayUserIntent;


    private boolean isFirstTime = true;
    private boolean isProcessingIntent;
    private SystemIntent previousIntent;

    public static SystemIntent trIntent;
    private static String availableSharedExp = "NOT_AVAILABLE";
    private static Double percSmileWindow;
    private static Double percGazeWindow;
    public static String pathToDavosData;
    public static String pathToDavosResults;
    public static String pathToExcelOutput;
    private static String executeEmulator;
    public static String wozerOutput;
    private static Scanner scanner;
    private static Emulator emulator;

    public SocialReasonerController() {
        loadProperties();
        checkStart();
    }

    public static void main(String args[]) {
        socialReasonerController = new SocialReasonerController();
        scanner = new Scanner(System.in);
        emulator = new Emulator();
        emulator.setSocialReasonerController(socialReasonerController);
        while (!stop) {
            if (executeEmulator.equals("console")) {
                executeConsoleMode();
            } else {
                executeScriptMode();
            }
        }
        System.out.println("\n|-- DONE");
        System.exit(0);
    }


    private static void executeConsoleMode() {
        System.out.println("\nEnter an intent ('stop' to exit): ");
        String command = scanner.nextLine();
        if (command.equals("stop")) {
            stop = true;
        } else {
            socialReasonerController.addSystemIntent(new SystemIntent(command, "phase"));
        }
    }

    private static void executeScriptMode() {
        stop = emulator.execute() == null;
    }

    public void setRapportScore(double rapport) {
        rapportScore = rapport;
    }

    public void process() {
        if (!pause) {
            try {
                while (intentsQueue.size() > 0) {
                    isProcessingIntent = true;
                    trIntent = intentsQueue.poll();
                    setNonVerbalWindow(true);
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
        smile = null;
        eyeGaze = null;
    }

    private void printOutput() {
        String output = removeSufix(trIntent.getIntent() + "\t" + rapportScore + "\t" + rapportLevel
                + "\t" + rapportDelta + "\t" + userConvStrategy + "\t" + smile + "\t" + eyeGaze + "\t" + availableSharedExp
                + "\t" + blackboard.search("NUM_TURNS") + "\t" + blackboard.search(Constants.ASN_HISTORY_SYSTEM)
                + "\t" + blackboard.search(Constants.VSN_HISTORY_SYSTEM) + "\t" + blackboard.search(Constants.SD_HISTORY_SYSTEM)
                + "\t" + blackboard.search(Constants.QESD_HISTORY_SYSTEM) + "\t" + blackboard.search(Constants.PR_HISTORY_SYSTEM)
                + "\t" + blackboard.search(Constants.ACK_HISTORY_SYSTEM) + "\t" + blackboard.search(Constants.RSE_HISTORY_SYSTEM)
                + "\t" + blackboard.search(Constants.ASN_HISTORY_USER) + "\t" + blackboard.search(Constants.VSN_HISTORY_USER)
                + "\t" + blackboard.search(Constants.SD_HISTORY_USER) + "\t" + blackboard.search(Constants.QESD_HISTORY_USER)
                + "\t" + blackboard.search(Constants.PR_HISTORY_USER) + "\t" + blackboard.search(Constants.ACK_HISTORY_USER)
                + "\t" + blackboard.search(Constants.RSE_HISTORY_USER) + "\t" + (wozerOutput + "_WOZER") + "\t" + (getConvStrategyFormatted() + "_SR")
                + "\t" + Arrays.toString(conversationalStrategies) + "\t" + socialReasoner.getOutput())
                + "\t" + socialReasoner.getStates() + "\t" + socialReasoner.getMatches() + "\t" + userUtterance
                + "\t" + systemUtterance + "\n";
        userUtterance = "";
        systemUtterance = "";
        if (verbose) {
            System.out.print("   |-- BLACKBOARD CONTENT: " + blackboard.toString());
        }
        outputResults += output;
        vectorID++;
    }

    public String getConvStrategyFormatted() {
        if (conversationalStrategies == null || conversationalStrategies[0] == null) {
            return Constants.NONE;
        }
        return conversationalStrategies[0].equals(Constants.ACK_SYSTEM_CS) ? Constants.ACK_SYSTEM_CS + " -> "
                + conversationalStrategies[1] : conversationalStrategies[0];
    }

    private String removeSufix(String s) {
        return s.replace("_NONVERBAL", "");//.replace("_SYSTEM_CS", "").replace("_USER_CS", "");
    }

    private void createSocialReasoner() {
        socialReasoner = SocialReasoner.getInstance(bnController, "ConversationalStrategyBN");
    }

    private void checkStart() {
        // waiting for confirmation to process the reasoning process
        while (!flagStart) {
            Utils.sleep(100);
        }
        if (flagStart || isFirstTime) {
            System.out.println("\nStarting Social Reasoner...");
            intentsQueue = new LinkedList<>();
            SocialReasonerController.userCSHistory = UserCSHistory.getInstance();

            //singletons
            blackboard = Blackboard.getInstance();
            socialHistory = SocialHistory.getInstance();

            bnController = new ConversationalStrategyBN();
            blackboard.setModel(bnController.getStatesList());
            blackboard.subscribe((ConversationalStrategyBN) bnController);
            userConvStrategy = Constants.NONE_USER_CS;
            rapportLevel = calculateRapScore("2");
            createSocialReasoner();
            isFirstTime = false;
            flagReset = false;
        }
    }

    private void checkReset() {
        if (flagReset) {
            Blackboard.reset();
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
            socialReasonerController.loadProperties();
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
            rapportDelta = scoreTemp >= (rapportScore + .05) ? Constants.RAPPORT_INCREASED : scoreTemp <= (rapportScore - .05) ?
                    Constants.RAPPORT_DECREASED : Constants.RAPPORT_MAINTAINED;
            rapportScore = scoreTemp;
            rapportLevel = (rapportScore > 4.4 ? Constants.HIGH_RAPPORT : rapportScore < 3 ? Constants.LOW_RAPPORT
                    : Constants.MEDIUM_RAPPORT);
            return rapportLevel;
        } catch (Exception e) {
            return null;
        }
    }

    public static void setNonVerbals(String isSmiling, String whereEyeGaze) {
        if (isNonVerbalWindowON) {
            if (isSmiling.equals("smile") || isSmiling.equals("true")) {
                smileCount++;
            } else {
                noSmileCount++;
            }
            if (whereEyeGaze.equals("gaze_partner") || whereEyeGaze.equals("true")) {
                gazeCount++;
            } else {
                noGazeCount++;
            }
        }
    }

    public static void setNonVerbals(boolean isSmiling, boolean isGazeAtPartner) {
//        smile = isSmiling? Constants.SMILE_NONVERBAL : Constants.NOT_SMILE_NONVERBAL;
//        eyeGaze = isGazeAtPartner? Constants.GAZE_PARTNER_NONVERBAL : Constants.GAZE_ELSEWHERE_NONVERBAL;
        if (isNonVerbalWindowON) {
            if (isSmiling) {
                smileCount++;
            } else {
                noSmileCount++;
            }
            if (isGazeAtPartner) {
                gazeCount++;
            } else {
                noGazeCount++;
            }
        }
    }

    public static void calculateNonVerbals() {
        if (isFirstNonVerbal) {
            smile = Constants.NOT_SMILE_NONVERBAL;
            eyeGaze = Constants.GAZE_ELSEWHERE_NONVERBAL;
            isFirstNonVerbal = false;
        } else {
            smile = smileCount >= (smileCount + noSmileCount) * percSmileWindow / 100.0 ? Constants.SMILE_NONVERBAL
                    : Constants.NOT_SMILE_NONVERBAL;
            eyeGaze = gazeCount >= (gazeCount + noGazeCount) * percGazeWindow / 100.0 ? Constants.GAZE_PARTNER_NONVERBAL
                    : Constants.GAZE_ELSEWHERE_NONVERBAL;
        }
        if (verbose)
            System.out.println("|-- NON-VERBALS: smileCount: " + smileCount + " noSmileCount: " + noSmileCount + " gazeCount: "
                    + gazeCount + " noGazeCount: " + noGazeCount + " smile: " + smile + " eyeGaze: " + eyeGaze);
        smileCount = 0;
        gazeCount = 0;
        noSmileCount = 0;
        noGazeCount = 0;
    }

    /**
     * we need a time window in order to calculate smile, gaze, etc. as user's non-verbals last several seconds
     **/
    public static void setNonVerbalWindow(boolean flag) {
        isNonVerbalWindowON = flag;
        //TODO: when should we use calculateNonVerbals?
        if (isNonVerbalWindowON) {
            //calculateNonVerbals();
        }
    }

    public void addContinousStates(SystemIntent intent) {
        if (intent != null) {
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
        if (previousIntent != null) {
            blackboard.removeMessages(previousIntent.getIntent() + ":" + previousIntent.getPhase() + ":" + "greeting" + ":" + "greetings");
        }
        blackboard.setStatesString(intent.getIntent() + ":" + intent.getPhase(), "SocialReasonerController");
    }

    private void addUserCSstates() {
        blackboard.removeMessagesContain("USER_CS");
        blackboard.setStatesString(userConvStrategy, "socialReasonerController");
    }

    private void addSystemCSstates() {
        blackboard.removeMessagesContain("SYSTEM_CS");
        blackboard.setStatesString(conversationalStrategies[0], "socialReasonerController");
    }

    private void addRapportstates() {
        blackboard.removeMessagesContain("RAPPORT");
        blackboard.setStatesString(rapportDelta + ":" + rapportLevel, "socialReasonerController");
    }

    private void addTurnstates() {
        blackboard.removeMessagesContain("NUM_TURNS");
        blackboard.setStatesString(currentTurn <= numberOfTurnsThreshold ? Constants.NUM_TURNS_LOWER_THAN_THRESHOLD
                : Constants.NUM_TURNS_HIGHER_THAN_THRESHOLD, "DMMain");
        if (currentTurn > numberOfTurnsThreshold) {
            currentTurn = 0;
        }
    }

    private void addNonVerbals() {
        if (smile != null && eyeGaze != null) {
            blackboard.removeMessagesContain("NONVERBAL");
            blackboard.setStatesString(smile + ":" + eyeGaze, "RapportEstimator");
        }
    }

    private void loadProperties() {
        InputStream input = null;
        try {
            input = new FileInputStream("config.properties");
            // load a properties file
            properties.load(input);

            // get the property value and printFileName it out
            useVHTConnnector = Boolean.valueOf(properties.getProperty("useVHTConnnector"));
            useManualMode = Boolean.valueOf(properties.getProperty("useManualMode"));
            delayMainLoop = Long.valueOf(properties.getProperty("delayMainLoop"));
            delayUserIntent = Long.valueOf(properties.getProperty("delayUserIntent"));
            useDummyGoals = Boolean.valueOf(properties.getProperty("useDummyGoals"));
            flagStart = Boolean.valueOf(properties.getProperty("shouldStartAutomatically"));
            numberOfTurnsThreshold = Integer.valueOf(properties.getProperty("numberOfTurnsThreshold"));
            percSmileWindow = Double.valueOf(properties.getProperty("percSmileWindow"));
            percGazeWindow = Double.valueOf(properties.getProperty("percGazeWindow"));
            verbose = Boolean.valueOf(properties.getProperty("verbose"));
            pathToDavosData = properties.getProperty("pathToDavosData");
            pathToDavosResults = properties.getProperty("pathToDavosResults");
            pathToExcelOutput = properties.getProperty("pathToExcelOutput");
            executeEmulator = properties.getProperty("executeEmulator");
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
        boolean sd = Boolean.parseBoolean(values[2]);
        boolean se = Boolean.parseBoolean(values[5]);
        boolean pr = Boolean.parseBoolean(values[8]);
        boolean vsn = Boolean.parseBoolean(values[11]);
        boolean asn = Boolean.parseBoolean(values[14]);
        userConvStrategy = sd ? Constants.SD_USER_CS : se ? Constants.RSE_USER_CS : pr ? Constants.PR_USER_CS : vsn ?
                Constants.VSN_USER_CS : asn ? Constants.ASN_USER_CS : Constants.NONE_USER_CS;
        blackboard.setStatesString(userConvStrategy, "ConversationalStrategyClassifier");
        if (!userConvStrategy.equals(Constants.PR_USER_CS)) {
            blackboard.setStatesString(Constants.NOT_PR_USER_CS, "ConversationalStrategyClassifier");
        }

        String winner = "NONE";
        double max = 0;
        for (int i = 3; i < 11; i = i + 3) {
            if (values[i] != null && Double.valueOf(values[i]) > max) {
                max = Double.valueOf(values[i]);
                winner = values[i - 2];
            }
        }
        userCSHistory.add(System.currentTimeMillis(), winner, rapportLevel, rapportScore);
    }

    public static void setAvailableSE(boolean availableSharedExperiences) {
        if (availableSharedExperiences) {
            blackboard.setStatesString(Constants.AVAILABLE_SHARED_EXPERIENCES, "DMMain");
            availableSharedExp = "AVAILABLE";
            return;
        }
        availableSharedExp = "NOT_AVAILABLE";
    }

    public void addSystemIntent(SystemIntent systemIntent) {
        try {
            intentsQueue.add(systemIntent);
            process();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean extractTypeOfTRmessage(String message) {
        if (message.contains("process")) {
            SocialReasonerController.flagStart = true;
        } else if (message.contains("set")) {
            return true;
        } else if (message.contains("reset")) {
            SocialReasonerController.flagReset = true;
            return true;
        } else if (message.contains("stop")) {
            SocialReasonerController.stop = true;
        }
        return false;
    }

    public static void setUserConvStrategy(String userConvStrategy) {
        SocialReasonerController.userConvStrategy = userConvStrategy;
    }

    public SocialReasoner getSocialReasoner() {
        return socialReasoner;
    }

}