/*
 * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Mario AI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package ch.idsia.agents.controllers;

import java.util.Hashtable;
import java.util.Random;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.engine.sprites.Sprite;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey_at_idsia_dot_ch
 * Date: May 9, 2009
 * Time: 1:42:03 PM
 * Package: ch.idsia.agents.controllers
 */

public class ScaredShooty extends BasicMarioAIAgent implements Agent
{
	private static final double LEARNING_RATE = 0.6;
	private static final int POSSIBLE_ACTIONS = 3;
public ScaredShooty(Hashtable<Integer,double[]> statesAndActions, int decay)
{
    super("ScaredShooty");
    this.statesAndActions = statesAndActions;
    this.runNumber = decay;
    
}
boolean initialised = false;
Hashtable<Integer,double[]> statesAndActions;
int trueJumpCounter = 0;
int trueSpeedCounter = 0;
int lastReward = 0;
double runNumber = 0.0;
int previousState = -1;
int previousAction = 0;
public Hashtable<Integer,double[]> getHashtable()
{
	return statesAndActions;
}
private boolean isCreature(int c)
{
    switch (c)
    {
        case Sprite.KIND_GOOMBA:
        case Sprite.KIND_RED_KOOPA:
        case Sprite.KIND_RED_KOOPA_WINGED:
        case Sprite.KIND_GREEN_KOOPA_WINGED:
        case Sprite.KIND_GREEN_KOOPA:
            return true;
    }
    return false;
}

private boolean Wall(int xpos,int ypos)
{
	//levelScene[xpos][ypos];
	if (levelScene[xpos][ypos]!=0 && levelScene[xpos][ypos] != 2)//Check level scene for walls
	{
		return true;
	}
	return false;
}
private String booleanToInteger(boolean argument)
{
	if(argument)
	{
		return "1";
	}
	else
	{
		return "0";
	}
}

public boolean[] getAction()
{
	
        
    int state = getState();
    
    int maxIndex = 0;
    
    if(statesAndActions.containsKey(state))
    {
    	double[] actionsResults = statesAndActions.get(state);
    	//System.out.println("Found this state before!" + actionsResults);
    	
    	double max = Double.MIN_VALUE;
    	for (int i = 0; i < actionsResults.length; i++) {
    	    if (actionsResults[i] > max) {
    	        max = actionsResults[i];
    	        maxIndex = i;
    	    }
    	}
    	
    }
    else
    {
    	double[] actionsResults = new double[POSSIBLE_ACTIONS];
    	for(int x = 0; x < POSSIBLE_ACTIONS; x++)
    	{
    		actionsResults[x] = -1.0;
    	}
    	statesAndActions.put(state,actionsResults);
    	Random r = new Random();
    	maxIndex = Math.abs(r.nextInt()%POSSIBLE_ACTIONS);
    	//System.out.println("Action random: " + maxIndex);
    	
    }
    
    //System.out.println(statesAndActions.size());
   // System.out.println("Really wall in front: " + wallInFront);
    Random r = new Random();
    
    if(r.nextDouble()%1 > 0.4){
    	//System.out.println("Choosing random");
    	maxIndex = Math.abs(r.nextInt()%POSSIBLE_ACTIONS);
    	//System.out.println("Randomly: " + maxIndex);
    }
    
    if(maxIndex == 0)
    {
        action[Mario.KEY_RIGHT] = true ;
        action[Mario.KEY_JUMP] = false ;
    }
    if(maxIndex == 1)
    {
        action[Mario.KEY_RIGHT] = false ;
        action[Mario.KEY_JUMP] = true ;
    }
    if(maxIndex == 2)
    {
        action[Mario.KEY_RIGHT] = true ;
        action[Mario.KEY_JUMP] = true ;
    }
    
    previousAction = maxIndex;
    previousState = state;
    if(action[Mario.KEY_JUMP])
    {
    	trueJumpCounter++;
    }
    else
    {
    	trueJumpCounter = 0;
    }
   // System.out.println("Now deciding in state " + state);
    return action;
}
private int getState() {
	//int x = marioEgoRow;
    //int y = marioEgoCol;

	String wallString = "";
	wallString += booleanToInteger(trueJumpCounter>14);
    wallString += booleanToInteger(this.isMarioAbleToJump);
    wallString += booleanToInteger(this.isMarioOnGround);
    wallString += booleanToInteger(this.isMarioCarrying);
    wallString += booleanToInteger(this.isMarioAbleToShoot);
    System.out.println("======");
    for(int s = marioEgoRow; s < marioEgoRow+5; s++ )
    {
    	for(int t = marioEgoCol+3; t < marioEgoCol+4; t++ )
    	{
    		if(s >= 0 && s < levelScene.length && t >= 0 && t < levelScene[0].length)
    		{
    			if(Wall(s,t))
	    		{
	    			wallString += "1";
	    		}
	    		else
	    		{
	    			wallString += "0";
	    		}
    			
    			
    			
        		if(Wall(s,t))
    			{
        			System.out.print("1");
    			}
        		else
        		{
        			System.out.print("0");
        		}
        		System.out.print(" = " + levelScene[s][t]+"");
        		if(marioEgoCol+2==s||t==marioEgoRow+2)
        		{
        			System.out.print("<-m");
        		}
    		}
    		else
    		{
    			wallString += "1";
    		}
    		
    		
    	}
    	System.out.println();
    }
    System.out.println("======");
    int state = Integer.parseInt(wallString,2);
	return state;
}

public void giveIntermediateReward(int intermediateReward)
{
	int reward = intermediateReward - lastReward-1;
	lastReward = intermediateReward;
	
	//System.out.println("Intermediate reward: " + reward + " intermediate: " + intermediateReward + " previous action:  " + previousAction);
	if(statesAndActions.containsKey(previousState)){
		double[] actionsResults = statesAndActions.get(previousState);
		int newState = getState();
		if(newState == previousState)
		{
			//System.out.println("Same state " + newState);
		}
		else if (statesAndActions.containsKey(newState))
		{
			System.out.println("New state can be found");
		}
		else
		{
			System.out.println("New state can not be found");
		}
		
		actionsResults[previousAction] = (1-LEARNING_RATE)*actionsResults[previousAction] + LEARNING_RATE*(reward);
		statesAndActions.put(previousState, actionsResults);
	}
}
public void reset()
{
    action[Mario.KEY_RIGHT] = true;
//    action[Mario.KEY_SPEED] = true;
}

}

