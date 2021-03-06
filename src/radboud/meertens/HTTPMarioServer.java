package radboud.meertens;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import edu.stanford.cs229.agents.InformationSingleton;
import edu.stanford.cs229.agents.MarioAction;

public class HTTPMarioServer implements ActionModificatorInterface {

	MarioAction action = null;
	protected JTextArea textArea;
	MyHandler theHandler;
	

	public HTTPMarioServer() throws Exception {

		JFrame frame = new JFrame("HelloWorldSwing");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
		this.theHandler = new MyHandler(this, textArea);
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

		ConcurrentHashMap<String, MarioCommandWithTime> actions = new ConcurrentHashMap<String, MarioCommandWithTime>();
		ConcurrentHashMap<String, MarioCommandWithTime> actionsThatExpired = new ConcurrentHashMap<String, MarioCommandWithTime>();
		
		public MyHandler(HTTPMarioServer parent, JTextArea are) {
			this.parent = parent;
			this.textArea = are;
		}

		public ConcurrentHashMap<String, MarioCommandWithTime> getActions() {
			
			Date currentDate = new Date();
			System.out.println("amount of actions: " + actions.size());
			for(Iterator<Entry<String, MarioCommandWithTime>> it = actions.entrySet().iterator(); it.hasNext(); ) {
			      Entry<String, MarioCommandWithTime> entry = it.next();
			      if(currentDate.getTime() - entry.getValue().dateSent.getTime() > 1500) {
			    	 actionsThatExpired.put(entry.getKey(), entry.getValue());
			        it.remove();
			        
			        System.err.println("Removed the entry by " + entry.getKey() + " due to inactivity");
			      }
			    }
			InformationSingleton.getInstance().setGotAction(actions.size() > 0);
			
			System.out.println("amount of actions: " + actions.size() + " amount of removed actions" + actionsThatExpired.size());
			return actions;

		}

		public void handle(HttpExchange t) throws IOException {
			//System.err.println("handling stuff");
			Map<String, String> parms = HTTPMarioServer.queryToMap(t
					.getRequestURI().getQuery());

			String response = "";

			if (parms.containsKey("option")) {
				response = "congratulations, you did it";
				String option = parms.get("option");
				//System.out.println("option = " + option);

				if ("pressButtons".equals(option)) {
										
					String newName = parms.get("name");
					String newCommand = parms.get("command");
					MarioCommandWithTime toAdd = new MarioCommandWithTime(
							newName, newCommand, new Date());
					actions.put(newName, toAdd);
					System.out.println("Because " + newName + " pressed a button we now have " + actions.size() + " actions");
					InformationSingleton.getInstance().setGotAction(true);
					this.textArea.append(newName + " pressed: " + newCommand
							+ "\n");

				} else if ("releaseButtons".equals(option)) {
					String newName = parms.get("name");
					if (actions.containsKey(newName))
					{
						actions.remove(newName);
					}
					boolean gotAction = actions.size() > 0;
					System.out.println("Because " + newName + " stopped pressing we now have " + actions.size() + " actions " + gotAction);
					InformationSingleton.getInstance().setGotAction(gotAction);
					this.textArea.append(newName + " released buttons\n");
				} else if ("refreshTime".equals(option)) {
					String newName = parms.get("name");
					if (actions.containsKey(newName))
					{
						MarioCommandWithTime toAdd = actions.get(newName);
						toAdd.dateSent = new Date();
						actions.put(newName, toAdd);
					}
					else if (actionsThatExpired.containsKey(newName))
					{
						MarioCommandWithTime toAdd = actions.get(newName);
						toAdd.dateSent = new Date();
						actions.put(newName, toAdd);
					}
					else
					{
						System.err.println("Trying to refresh a time that does not exist");
					}
					this.textArea.append(newName + " updated\n");
				}

			} else {
				response = "Use /marioserver?option=yourKey&foo=unused to see how to handle url parameters";
				//192.168.2.17:8001/marioserver?option=pressButtons&name=Roland&command=left
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

	
	@Override
	public int modifyAction(int actionNumber) {

		if (InformationSingleton.getInstance().getHasAction()) {
			ConcurrentHashMap<String, MarioCommandWithTime> actions = theHandler.getActions();
			Object[] values = actions.values().toArray();
			
			try {
				if (values.length==0)
				{
					return actionNumber;
				}
				MarioCommandWithTime randomValue = getPopularElement(values);
				//Random generator = new Random();
				//int index = generator.nextInt(values.length);
				//MarioCommandWithTime randomValue = (MarioCommandWithTime) values[index];

				InformationSingleton.getInstance().setLatestName(
						randomValue.nameSender);
				// return MarioAction.DO_NOTHING.getActionNumber();
				return getAction(randomValue.nameCommand).getActionNumber();
			} catch (Exception e) {
				e.printStackTrace();
				InformationSingleton.getInstance().setLatestName("");
				return actionNumber;
			}

		}
		InformationSingleton.getInstance().setLatestName("");
		return actionNumber;
	}

	public MarioCommandWithTime getPopularElement(Object[] a)
	{
	  int count = 1, tempCount;
	  MarioCommandWithTime popular = (MarioCommandWithTime)a[0];
	  MarioCommandWithTime temp = (MarioCommandWithTime)a[0];
	  
	  // Take each element except for the last one
	  for (int i = 0; i < (a.length - 1); i++)
	  {
	    temp = ((MarioCommandWithTime) a[i]);
	    tempCount = 0;
	    // Takes each element except for the first one
	    for (int j = 1; j < a.length; j++)
	    {
	      if (temp.nameCommand.equals(((MarioCommandWithTime)a[j]).nameCommand))
	        tempCount++;
	    }
	    if (tempCount > count)
	    {
	      popular = temp;
	      count = tempCount;
	    }
	  }
	  
	  return popular;
	}
	@Override
	public void startListening() {

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
