package edu.cmu.inmind.multiuser.rapportestimator.temporal_association_rule;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A TCP server that runs on port 9090.  When a client connects, it
 * sends the client the current date and time, then closes the
 * connection with that client.  Arguably just about the simplest
 * server you can write.
 */
public class DataServer extends Thread {

    /**
     * Runs the server.
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        ServerSocket listener = new ServerSocket(9090);
        Socket socket = listener.accept();
            try{
            	while (true) {
                
                  


                    Thread.sleep(1000);

                        PrintWriter out =
                            new PrintWriter(socket.getOutputStream(), true);
                        out.println("1\t"+"event.SD"+"\t1");
                        out.println("1\t"+"event.SE"+"\t1");
                        out.println("1\t"+"event.PR"+"\t1");
                     
                }
            }
            	
            finally{
            	socket.getOutputStream().flush();
            	socket.close();
            }
        
       
    }
}