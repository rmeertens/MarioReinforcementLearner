package ch.idsia.agents.controllers;

import java.util.Random;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey.karakovskiy@gmail.com
 * Date: Apr 8, 2009
 * Time: 4:03:46 AM
 */

public class TwitchAgent extends BasicMarioAIAgent implements Agent
{
int trueJumpCounter = 0;
int trueSpeedCounter = 0;

public TwitchAgent()
{
    super("TwitchAgent");
    reset();
}

public void reset()
{
    action = new boolean[Environment.numberOfKeys];
    action[Mario.KEY_RIGHT] = true;
    action[Mario.KEY_SPEED] = true;
    trueJumpCounter = 0;
    trueSpeedCounter = 0;
}

private boolean DangerOfAny()
{

        if ((getReceptiveFieldCellValue(marioEgoRow + 2, marioEgoCol + 1) == 0 &&
            getReceptiveFieldCellValue(marioEgoRow + 1, marioEgoCol + 1) == 0) ||
            getReceptiveFieldCellValue(marioEgoRow, marioEgoCol + 1) != 0 ||
            getReceptiveFieldCellValue(marioEgoRow, marioEgoCol + 2) != 0 ||
            getEnemiesCellValue(marioEgoRow, marioEgoCol + 1) != 0 ||
            getEnemiesCellValue(marioEgoRow, marioEgoCol + 2) != 0)
            return true;
        else
            return false;
}

public boolean[] getAction()
{
    // this Agent requires observation integrated in advance.
	Random r = new Random();
	action[Mario.KEY_JUMP] = r.nextBoolean();
	action[Mario.KEY_RIGHT] = r.nextBoolean();
	/*
    if (DangerOfAny() && getReceptiveFieldCellValue(marioEgoRow, marioEgoCol + 1) != 1)  // a coin
    {
        if (isMarioAbleToJump || (!isMarioOnGround && action[Mario.KEY_JUMP]))
        {
            action[Mario.KEY_JUMP] = true;
        }
        ++trueJumpCounter;
    }
    else
    {
        action[Mario.KEY_JUMP] = false;
        trueJumpCounter = 0;
    }

    if (trueJumpCounter > 16)
    {
        trueJumpCounter = 0;
        action[Mario.KEY_JUMP] = false;
    }*/
    
    return action;
}
}