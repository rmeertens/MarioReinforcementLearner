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

public class NormalServerObject extends Thread{
	// The server to connect to and our details.
	static String  server = "irc.twitch.tv";
	//static String nick = "cognacplaysmario";
	static String nick = "twitchteachesmario";
	//static String login = "oauth:d3lpq70bejo9cb269w6qavwpqx1m5ax";
	static String login = "oauth:gmkbitwej03ha2cmd4mxy1b5s65dwei";
    // The channel which the bot will join.
	//static String channel = "#cognacplaysmario";
	static String channel = "#twitchteachesmario";
	BufferedReader reader ;
	private String latestName = "";
	MarioAction action = null;
	public NormalServerObject() 
	{
		System.err.println("Great: using class TwitchObject");
		connectToTwitch();
	}
	private void connectToTwitch()
	{
		InetAddress addr;
		try {
			addr = InetAddress.getByName(server);
			 server = addr.toString().split("/")[1];
	        System.out.println(addr.toString());
	        System.out.println(addr.getHostName());
	        // Connect directly to the IRC server.
	        Socket socket = new Socket(server, 6667);
	        BufferedWriter writer = new BufferedWriter(
	                new OutputStreamWriter(socket.getOutputStream( )));
	        reader = new BufferedReader(
	                new InputStreamReader(socket.getInputStream( )));
	        
	        // Log on to the server.
	        writer.write("PASS " + login + "\r\n");
	        writer.write("NICK " + nick + "\r\n");
	        writer.flush( );
	        
	        // Read lines from the server until it tells us we have connected.
	        String line = null;
	        while ((line = reader.readLine( )) != null) {
	            System.out.println(line);
	            if(line.contains("End of") | line.contains(">"))
	            	break;
	        }
	        System.out.println("Joining!!!!");
	        // Join the channel.
	        writer.write("JOIN " + channel + "\r\n");
	        writer.write("NOTICE Hello world\r\n");
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
				   String[] parts = line.split(":");
				   for(String p :parts)
				   {
				   
					if(p.trim().equalsIgnoreCase("nothing") || p.trim().equalsIgnoreCase("n"))
					{
						setAction(line,MarioAction.DO_NOTHING);
					}
					else if(p.trim().equalsIgnoreCase("left") || p.trim().equalsIgnoreCase("l"))
					{
						setAction(line,MarioAction.LEFT);
					}
					else if(p.trim().equalsIgnoreCase("right") || p.trim().equalsIgnoreCase("r"))
					{
						setAction(line,MarioAction.RIGHT);
					}
					else if(p.trim().equalsIgnoreCase("jump") || p.trim().equalsIgnoreCase("j"))
					{
						setAction(line,MarioAction.JUMP);
					}
					else if(p.trim().equalsIgnoreCase("fire") || p.trim().equalsIgnoreCase("f"))
					{
						setAction(line,MarioAction.FIRE);
					}
					else if(p.trim().equalsIgnoreCase("leftjump") || p.trim().equalsIgnoreCase("lj"))
					{
						setAction(line,MarioAction.LEFT_JUMP);
					}
					else if(p.trim().equalsIgnoreCase("leftfire") || p.trim().equalsIgnoreCase("lf"))
					{
						setAction(line,MarioAction.LEFT_FIRE);
					}
					else if(p.trim().equalsIgnoreCase("rigthjump") || p.trim().equalsIgnoreCase("lj"))
					{
						setAction(line,MarioAction.RIGHT_JUMP);
					}
					else if(p.trim().equalsIgnoreCase("rightfire") || p.trim().equalsIgnoreCase("rf"))
					{
						setAction(line,MarioAction.RIGHT_FIRE);
					}
					else if(p.trim().equalsIgnoreCase("jumpfire") || p.trim().equalsIgnoreCase("jf"))
					{
						setAction(line,MarioAction.JUMP_FIRE);
					}
					else if(p.trim().equalsIgnoreCase("leftjumpfire") || p.trim().equalsIgnoreCase("ljf"))
					{
						setAction(line,MarioAction.LEFT_JUMP_FIRE);
					}
					else if(p.trim().equalsIgnoreCase("rightjumpfire") || p.trim().equalsIgnoreCase("rjf"))
					{
						setAction(line,MarioAction.RIGHT_JUMP_FIRE);
					}
				   }
				}
				System.err.println("TwitchObject: server stopped");
				InformationSingleton.getInstance().setRunningTwitch(false);
			} catch (IOException e) {
				e.printStackTrace();
				InformationSingleton.getInstance().setRunningTwitch(false);
			}
	 }
	 
	 private void setAction(String line,MarioAction newAction)
	 {
		 String[] parts = line.split("!");
		 latestName = parts[0].substring(1, Math.min(20,parts[0].length())).replaceAll("[^a-zA-Z0-9]","");
		 
		 InformationSingleton.getInstance().setLatestName(latestName);
		 InformationSingleton.getInstance().setGotAction(true);
		 action = newAction;
	 }
	 
	public int modifyAction(int actionNumber)
	{
		
		if(action!=null)
		{
			int number = action.getActionNumber(); 
			action = null;
			return number;
		}
		return actionNumber;
	}
}
