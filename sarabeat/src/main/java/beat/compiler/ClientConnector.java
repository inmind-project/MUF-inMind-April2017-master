package beat.compiler;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientConnector extends Thread{
	Socket socket;
	private String host = null;
	private int port = 0;
	private BufferedReader input;
	private PrintWriter out;
	private BSONCompiler bsonCompiler;
	private boolean isConnected;

	public ClientConnector(BSONCompiler bsonCompiler, String host, int port){
		this.bsonCompiler = bsonCompiler;
		this.host = host;
		this.port = port;
		this.isConnected = false;
	}

	public ClientConnector(String host, int port){
		this.host = host;
		this.port = port;
		this.isConnected = false;
	}
	
	public void connect(){
        try {
			socket = new Socket(host, port);
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
			isConnected = true;
			System.out.println("Connected!!!");
			this.isConnected = true;
		} catch (UnknownHostException e) {
			//e.printStackTrace();
			reConnect();
		} catch (IOException e) {
			//e.printStackTrace();
			reConnect();
		}
	}

	public void reConnect(){
		try {
			System.out.println("try to re-connect.");
			Thread.sleep(1000);
			this.connect();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendMessage(String msg){
		if(isConnected){
			out.write(msg);
			out.flush();
		}
	}
	
	
    public static void main(String[] args){
//    	ClientConnector client = new ClientConnector(Config.UNITY_SERVER_URL, 4444);
//		Scanner scanner = new Scanner(System.in);
//		while(true){
//			String s = scanner.nextLine();
//			client.sendMessage(s);
//		}
    }
}