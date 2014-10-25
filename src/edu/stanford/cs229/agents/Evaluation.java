package edu.stanford.cs229.agents;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.benchmark.tasks.LearningTask;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.MarioAIOptions;

/**
 * Class to evaluate performance.
 * 
 * @author zheyang@stanford.edu (Zhe Yang)
 */
public class Evaluation {

	public static void main(String[] args) throws Exception {
		int numRounds = 50000;

		Evaluation eval = new Evaluation();
		List<Float> averageEvaluationScores = new ArrayList<Float>(numRounds);

		for (int i = 0; i < numRounds; i++) {
			float finalScore = eval.evaluate();
			System.out.println("Final Score " + i + " = " + finalScore + "");
			InformationSingleton.getInstance().setFitness((int) finalScore);

			averageEvaluationScores.add(finalScore);
			eval.dumpResult();
		}
		eval.dumpResult();

		System.exit(0);
	}

	public static class EvaluationData {
		public EvaluationData() {
			Date data = new Date();
			milisStarted = data.getTime();
		}

		long milisStarted;
		long milisTotal;
		float averageScore = 0;
		float wins = 0;
		float averageKills = 0;
		float averageDistance = 0;
		float averageTimeSpent = 0;
		float coinsGained = 0;
		float flowersDevoured;
		float killsTotal;
		float mushroomsDevoured;
		int totalStatesExplored;
		int killsByFire;
		int killsByStomp;
		int killsByShell;
		int timeLeft;
		int totalNumberOfCoins;
		int totalNumberOfCreatures;
		int totalNumberOfFlowers;
		int totalNumberOfMushrooms;

		float coinsGainedPerEvaluation;
		float percentageCoinsGained;

		float flowersDevouredPerEvaluation;
		float percentageFlowersDevoured;

		float mushroomsPerEvaluation;
		float percentageMushrooms;

		float killsByFirePerEvaluation;
		float percentageKillsByFire;

		float killsByStompPerEvaluation;
		float percentageKillsByStomp;

		float killsByShellPerEvaluation;
		float percentageKillsByShell;

		float timeLeftPerEvaluation;

		public static String getHeader() {
			return "averageScore, wins, averageKills, averageDistance, averageTimeSpent,coinsGainedPerEvaluation,percentageCoinsGained,flowersDevouredPerEvaluation,percentageFlowersDevoured,mushroomsPerEvaluation,percentageMushrooms,killsByFirePerEvaluation,percentageKillsByFire,killsByStompPerEvaluation,percentageKillsByStomp,killsByShellPerEvaluation,percentageKillsByShell,timeLeftPerEvaluation,killsTotal,milisTotal,totalStatesExplored";
		}

		public String toString() {
			return String
					.format("%f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %d %d",
							averageScore, wins, averageKills, averageDistance,
							averageTimeSpent, coinsGainedPerEvaluation,
							percentageCoinsGained,
							flowersDevouredPerEvaluation,
							percentageFlowersDevoured, mushroomsPerEvaluation,
							percentageMushrooms, killsByFirePerEvaluation,
							percentageKillsByFire, killsByStompPerEvaluation,
							percentageKillsByStomp, killsByShellPerEvaluation,
							percentageKillsByShell, timeLeftPerEvaluation,
							killsTotal, milisTotal, totalStatesExplored);

		}

		public void computeFinalEvalInfo(int totalStatesExploredInteger) {
			averageScore /= LearningParams.NUM_EVAL_ITERATIONS;
			wins /= LearningParams.NUM_EVAL_ITERATIONS;
			averageKills /= LearningParams.NUM_EVAL_ITERATIONS;
			averageDistance /= LearningParams.NUM_EVAL_ITERATIONS;
			averageTimeSpent /= LearningParams.NUM_EVAL_ITERATIONS;

			coinsGainedPerEvaluation = coinsGained
					/ LearningParams.NUM_EVAL_ITERATIONS;
			percentageCoinsGained = coinsGained / totalNumberOfCoins;

			flowersDevouredPerEvaluation = flowersDevoured
					/ LearningParams.NUM_EVAL_ITERATIONS;
			percentageFlowersDevoured = flowersDevoured / totalNumberOfFlowers;

			mushroomsPerEvaluation = mushroomsDevoured
					/ LearningParams.NUM_EVAL_ITERATIONS;
			percentageMushrooms = mushroomsDevoured / totalNumberOfMushrooms;

			killsByFirePerEvaluation = killsByFire
					/ LearningParams.NUM_EVAL_ITERATIONS;
			percentageKillsByFire = killsByFire / killsTotal;

			killsByStompPerEvaluation = killsByStomp
					/ LearningParams.NUM_EVAL_ITERATIONS;
			percentageKillsByStomp = killsByStomp / killsTotal;

			killsByShellPerEvaluation = killsByShell
					/ LearningParams.NUM_EVAL_ITERATIONS;
			percentageKillsByShell = killsByShell / killsTotal;

			timeLeftPerEvaluation = timeLeft
					/ LearningParams.NUM_EVAL_ITERATIONS;

			killsTotal /= LearningParams.NUM_EVAL_ITERATIONS;
			totalStatesExplored = totalStatesExploredInteger;

			Date data = new Date();
			milisTotal = data.getTime() - milisStarted;
		}

