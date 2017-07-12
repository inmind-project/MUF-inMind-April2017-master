package beat;

public class Config {
    /**
     * Path to behavior rules XML
     */
    public static final String XMLDIR="sarabeat/XMLData/";
    /**
     * Filename of knowledgebase
     */
    public static final String KNOWLEGEBASE_FILE = "database.xml";
    /**
     * Connecting to VHMsg or not
     */
    public static boolean isConnectVHMsg = false;
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
     * Log
     */
    public static boolean logging = false;
}
