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

public class TwitchLogger extends Thread {
	// The server to connect to and our details.
	static String server = "irc.twitch.tv";
	static String nick = "twitchteachesmario";
	static String login = "oauth:gmkbitwej03ha2cmd4mxy1b5s65dwei";
	static String channel = "#twitchteachesmario"; // The channel which the bot will join.
	
	BufferedReader reader;

	public TwitchLogger() {
		System.err.println("Watch out: using class TwitchLogger");
		connectToTwitch();
	}

	private void connectToTwitch() {
		InetAddress addr;
		try {
			// Set the address
			addr = InetAddress.getByName(server);
			server = addr.toString().split("/")[1];
			System.out.println("TwitchLogger connecting to " + addr.toString());
			System.out.println("TwitchLogger connecting to "
					+ addr.getHostName());

			// Connect directly to the IRC server.
			Socket socket = new Socket(server, 6667);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			// Log on to the server.
			writer.write("PASS " + login + "\r\n");
			writer.write("NICK " + nick + "\r\n");
			writer.flush();

			// Read lines from the server until it tells us we have connected.
			String line = null;
			while ((line = reader.readLine()) != null) {
				System.out.println("Twitchlogger login says: " + line);
				if (line.contains("End of") | line.contains(">"))
					break;
			}
			System.out.println("Twitchlogger joining channel " + channel);
			
			// Join the channel.
			writer.write("JOIN " + channel + "\r\n");
			writer.flush();
			InformationSingleton.getInstance().setRunningTwitch(true);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			InformationSingleton.getInstance().setRunningTwitch(false);
		} catch (IOException e) {
			e.printStackTrace();
			InformationSingleton.getInstance().setRunningTwitch(false);
		}

	}

	public void run() {
		while (true) {
			// Keep reading lines from the server.
			if (InformationSingleton.getInstance().getRunningTwitch()) {
				readLines();
			} else {
				connectToTwitch();
			}

		}
	}

	private void readLines() {
		String line;
		try {
			// Read all the lines!
			while ((line = reader.readLine()) != null) {
				System.err.println("Twitchobject reading line: " + line);
			}
			System.err.println("TwitchObject: server stopped");
			InformationSingleton.getInstance().setRunningTwitch(false);
		} catch (IOException e) {
			e.printStackTrace();
			InformationSingleton.getInstance().setRunningTwitch(false);
		}
	}

}
