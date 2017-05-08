package beat;

import beat.beat.gui.ColorTheme;

public class Config {
    /**
     * VHMsg server host
     */
    public static String VHMSG_SERVER_URL = "128.237.210.199";
    //public static String VHMSG_SERVER_URL = "localhost";
    /**
     * Unity server ip
     */
    public static String UNITY_SERVER_URL = "localhost";
    /**
     * Path to behavior rules XML
     */
    public static final String XMLDIR="XMLData/";
    /**
     * Filename of knowledgebase
     */
    public static final String KNOWLEGEBASE_FILE = "database.xml";
    /**
     * Connecting to VHMsg or not
     */
    public static boolean isConnectVHMsg = false;
    /**
     * Color theme
     */
    public static ColorTheme colorTheme = ColorTheme.BRIGHT;
    /**
     * file name of NLG generation guidance
     */
    public static final String NLG_Database="nlg_formatted_utterances.tsv";
    public static String agentName = "Brad";
    /**
     * Stanfprd POS tagger
     */
    public static final String POSTAGGER_HOST = "localhost";
    public static final int    POSTAGGER_PORT = 9000; //9090
    /**
     *
     */
    public static final String BML_FILE = "BML/sentence-bml.tsv";
    /**
     *
     */
    public static final int AUTOMATIC_MODE = 0;
    public static final int MANUAL_MODE = 1;
    public static final int HYBRID_MODE = 2;
    /**
     *
     */
    public static int mode = AUTOMATIC_MODE;
    /**
     * Log
     */
    public static boolean logging = false;
    /**
     * TTS
     */
    public static String accessKey = "GDNAJRE2KBPMBQCEU2CQ";
    public static String secretKey = "rCDTav82VFzUsmPeSP8jxj+mlFAhhfCT9i71DCsm";

    //public static String soundPath = "C:\\vhtoolkit\\data\\cache\\audio\\";
    public static String soundPath = "/Users/yoichimatsuyama/VHToolkit/data/cache/audio/";

    public static final int RUN_ON_WINDOWS = 0;
    public static final int RUN_ON_MAC = 1;
    public static int OPERATION_SYSTEM = RUN_ON_MAC;

    public static String getOutputSoundFileName(){
        String path = "output/speech.wav";
        if(OPERATION_SYSTEM == RUN_ON_MAC) path = "output/speech.mp3";
        else if(OPERATION_SYSTEM == RUN_ON_WINDOWS) path = "output\\speech.mp3";
        return path;
    }

    public static String getSpeechMarkFileName(){
        String path = "output/speech.mark";
        if(OPERATION_SYSTEM == RUN_ON_MAC) path = "output/speech.mark";
        else if(OPERATION_SYSTEM == RUN_ON_WINDOWS) path = "output\\speech.mark";
        return path;
    }

    public static boolean onCache = false;
    public static String cacheHashMapFile = "resources/cache.tsv";
}
