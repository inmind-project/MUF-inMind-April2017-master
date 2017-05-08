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

import edu.cmu.inmind.multiuser.common.Utils;

public class Config {
	//public static String VHMSG_SERVER_URL = "128.237.219.190"; //
	//public static String VHMSG_SERVER_URL = "192.168.1.133"; //windows (Starbug)

	/************************************************************************/
	/************************************************************************/
	/***     DO NOT USE HARD CODED VALUES, USE CONFIG.PROPERTIES INSTEAD ****/
	/************************************************************************/
	/************************************************************************/


	public static String VHMSG_SERVER_URL = Utils.getProperty("VHMSG_SERVER_URL");

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
	public static String SCENARIO_FILE = Utils.getProperty("SCENARIO_FILE");
	
	/**
	 * sentence file name
	 */
	public static String SENTENCE_FILE = Utils.getProperty("SENTENCE_FILE");
	
	
	
	public static final int LAUNCH_MAC = 0;
	public static final int LAUNCH_WIN = 1;
	/**
	 * Mac or Windows
	 */
	public static int OSType = LAUNCH_MAC;
	
	
	/**
	 * MySQL setting
	 */
	public static String mySqlPath = Utils.getProperty("mySqlPath");
	public static String DbName = Utils.getProperty("DbName");
	public static String SqlUserName = Utils.getProperty("SqlUserName");
	public static String SqlPassword = Utils.getProperty("SqlPassword");
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
