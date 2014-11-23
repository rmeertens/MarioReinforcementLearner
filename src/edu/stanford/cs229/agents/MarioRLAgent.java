/* Some thoughts on the Mario implementation. What is to be gained from training?
 * 1. Should the states be rough and abstract? Yes. How abstract? It should be 
 *    abstract enough that the number of state-action pairs is exceedingly large.
 *    But if it is too abstract, we may lose the approximate Markov property. 
 *    (e.g. if the defined Mario state lacks "Mario's position", then suppose
 *    two original scenes, one with Mario on high platform, the other wiht Mario 
 *    on low platform, and other parameters the same. They have the same abstract
 *    state S. But S x Action A -> undetermined for the two scenes.
 *       With that said, we hope given many trials and a large state space the
 *    effect is not affecting us.
 *  
 * 2. Learning for specific actions (keystrokes) or movement preferences?
 *    Learning for keystrokes seems to be hard, but can be tolerated. Consider we
 *    can first hard-code the preferences, and modify the reward function to "unit
 *    learn" the keystroke combo. For example, we could define first learning unit
 *    to be "advance", and set reward to be large for every step going rightward.
 *    Then we train the "search" unit, etc.
 *      After the units complete, we face the problem that given a scene, what is
 *    the task to carry out. This can be completed using a higher-level QTable, or
 *    simply estimate the reward given by carrying out each task, and pick the
 *    best-rewarded.
 *        I think the latter approach is easier, but possibly contain bugs. Let's see
 *    whether is will become a problem.
 * 
 * 3. How to let Mario advance?
 *    -given a scene, abstract to Mario state
 *    -construct a QTable AdvanceTable, containing State, Action pairs
 *    -each Action is a combination of keystrokes
 *    -the MDP is also learned, not predetermined?
 *    -the reward function: the number of steps rightward taken
 *    -possible problem: how to let Mario jump through gaps, platforms and enemies?
 *        -jump until necessary? could give negative rewards for unnecessary jumps
 *    -the Mario state should contain "complete" information about the scene
 *        -idea: "poles", where the Mario should be jumping off and how far?
 * 
 * */

package edu.stanford.cs229.agents;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import radboud.meertens.ActionModificatorFactory;
import radboud.meertens.ActionModificatorInterface;
import ch.idsia.agents.Agent;
import ch.idsia.agents.LearningAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.tasks.LearningTask;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.MarioAIOptions;

/**
 * The learning agent.
 * 
 * @author kunyi@stanford.edu (Kun Yi)
 */
public class MarioRLAgent implements LearningAgent {

	private String name;

	// Training options, task and quota.
	private MarioAIOptions options;
	private LearningTask learningTask;

	// Fields for the Mario Agent
	private MarioState currentState;

	// Associated Qtable for the agent. Used for RL training.
	private ActionQtable actionTable;

	// The type of phase the Agent is in.
	// INIT: initial phase
	// LEARN: accumulatively update the Qtable
	private enum Phase {
		INIT, LEARN, EVAL
	};

	private Phase currentPhase = Phase.INIT;

	private int learningTrial = 0;

	private List<Integer> scores = new ArrayList<Integer>(
			LearningParams.NUM_TRAINING_ITERATIONS);
	private List<String> evaluationInfos = new ArrayList<String>(
			LearningParams.NUM_TRAINING_ITERATIONS);
	ActionModificatorInterface twitchObject;

	public MarioRLAgent() throws Exception {
		twitchObject = ActionModificatorFactory.getActionModificator();
		twitchObject.startListening();
		setName("BNAIC teaches mario!!");

		currentState = new MarioState();
		actionTable = new ActionQtable(MarioAction.TOTAL_ACTIONS);
		System.out.println("Loaded qtable: " + LearningParams.LOAD_QTABLE);
		if (LearningParams.LOAD_QTABLE) {
			System.out.println("Loaded qtable!!!!: "
					+ LearningParams.LOAD_QTABLE);
			actionTable.loadQtable(LearningParams.FINAL_QTABLE_NAME);
			InformationSingleton.getInstance().setLevel(loadLevel());
			// Load actionTable
			// Load scores
			scores = loadScores();
			evaluationInfos = loadEvaluationInfos();
		}

		Logger.println(0, "*************************************************");
		Logger.println(0, "*                                               *");
		Logger.println(0, "*                Super Mario 229                *");
		Logger.println(0, "*                 Twitch Agent created!         *");
		Logger.println(0, "*                                               *");
		Logger.println(0, "*************************************************");
	}

