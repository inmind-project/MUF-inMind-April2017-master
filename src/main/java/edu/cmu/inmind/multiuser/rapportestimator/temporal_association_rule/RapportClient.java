package edu.cmu.inmind.multiuser.rapportestimator.temporal_association_rule;

import edu.cmu.inmind.multiuser.controller.plugin.StatelessComponent;
import edu.cmu.inmind.multiuser.rapportestimator.rapport.Config;
import edu.cmu.inmind.multiuser.rapportestimator.vhmsg.main.VhmsgSender;
import edu.usc.ict.vhmsg.MessageEvent;
import edu.usc.ict.vhmsg.MessageListener;
import edu.usc.ict.vhmsg.VHMsg;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Trivial client for the date server.
 */
@StatelessComponent
public class RapportClient implements MessageListener {

	//RapportClient
	public static RapportClient client;
	
	
	// Last conversational strategy used by user or system
	public static String last_user_strategy;
	public static String last_system_strategy;
	public static boolean user_smile;
	public static double last_temporal_rule_result;
	public static double potential_rapport_score;

	// Handcrafted rule addition and substraction
	public static double addition;
	public static double substract;

	// Turn information
	public static int Turn_number = 0;

	// for subscription
	public static VHMsg vhmsgSubscriber;

	// for sending
	private VhmsgSender rapportEstSender;
	private final static int agentId = 0;
	public static ServerSocket listener;
	public static Socket socket;

	public final static int port = 2003;
	public static double global_rapport_score;

	/**
	 * Runs the client as an application. First it displays a dialog box asking
	 * for the IP address or hostname of a host running the date server, then
	 * connects to it and displays the date that it serves.
	 */
	public static void main(String[] args) throws IOException {

		//Initlization 
		client = new RapportClient();
		global_rapport_score = 2.0;
		potential_rapport_score=2.0;
		last_system_strategy = "NA";
		last_user_strategy = "NA";
		addition = 0.0;
		substract = 0.0;
		user_smile=false;
		
		client.initializeVHMsg();
	
		String serverAddress = JOptionPane
				.showInputDialog("Enter IP Address of a machine that is\n"
						+ "running the temporal association rules:","localhost");
		Socket s = new Socket(serverAddress, port);

		while (true) {
			BufferedReader input = new BufferedReader(new InputStreamReader(
					s.getInputStream()));
			String event = input.readLine();
			if (event.contains("rapport_score")) {

				String[] event_content = event.split("\t");
				double score_num = Double.parseDouble(event_content[2]);
				
				if(Turn_number<=10){
					client.rapportEstSender.sendMessage(agentId + " " + score_num);	
				}
				
				else{
					potential_rapport_score=global_rapport_score;
					potential_rapport_score+=score_num-last_temporal_rule_result;
					if(potential_rapport_score<=7.0){
						global_rapport_score=potential_rapport_score;
					}
					client.rapportEstSender.sendMessage(agentId + " " + global_rapport_score);
				}
				last_temporal_rule_result=score_num;

			}

			System.out.println(event);
		}

	}

	public void initializeVHMsg() throws IOException {
		String URL_file = "C:\\Users\\fpecune\\Desktop\\SARA\\inmind-development-master\\Server\\Multiuser\\SaraProject\\src\\main\\java\\edu\\cmu\\inmind\\multiuser\\rapportestimator\\IP.txt";
		BufferedReader IP_br;
		String line;
		IP_br = new BufferedReader(new FileReader(URL_file));
		String[] IP_address = null;
		if ((line = IP_br.readLine()) != null) {

			IP_address = line.split(" ");

		}

		Config.VHMSG_SERVER_URL = IP_address[0];
		System.out.println("IP Address: " + Config.VHMSG_SERVER_URL);
		// initialize sender
		rapportEstSender = new VhmsgSender("vrRapportEst");
		vhmsgSubscriber = new VHMsg();
		vhmsgSubscriber.openConnection();
		vhmsgSubscriber.enableImmediateMethod();
		vhmsgSubscriber.addMessageListener(this);
		vhmsgSubscriber.subscribeMessage("vrStrategyRecog");
		vhmsgSubscriber.subscribeMessage("vrMultisense");
		vhmsgSubscriber.subscribeMessage("vrSocialReasoner");

	}

	@Override
	public void messageAction(MessageEvent e) {
		// TODO Auto-generated method stub

		String message = e.toString();

		String[] tokens = message.split(" ");
		System.out.println(e);
		String content = "";

		// ###############Begin of User Input###################
		// Conversational Strategy Classifier output
		if (tokens[0].equals("vrStrategyRecog")) {

			// Count Turns of user speak
			Turn_number++;

			// SD =3, SE=6,PRAISE=9,VSN=12, ASN=15
			if (tokens[3].equals("True")) {
				last_user_strategy = "SD";
				// Rule 2:Encourage user did self-disclosure as early as
				// possible
				if (Turn_number <= 10) {
					addition = 1 / Turn_number;
				} else {
					addition = 1 / 10;
				}

				// Rule 1: encourage norm of reciprocity except PR
				if (last_system_strategy == "SD") {
					addition = 0.5;
				}

			}

			if (tokens[6].equals("True")) {
				last_user_strategy = "SE";

				// Rule 1: encourage norm of reciprocity except PR
				if (last_system_strategy == "SE") {
					addition = 0.5;
				}

			}

			if (tokens[9].equals("True")) {
				last_user_strategy = "PR";
				// Rule 1: encourage norm of reciprocity except PR
				if (last_system_strategy == "PR") {
					substract = 0.5;
				}
			}

			if (tokens[12].equals("TRUE")) {
				last_user_strategy = "VSN";
				
				// Rule 1: encourage norm of reciprocity except PR
				if (last_system_strategy == "VSN") {
					addition = 0.5;
				}
				
				//Rule 3: encourage user did VSN at the later stage with smile
				if(user_smile&&Turn_number>10){
					addition=0.5;
				}
				else{
					substract=0.5;
				}

			}

			if (tokens[15].equals("TRUE")) {
				last_user_strategy = "ASN";
				if(last_system_strategy=="SD"){
					substract=0.5;
				}
			}

			else {
				
				System.out.println("##################I am in else#############");
			}

		}

		// Multisense output
		if (tokens[0].equals("vrMultisense")) {
			if (tokens[2].equals("true")) {
				user_smile=true;
			}
			else{
				user_smile=false;
			}
		}
		// ###############End of User Input###################

		// ###############Begin of System Input###################
		// Social Reaonser output
		if (tokens[0].equals("vrSocialReasoner")) {
			if (tokens[2].equals("SD") || tokens[2].equals("QESD")) {
				last_system_strategy="SD";
			}

			if (tokens[2].equals("RSE")) {
				last_system_strategy="SE";
			}
			if (tokens[2].equals("PR")) {
				last_system_strategy="PR";
			}
			if (tokens[2].equals("VSN")) {
				last_system_strategy="VSN";
			}
			if (tokens[2].equals("ASN")) {
				last_system_strategy="ASN";
			}
			else{
				last_system_strategy="NA";
			}

		}
	
		potential_rapport_score=global_rapport_score;
		potential_rapport_score+=addition;
		potential_rapport_score-=substract;
		addition=0.0;
		substract=0.0;
		if(potential_rapport_score<=7.0){
			global_rapport_score=potential_rapport_score;
			//client.rapportEstSender.sendMessage(agentId + " " + global_rapport_score);

			System.out.println("Global_rapport_score: "+global_rapport_score);
			System.out.println("Turn information: "+Turn_number);
			
		}
	}

}