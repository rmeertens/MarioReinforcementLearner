package radboud.meertens;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import edu.stanford.cs229.agents.InformationSingleton;
import edu.stanford.cs229.agents.MarioAction;

public class HitboxLogger extends Thread{
	// The server to connect to and our details.
	static String  server = "irc.glados.tv";
	//static String nick = "hitboxteachesmario";
	static String nick = "cogbot1";
	//static String login = "oauth:d3lpq70bejo9cb269w6qavwpqx1m5ax";
	//static String login = "ComingToBNAIC2014";
	static String login = "cogbot";
    // The channel which the bot will join.
	//static String channel = "#cognacplaysmario";
	static String channel = "#cogbot1";
	BufferedReader reader ;
	private String latestName = "";
	MarioAction action = null;
	public HitboxLogger() 
	{
		System.err.println("Error: using class HitboxLogger");
		connectToTwitch();
	}
	private void connectToTwitch() 
	{
		try {
	        System.out.println(server);
	        // Connect directly to the IRC server.
	        Socket socket = new Socket(server, 6667);
	        BufferedWriter writer = new BufferedWriter(
	                new OutputStreamWriter(socket.getOutputStream( )));
	        reader = new BufferedReader(
	                new InputStreamReader(socket.getInputStream( )));
	        
	        // Log on to the server.
	        writer.write("PASS " + login + "\r\n");
	        writer.flush( );
	        writer.write("NICK " + nick + "\r\n");
	        writer.write("USER " + nick + " 0 * :Ronnie Reagan\r\n");
	        writer.flush();

//	        writer.write("/msg nickserv register " + login + " rolandmeertens@hotmail.com");
//	        writer.write("asfjsfjse");
	          
	        
	        
	        System.out.println("Joining!!!!");
	        // Read lines from the server until it tells us we have connected.
	        
	        String line;
	        while ((line = reader.readLine( )) != null) {
	            System.out.println("READ: " + line);
	            if(line.contains("End of MOTD command"))
	            	break;
	         
	        }
	        System.out.println("Joining 2!!!!");
	        writer.write("JOIN " + channel + "\r\n");
	        System.out.println(line);
	        System.out.println("Joining!!!!");
	        // Join the channel.
	        
	        writer.flush( );
	        InformationSingleton.getInstance().setRunningTwitch(true);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			InformationSingleton.getInstance().setRunningTwitch(false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			InformationSingleton.getInstance().setRunningTwitch(false);
		}
       
	}
	 public void run() {
		 while(true){
			 if(InformationSingleton.getInstance().getRunningTwitch())
			 {
				 readLines();
			 }
			 else
			 {
				 connectToTwitch();
			 }
		
		 }
     }
	 private void readLines()
	 {
		 String line;
	        // Keep reading lines from the server.
	        try {
				while ((line = reader.readLine( )) != null) {
				   System.err.println(line);
				   
				}
				System.err.println("TwitchObject: server stopped");
				InformationSingleton.getInstance().setRunningTwitch(false);
			} catch (IOException e) {
				e.printStackTrace();
				InformationSingleton.getInstance().setRunningTwitch(false);
			}
	 }
	 
	
}