	private List<String> loadEvaluationInfos() {
		String scorefile = "evaluationInfosReached.txt";
		try {
			// To read the list from a file, do the following:
			FileInputStream fis = new FileInputStream(scorefile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			List<String> newScores = (ArrayList<String>)ois.readObject();
			ois.close();
			return newScores;
		} catch (Exception e) {
			System.err.println("Failed to load scores from: " + scorefile);
		}
		// return 0;
		//return new ArrayList<Integer>(LearningParams.NUM_TRAINING_ITERATIONS);
		return new ArrayList<String>(LearningParams.NUM_TRAINING_ITERATIONS);
	}

	private List<Integer> loadScores() {
		String scorefile = "scoresReached.txt";
		try {

			// To read the list from a file, do the following:

			FileInputStream fis = new FileInputStream(scorefile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			List<Integer> newScores = (ArrayList<Integer>)ois.readObject();
			ois.close();
			return newScores;
		} catch (Exception e) {
			System.err.println("Failed to load scores from: " + scorefile);
		}
		// return 0;
		return new ArrayList<Integer>(LearningParams.NUM_TRAINING_ITERATIONS);
	}

	@Override
	public boolean[] getAction() {
		// Transforms the best action number to action array.
		int actionNumber = actionTable.getNextAction(currentState
				.getStateNumber(), this.currentPhase==Phase.LEARN);
		// System.out.println("MarioRLAgent.getAction: Current phas learn: " +
		// (this.currentPhase == Phase.LEARN) + " eval: " +
		// (this.currentPhase==Phase.EVAL) + " else: " +
		// (this.currentPhase==Phase.INIT));
		if(this.currentPhase == Phase.LEARN)
		{
			//System.out.println("We are now learning" + InformationSingleton.getInstance().getFramesTrained());
			InformationSingleton.getInstance().trainFrame();
		}
		if (this.currentPhase == Phase.LEARN
				&& LearningParams.PLAYING_WITH_TWITCH) {
			int newactionNumber = twitchObject.modifyAction(actionNumber);
			actionTable.overrideNextAction(newactionNumber);
			// System.out.println("MarioRLAgent.getAction: overwriting action for "
			// + actionNumber + " to " + newactionNumber);
			actionNumber = newactionNumber;

		} else if (currentPhase == Phase.INIT) {
			return MarioAction.DO_NOTHING.getAction();

		} else {
			// System.out.println("MarioRLAgent.getAction: Not overwriting action");
		}

		Logger.println(2, "Next action: " + actionNumber + "\n");

		return MarioAction.getAction(actionNumber);
	}

	/**
	 * Importance of this function: the scene observation is THE RESULT after
	 * performing some action given the previous state. Therefore we could get
	 * information on: 1. prev state x prev action -> current state. 2. get the
	 * reward for prev state, prev action pair.
	 * 
	 * The reward function, however, is not provided and has to be customized.
	 */
	@Override
	public void integrateObservation(Environment environment) {
		// Update the current state.
		currentState.update(environment);

		if (currentPhase == Phase.INIT && environment.isMarioOnGround()) {
			// Start learning after Mario lands on the ground.
			Logger.println(1, "============== Learning Phase =============");
			currentPhase = Phase.LEARN;
		} 
		else if (currentPhase == Phase.LEARN) {
			// Update the Qvalue entry in the Qtable.
			//System.out.println("Updating the qvalue as we are learning");
			actionTable.updateQvalue(currentState.calculateReward(),currentState.getStateNumber(), currentPhase==Phase.LEARN);
		}
		else
		{
			//System.out.println("Not learning and not init");
		}
	}

	private void learnOnce() {
		Logger.println(1, "================================================");
		Logger.println(1, "Trial: %d of %d", learningTrial,
				LearningParams.NUM_MODES_TO_TRAIN
						* LearningParams.NUM_SEEDS_TO_TRAIN
						* LearningParams.NUM_TRAINING_ITERATIONS);

		init();
		learningTask.runSingleEpisode(1);

		EvaluationInfo evaluationInfo = learningTask.getEnvironment()
				.getEvaluationInfo();

		int score = evaluationInfo.computeWeightedFitness();

		Logger.println(1, "Intermediate SCORE = " + score);
		Logger.println(1, evaluationInfo.toStringSingleLine());

		scores.add(score);
		evaluationInfos.add(evaluationInfo.toStringSingleLine() + " level: "
				+ InformationSingleton.getInstance().getLevelReached());
		// learningTask.getEnvironment().get
		dumpScores("TemporaryScores.txt");
		dumpEvaluationInfos("TemporaryEvaluationInfos.txt");
		dumpLevel();
		// Dump the info of the most visited states into file.
		if (LearningParams.DUMP_INTERMEDIATE_QTABLE) {
			actionTable.dumpQtable(String.format(
					LearningParams.QTABLE_NAME_FORMAT, learningTrial));
			dumpLevel();
		}

		learningTrial++;
	}

	@Override
	public void learn() {
		InformationSingleton.getInstance().setEvaluating(false);

		System.out.println("Learning level reached "
				+ InformationSingleton.getInstance().getLevelReached());
		options.setLevelDifficulty(Math.min(InformationSingleton.getInstance()
				.getWorldReached(), LearningParams.MAXIMUM_DIFFUCULTY));
		options.setMarioMode(0);
		options.setLevelRandSeed(InformationSingleton.getInstance()
				.getLevelReached());
		
		// TODO maybe change this
		//InformationSingleton.getInstance().setLevel(InformationSingleton.getInstance().getLevelReached()+1);
		
		options.setVisualization(LearningParams.VISUALISE_LEARNING);
		// options.setScale2X(true);
		for (int i = 0; i < LearningParams.NUM_TRAINING_ITERATIONS; i++) {
			options.setVisualization(i < LearningParams.VISUALISE_FIRST_X_LEARNING
					|| LearningParams.VISUALISE_LEARNING);
			options.setLevelType(getLevelType());

			learnOnce();
			if (learningTask.getEnvironment().getEvaluationInfo().marioStatus == Mario.STATUS_WIN
					&& LearningParams.PROCEED_TO_NEXT_LEVEL_AFTER_COMPLETION) {
				// System.out.println("Won the level!");
				InformationSingleton.getInstance()
						.setLevel(
								InformationSingleton.getInstance()
										.getLevelReached() + 1);
				options.setLevelRandSeed(InformationSingleton.getInstance()
						.getLevelReached());
			}
		}

		setUpForEval();
	}

	private int getLevelType() {
		if ((InformationSingleton.getInstance().getLevelReached() % LearningParams.NEW_WORLD_EVERY_X_LEVELS) % 5 == 2) {
			return 1;
		} else if ((InformationSingleton.getInstance().getLevelReached() % LearningParams.NEW_WORLD_EVERY_X_LEVELS) % 5 == 4) {
			return 2;
		} else {
			return 0;
		}
	}

	@Override
	public void init() {
		Logger.println(1, "=================== Init =================");
		currentPhase = Phase.INIT;
		actionTable.explorationChance = LearningParams.EXPLORATION_CHANCE;
	}

	@Override
	public void reset() {
		Logger.println(1, "================== Reset =================");
		currentState = new MarioState();
	}

	public void setUpForEval() {
		Logger.println(1, "============= Dumping Results ============");
		// Dump final Qtable.
		actionTable.dumpQtable(LearningParams.FINAL_QTABLE_NAME);
		// Dump training scores.
		dumpScores(LearningParams.SCORES_NAME);

		// Entering EVAL phase.
		Logger.println(1, "================ Eval Phase ==============");
		currentPhase = Phase.EVAL;
		InformationSingleton.getInstance().setEvaluating(true);
		// Set exploration chance for evaluations.
		actionTable.explorationChance = LearningParams.EVAL_EXPLORATION_CHANCE;
	}

	ActionQtable getActionTable() {
		return this.actionTable;
	}

	private void dumpScores(String logfile) {
		Utils.dump(logfile, Utils.join(scores, "\n"));
	}

	private void dumpEvaluationInfos(String logfile) {
		Utils.dump(logfile, Utils.join(evaluationInfos, "\n"));
	}

	public void setOptions(MarioAIOptions options) {
		this.options = options;
	}

	/**
	 * Gives access to the evaluator through learningTask.evaluate(Agent).
	 */
	@Override
	public void setLearningTask(LearningTask learningTask) {
		this.learningTask = learningTask;
	}

	@Deprecated
	@Override
	public void setEvaluationQuota(long num) {
	}

	@Deprecated
	@Override
	public Agent getBestAgent() {
		return this;
	}

	@Override
	public void setObservationDetails(int rfWidth, int rfHeight, int egoRow,
			int egoCol) {
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Deprecated
	@Override
	public void newEpisode() {
	}

	@Deprecated
	@Override
	public void giveReward(float reward) {
	}

	// This function is completely bogus! intermediateReward is not properly
	// given
	// either modify the intermediate reward calculation or ignore this function
	// and do reward update elsewhere. forexample when integrating observation.
	@Deprecated
	@Override
	public void giveIntermediateReward(float intermediateReward) {
		// TODO Auto-generated method stub
	}

	public int loadLevel() {
		String logfile = "levelReached.txt";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(logfile));
			String line = reader.readLine();
			int level = Integer.valueOf(line);
			reader.close();
			return level;
		} catch (Exception e) {
			System.err.println("Failed to load qtable from: " + logfile);
		}
		return 0;
	}

	public void dumpLevel() {
		String logfile = "levelReached.txt";

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(logfile));
			writer.write(InformationSingleton.getInstance().getLevelReached()
					+ "");
			writer.write("\n^^Level reached!^^");
			writer.close();
		} catch (IOException x) {
			System.err.println("Failed to write qtable to: " + logfile);
		}

		try {
			String scorefile = "scoresReached.txt";
			FileOutputStream fos = new FileOutputStream(scorefile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);

			oos.writeObject(scores);
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			String scorefile = "evaluationInfosReached.txt";
			FileOutputStream fos = new FileOutputStream(scorefile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			//private List<String> evaluationInfos = new ArrayList<String>(
				//	LearningParams.NUM_TRAINING_ITERATIONS);

			oos.writeObject(evaluationInfos);
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
