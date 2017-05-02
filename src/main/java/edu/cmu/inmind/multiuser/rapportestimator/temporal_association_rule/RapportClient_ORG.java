package edu.cmu.inmind.multiuser.rapportestimator.temporal_association_rule;

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
public class RapportClient_ORG implements MessageListener {

	
	// for subscription
	public static VHMsg vhmsgSubscriber;
		
		
	// for sending
	private VhmsgSender rapportEstSender;
	private final static int agentId = 0;
	public static ServerSocket listener;
	public static Socket socket;

	public final static int port = 2003;

	/**
	 * Runs the client as an application. First it displays a dialog box asking
	 * for the IP address or hostname of a host running the date server, then
	 * connects to it and displays the date that it serves.
	 */
	public static void main(String[] args) throws IOException {

		RapportClient_ORG client = new RapportClient_ORG();
		client.initializeVHMsg();
		String serverAddress = JOptionPane
				.showInputDialog("Enter IP Address of a machine that is\n"
						+ "running the temporal association rules:");
		Socket s = new Socket(serverAddress, port);
		double[] rapport_vec = new double[7];
		// Linear Regression Model weight
		double[] LRweight = { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 };

		while (true) {
			double rapport_score = 0.0;
			BufferedReader input = new BufferedReader(new InputStreamReader(
					s.getInputStream()));
			String event = input.readLine();
			if (event.contains("rapport_score")) {

				String[] event_content = event.split("\t");
				double score_num = Double.parseDouble(event_content[2]);
				client.rapportEstSender.sendMessage(agentId + " " + score_num);

			}

			System.out.println(event);
		}

	}

	public void initializeVHMsg() throws IOException {
		String URL_file = "IP.txt";
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
		vhmsgSubscriber.subscribeMessage("vrBEAT");
		

	}

	@Override
	public void messageAction(MessageEvent e) {
		// TODO Auto-generated method stub
		
	}
}