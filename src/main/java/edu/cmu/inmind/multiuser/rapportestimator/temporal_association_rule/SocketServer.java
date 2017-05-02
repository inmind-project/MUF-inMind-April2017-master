package edu.cmu.inmind.multiuser.rapportestimator.temporal_association_rule;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;


/**
 * Created by mertcan on 6.8.2015.
 */
public class SocketServer extends Thread {

    private Socket socket = null;
    
    private String output="Initial";
   

    public SocketServer(Socket socket) {

        super("MiniServer");
        this.socket = socket;
    }

    public void run() {
        try {
            final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            
            out.print(currentOutput());
//            while (Util.listening) {
//                out.println(currentOutput());
//                Thread.sleep(1000);
//            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String currentOutput() {
 
        return output;
    }
    
    public void updateOutput(String update_string) {
    	output= update_string;
    	
    }
}