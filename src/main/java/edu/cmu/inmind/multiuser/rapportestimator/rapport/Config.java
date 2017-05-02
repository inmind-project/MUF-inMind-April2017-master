/**
 * Copyright (C) Carnegie Mellon University - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * This is proprietary and confidential.
 * Written by members of the ArticuLab, directed by Justine Cassell, 2014.
 * 
 * @author Yoichi Matsuyama <yoichim@cs.cmu.edu>
 * 
 */
package edu.cmu.inmind.multiuser.rapportestimator.rapport;

public class Config {
	//public static String VHMSG_SERVER_URL = "128.237.219.190"; //
	//public static String VHMSG_SERVER_URL = "192.168.1.133"; //windows (Starbug)
	public static String VHMSG_SERVER_URL = "localhost";

	/**
	 * Number of participants
	 * 2: Dyadic (default)
	 * 3: Three participant
	 * 4: Four participant
	 */
	public static int PARTICIPANT_NUM = 1;

	/**
	 * Connecting to VHMsg or not
	 */
	public static boolean isConnectVHMsg = false;

	/**
	 * Connecting MySQL or not
	 */
	public static boolean isConnectingMySQL = false;
	
	/**
	 * Scenario is starting or not
	 */
	public static boolean isScenerioStart = false;
	
	/**
	 * Scenario script file name
	 */
	public static String SCENARIO_FILE = "scenario/scenario.txt";
	
	/**
	 * sentence file name
	 */
	public static String SENTENCE_FILE = "scenario/sentence.txt";
	
	
	
	public static final int LAUNCH_MAC = 0;
	public static final int LAUNCH_WIN = 1;
	/**
	 * Mac or Windows
	 */
	public static int OSType = LAUNCH_MAC;
	
	
	/**
	 * MySQL setting
	 */
	public static String mySqlPath = "localhost";
	public static String DbName = "rapport_sentence";
	public static String SqlUserName = "root";
	public static String SqlPassword = "root";
	public static String SqlUrl =
			"jdbc:mysql://"
					+ mySqlPath
					+ "/"
					+ DbName
					+ "?user="
					+ SqlUserName
					+"&password="
					+ SqlPassword;
}