		public void accumulateEvalInfo(EvaluationInfo evaluationInfo) {
			averageScore += evaluationInfo.computeWeightedFitness();
			wins += evaluationInfo.marioStatus == Mario.STATUS_WIN ? 1 : 0;
			averageKills += 1.0 * evaluationInfo.killsTotal
					/ evaluationInfo.totalNumberOfCreatures;
			averageDistance += 1.0 * evaluationInfo.distancePassedCells
					/ evaluationInfo.levelLength;
			averageTimeSpent += evaluationInfo.timeSpent;
			coinsGained += 1.0 * evaluationInfo.coinsGained;
			flowersDevoured += 1.0 * evaluationInfo.flowersDevoured;
			killsTotal += 1.0 * evaluationInfo.killsTotal;
			killsByFire += evaluationInfo.killsByFire;
			killsByShell += evaluationInfo.killsByShell;
			killsByStomp += evaluationInfo.killsByStomp;
			killsTotal += evaluationInfo.killsTotal;
			mushroomsDevoured += evaluationInfo.mushroomsDevoured;
			timeLeft += evaluationInfo.timeLeft;
			totalNumberOfCoins += evaluationInfo.totalNumberOfCoins;
			totalNumberOfCreatures += evaluationInfo.totalNumberOfCreatures;
			totalNumberOfFlowers += evaluationInfo.totalNumberOfFlowers;
			totalNumberOfMushrooms += evaluationInfo.totalNumberOfMushrooms;

		}
	}

	private MarioAIOptions marioAIOptions;
	private MarioRLAgent agent;

	private int evaluationSeed;

	private List<EvaluationData> evaluationResults = new ArrayList<EvaluationData>();

	public Evaluation() throws Exception {

		evaluationSeed = 0;
		agent = new MarioRLAgent();
		// agent = new HumanKeyboardRLAgent(marioAIOptions);

		marioAIOptions = new MarioAIOptions();
		marioAIOptions.setScale2X(true);
		marioAIOptions.setAgent(agent);

		marioAIOptions.setFPS(LearningParams.FRAMES_PER_SECOND_FPS);

		agent.setOptions(marioAIOptions);

		agent.setLearningTask(new LearningTask(marioAIOptions));
		marioAIOptions.setScale2X(false);
	}

	public float evaluate() {
		agent.learn();

		// Visualise an amount of evaluations
		BasicTask basicTask = new BasicTask(marioAIOptions);
		for (int i = 0; i < LearningParams.VISUALISE_FIRST_X_EVALUATIONS; i++) {
			// Set to a different seed for EVERY evaluation.
			System.out
					.println("Evaluation.evaluate: Starting evaluation number "
							+ i);
			marioAIOptions.setLevelRandSeed(evaluationSeed++);
			marioAIOptions.setVisualization(true);

			// Make evaluation on the same episode once.
			int failedCount = 0;
			while (!basicTask.runSingleEpisode(1)) {
				System.err
						.println("MarioAI: out of computational time per action? "
								+ failedCount);
				failedCount++;
			}
		}

		// End of visualisation
		basicTask = new BasicTask(marioAIOptions);
		EvaluationData results = new EvaluationData();
		evaluationResults.add(results);

		for (int i = 0; i < LearningParams.NUM_EVAL_ITERATIONS; i++) {
			// Set to a different seed for EVERY evaluation.
			marioAIOptions.setLevelRandSeed(evaluationSeed++);
			marioAIOptions.setLevelDifficulty(0);
			marioAIOptions.setVisualization(LearningParams.VISUALISE_TESTING);
			int failedCount = 0;
			while (!basicTask.runSingleEpisode(1)) {
				System.err
						.println("MarioAI: out of computational time per action? "
								+ failedCount);
				failedCount++;
			}

			EvaluationInfo evaluationInfo = basicTask.getEvaluationInfo();
			results.accumulateEvalInfo(evaluationInfo);
		}

		results.computeFinalEvalInfo(agent.getActionTable().getTable().size());
		return results.averageScore;
	}

	public void dumpResult() {
		Utils.dump("eval.txt", Utils.join(evaluationResults, "\n"),
				EvaluationData.getHeader());
	}

	public static String getParam(String[] args, String name) {
		for (int i = 0; i < args.length; i++) {
			String s = args[i];
			if (s.startsWith("-") && s.substring(1).equals(name)) {
				if (i + 1 < args.length) {
					String v = args[i + 1];
					if (!v.startsWith("-")) {
						return v;
					}
				}
				return "";
			}
		}
		return null;
	}

	public static boolean isNullOrEmpty(String v) {
		return v == null || v.isEmpty();
	}

	public static int getIntParam(String[] args, String name, int defaultValue) {
		String v = getParam(args, name);
		return isNullOrEmpty(v) ? defaultValue : Integer.valueOf(v);
	}

	public static boolean getBooleanParam(String[] args, String name) {
		String v = getParam(args, name);
		return v != null;
	}

}
