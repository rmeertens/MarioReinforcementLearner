package edu.stanford.cs229.agents;

public class InformationSingleton {
   private static InformationSingleton instance = null;
   protected InformationSingleton() {
      // Exists only to defeat instantiation.
   }
   public static InformationSingleton getInstance() {
      if(instance == null) {
         instance = new InformationSingleton();
      }
      return instance;
   }
   
   private int LEVEL_REACHED = 0;
   private boolean EVALUATING = false;
   private int LATESTFITNESS = 0;
   private String latestName = "";
   private boolean runningTwitch = false;
   private boolean hasAction = false;
   
   public int getWorldReached()
   {
	   return this.LEVEL_REACHED/LearningParams.NEW_WORLD_EVERY_X_LEVELS;
   }
   public int getLevelReached()
   {
	   return this.LEVEL_REACHED;
   }
   public boolean evaluating()
   {
	   return this.EVALUATING;
   }
   public int getLatestFitness()
   {
	   return this.LATESTFITNESS;
   }
	public void setEvaluating(boolean b) {
		this.EVALUATING = b;
	}
	public void setRunningTwitch(boolean setRunning)
	{
		runningTwitch = setRunning;
	}
	public boolean getRunningTwitch()
	{
		return runningTwitch;
	}
	
	public void setLevel(int i) {
		this.LEVEL_REACHED = i;
		
	}
	public void setFitness(int finalScore) {
		this.LATESTFITNESS = finalScore;
		
	}
	public String getLatestName()
	{
		
		 return this.latestName ;
	}
	
	public void setLatestName(String newName)
	{
		 this.latestName = newName;
	}
	
	public void setGotAction(boolean newAction) {
		this.hasAction = newAction;
	}
	
	public boolean getHasAction()
	{
		return this.hasAction;
	}
	
	   
}