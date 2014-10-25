package radboud.meertens.tests;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import radboud.meertens.ActionModificatorInterface;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import edu.stanford.cs229.agents.MarioAction;


public class TestMarioServer implements ActionModificatorInterface{

	
	MarioAction action = null;
	private String latestName = "";
	protected JTextArea textArea;

    public TestMarioServer() throws Exception {

        JFrame frame = new JFrame("HelloWorldSwing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add the ubiquitous "Hello World" label.
        JLabel label = new JLabel("Hello World");
        frame.getContentPane().add(label);

        textArea = new JTextArea(15,20);
        JScrollPane scrollPane = new JScrollPane(textArea);
        DefaultCaret caret = (DefaultCaret)textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        
        frame.getContentPane().add(scrollPane);
        
        //Display the window.
        frame.pack();
        frame.setVisible(true);
        
        
        
        
    	System.out.println("Starting HTTP Mario server");
        HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);
        server.createContext("/marioserver", new MyHandler(this,textArea));
        server.setExecutor(null); // creates a default executor
        server.start();
        
        
        
    }

    static class MarioCommandWithTime
    {
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
    	TestMarioServer parent;
    	JTextArea textArea;
    	
    	HashMap<String,MarioCommandWithTime> actions = new HashMap<String,MarioCommandWithTime>();
    	
    	public MyHandler(TestMarioServer parent, JTextArea are)
    	{
    		this.parent = parent;
    		this.textArea = are;
    	}
    	
    	public HashMap<String,MarioCommandWithTime> getActions()
    	{
    		return actions;
    		
    	}
    	
    	
        public void handle(HttpExchange t) throws IOException {
        	System.err.println("handling stuff");
            Map <String,String>parms = TestMarioServer.queryToMap(t.getRequestURI().getQuery());
            
            String response = "";

            if(parms.containsKey("option"))
            {
            	String option = parms.get("option");
            	System.out.println("option = " + option);

                if("pressButtons".equals(option))
                {
                	String newName = parms.get("name");
                	String newCommand = parms.get("command");
                	MarioCommandWithTime toAdd = new MarioCommandWithTime(newName,newCommand,new Date());
                	actions.put(newName, toAdd);
                	this.textArea.append(newName + " pressed: " + newCommand + "\n");
                	
                }
                else if("releaseButtons".equals(option))
                {
                	String newName = parms.get("name");
                	actions.remove(newName);
                	this.textArea.append(newName + " released buttons\n");
                }
                else if("refreshTime".equals(option))
                {
                	String newName = parms.get("name");
                	MarioCommandWithTime toAdd = actions.get(newName);
                	toAdd.dateSent = new Date();
                	actions.put(newName, toAdd);
                	this.textArea.append(newName + " updated\n");
                }
                
                
                
            }
            else
            {
            	response = "Use /marioserver?option=yourKey&foo=unused to see how to handle url parameters";
            	System.out.println("Something else");
            }
            
            
            
            if(parms.containsKey("name"))
            {
            	String newName = parms.get("name");
            	String newCommand = parms.get("command");
            	//System.out.println("Found the name of the person: " + newName + " and his command is: " + newCommand);
            	//this.textArea.append(newName + " says something name\n");
                
            	String p = newCommand;
            	String line=newName;
            	if(p.trim().equalsIgnoreCase("nothing") || p.trim().equalsIgnoreCase("n"))
				{
            		parent.setAction(line,MarioAction.DO_NOTHING);
				}
				else if(p.trim().equalsIgnoreCase("left") || p.trim().equalsIgnoreCase("l"))
				{
					parent.setAction(line,MarioAction.LEFT);
				}
				else if(p.trim().equalsIgnoreCase("right") || p.trim().equalsIgnoreCase("r"))
				{
					parent.setAction(line,MarioAction.RIGHT);
				}
				else if(p.trim().equalsIgnoreCase("jump") || p.trim().equalsIgnoreCase("j"))
				{
					parent.setAction(line,MarioAction.JUMP);
				}
				else if(p.trim().equalsIgnoreCase("fire") || p.trim().equalsIgnoreCase("f"))
				{
					parent.setAction(line,MarioAction.FIRE);
				}
				else if(p.trim().equalsIgnoreCase("leftjump") || p.trim().equalsIgnoreCase("lj"))
				{
					parent.setAction(line,MarioAction.LEFT_JUMP);
				}
				else if(p.trim().equalsIgnoreCase("leftfire") || p.trim().equalsIgnoreCase("lf"))
				{
					parent.setAction(line,MarioAction.LEFT_FIRE);
				}
				else if(p.trim().equalsIgnoreCase("rightjump") || p.trim().equalsIgnoreCase("lj"))
				{
					parent.setAction(line,MarioAction.RIGHT_JUMP);
				}
				else if(p.trim().equalsIgnoreCase("rightfire") || p.trim().equalsIgnoreCase("rf"))
				{
					parent.setAction(line,MarioAction.RIGHT_FIRE);
				}
				else if(p.trim().equalsIgnoreCase("jumpfire") || p.trim().equalsIgnoreCase("jf"))
				{
					parent.setAction(line,MarioAction.JUMP_FIRE);
				}
				else if(p.trim().equalsIgnoreCase("leftjumpfire") || p.trim().equalsIgnoreCase("ljf"))
				{
					parent.setAction(line,MarioAction.LEFT_JUMP_FIRE);
				}
				else if(p.trim().equalsIgnoreCase("rightjumpfire") || p.trim().equalsIgnoreCase("rjf"))
				{
					parent.setAction(line,MarioAction.RIGHT_JUMP_FIRE);
				}
            }
            
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

		private void saveKeyToFile(String newKey) {
			try{
					PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("keysDatabase.txt", true))); 
			    	out.println(newKey);
			    	out.close();
			}catch (IOException e) {
			    // TODO what happens if writing fails?
				e.printStackTrace();
			}
			
			
		}
    }

    /**
     * returns the url parameters in a map
     * @param query
     * @return map
     */
    public static Map<String, String> queryToMap(String query){
      Map<String, String> result = new HashMap<String, String>();
      for (String param : query.split("&")) {
          String pair[] = param.split("=");
          if (pair.length>1) {
              result.put(pair[0], pair[1]);
          }else{
              result.put(pair[0], "");
          }
      }
      return result;
    }

    /**
     * Sets the action that mario should perform including the name of the person who set it
     * @param nameOfSetter
     * @param newAction
     */
    private void setAction(String nameOfSetter,MarioAction newAction)
	 {
		 latestName = nameOfSetter.substring(1, Math.min(20,nameOfSetter.length())).replaceAll("[^a-zA-Z0-9]","");
		 
		 //InformationSingleton.getInstance().setLatestName(latestName);
		 //InformationSingleton.getInstance().setGotAction(true);
		 action = newAction;
	 }
    
	@Override
	public int modifyAction(int actionNumber) {
		if(action!=null)
		{
			int number = action.getActionNumber(); 
			action = null;
			return number;
		}
		return actionNumber;
	}

	@Override
	public void startListening() {
		
		
	}

}
