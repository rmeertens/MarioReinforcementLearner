package radboud.meertens.tests;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import radboud.meertens.HTTPMarioServer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import edu.stanford.cs229.agents.InformationSingleton;
import edu.stanford.cs229.agents.MarioAction;

public class StefTestIncMain {

	private String latestName = "";
	protected JTextArea textArea;
	MyHandler theHandler;
	boolean hasAction = false;

	public static void main(String[] args) throws Exception {
		StefTestIncMain t = new StefTestIncMain();
	}

	public StefTestIncMain() throws Exception {

		JFrame frame = new JFrame("HelloWorldSwing");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Add the ubiquitous "Hello World" label.
		JLabel label = new JLabel("Hello World");
		frame.getContentPane().add(label);

		textArea = new JTextArea(15, 20);
		JScrollPane scrollPane = new JScrollPane(textArea);
		DefaultCaret caret = (DefaultCaret) textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		frame.getContentPane().add(scrollPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);

		System.out.println("Starting HTTP Mario server");
		HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);
		this.theHandler = new MyHandler(textArea);
		server.createContext("/marioserver", theHandler);
		server.setExecutor(null); // creates a default executor
		server.start();
		InformationSingleton.getInstance().setRunningTwitch(true);

	}

	static class MarioCommandWithTime {
		public MarioCommandWithTime(String newName, String newCommand, Date date) {
			this.nameSender = newName;
			this.nameCommand = newCommand;
			this.dateSent = date;
		}

		String nameSender;
		String nameCommand;
		Date dateSent;
	}

	static class MyHandler implements HttpHandler {
		HTTPMarioServer parent;
		JTextArea textArea;

		HashMap<String, MarioCommandWithTime> actions = new HashMap<String, MarioCommandWithTime>();

		public MyHandler(JTextArea are) {
			this.parent = parent;
			this.textArea = are;
		}

		public HashMap<String, MarioCommandWithTime> getActions() {
			return actions;

		}

		public void handle(HttpExchange t) throws IOException {
			System.err.println("handling stuff");
			Map<String, String> parms = HTTPMarioServer.queryToMap(t
					.getRequestURI().getQuery());

			String response = "something wrong";

			if (parms.containsKey("option")) {
				response = "congratulations, you did it";
				String option = parms.get("option");
				System.out.println("option = " + option);

				if ("pressButtons".equals(option)) {

					String newName = parms.get("name");
					String newCommand = parms.get("command");
					MarioCommandWithTime toAdd = new MarioCommandWithTime(
							newName, newCommand, new Date());
					actions.put(newName, toAdd);
					this.textArea.append(newName + " pressed: " + newCommand
							+ "\n");

				} else if ("releaseButtons".equals(option)) {

					String newName = parms.get("name");
					actions.remove(newName);
					InformationSingleton.getInstance().setGotAction(false);
					this.textArea.append(newName + " released buttons\n");
				} else if ("refreshTime".equals(option)) {
					String newName = parms.get("name");
					MarioCommandWithTime toAdd = actions.get(newName);
					toAdd.dateSent = new Date();
					actions.put(newName, toAdd);
					this.textArea.append(newName + " updated\n");
				}

			} else {
				response = "Use /marioserver?option=yourKey&foo=unused to see how to handle url parameters";
				System.out.println("Something else");
				this.textArea.append("Received something strange\n");

			}

			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}

	}

	/**
	 * returns the url parameters in a map
	 * 
	 * @param query
	 * @return map
	 */
	public static Map<String, String> queryToMap(String query) {
		Map<String, String> result = new HashMap<String, String>();
		for (String param : query.split("&")) {
			String pair[] = param.split("=");
			if (pair.length > 1) {
				result.put(pair[0], pair[1]);
			} else {
				result.put(pair[0], "");
			}
		}
		return result;
	}

	

	private MarioAction getAction(String nameAction) {

		if (nameAction.trim().equalsIgnoreCase("nothing")
				|| nameAction.trim().equalsIgnoreCase("n")) {
			return MarioAction.DO_NOTHING;
		} else if (nameAction.trim().equalsIgnoreCase("left")
				|| nameAction.trim().equalsIgnoreCase("l")) {
			return MarioAction.LEFT;
		} else if (nameAction.trim().equalsIgnoreCase("right")
				|| nameAction.trim().equalsIgnoreCase("r")) {
			return MarioAction.RIGHT;
		} else if (nameAction.trim().equalsIgnoreCase("jump")
				|| nameAction.trim().equalsIgnoreCase("j")) {
			return MarioAction.JUMP;
		} else if (nameAction.trim().equalsIgnoreCase("fire")
				|| nameAction.trim().equalsIgnoreCase("f")) {
			return MarioAction.FIRE;
		} else if (nameAction.trim().equalsIgnoreCase("leftjump")
				|| nameAction.trim().equalsIgnoreCase("lj")) {
			return MarioAction.LEFT_JUMP;
		} else if (nameAction.trim().equalsIgnoreCase("leftfire")
				|| nameAction.trim().equalsIgnoreCase("lf")) {
			return MarioAction.LEFT_FIRE;
		} else if (nameAction.trim().equalsIgnoreCase("rightjump")
				|| nameAction.trim().equalsIgnoreCase("lj")) {
			return MarioAction.RIGHT_JUMP;
		} else if (nameAction.trim().equalsIgnoreCase("rightfire")
				|| nameAction.trim().equalsIgnoreCase("rf")) {
			return MarioAction.RIGHT_FIRE;
		} else if (nameAction.trim().equalsIgnoreCase("jumpfire")
				|| nameAction.trim().equalsIgnoreCase("jf")) {
			return MarioAction.JUMP_FIRE;
		} else if (nameAction.trim().equalsIgnoreCase("leftjumpfire")
				|| nameAction.trim().equalsIgnoreCase("ljf")) {
			return MarioAction.LEFT_JUMP_FIRE;
		} else if (nameAction.trim().equalsIgnoreCase("rightjumpfire")
				|| nameAction.trim().equalsIgnoreCase("rjf")) {
			return MarioAction.RIGHT_JUMP_FIRE;
		}
		return MarioAction.DO_NOTHING;
	}
}
