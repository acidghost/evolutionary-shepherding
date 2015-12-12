package nl.vu.ai.aso.evolution;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;
import nl.vu.ai.aso.shared.EvaluationResults;
import nl.vu.ai.aso.shared.Replay;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by acidghost on 05/12/15.
 */
public class HomoHerdingProblem extends HerdingProblem {

    private static final Parameter SHEPH_NUM = new Parameter("eval.num-shepherd");
    private static final Parameter SHEEP_NUM = new Parameter("eval.num-sheep");

    @Override
    public void evaluate(EvolutionState evolutionState, Individual[] individuals, boolean[] updateFitness, boolean countVictoriesOnly, int[] subpops, int threadnum) {
        int numSheep = evolutionState.parameters.getInt(SHEEP_NUM, SHEEP_NUM);
        int numShepherd = evolutionState.parameters.getInt(SHEPH_NUM, SHEPH_NUM);
        boolean predator = evolutionState.parameters.getBoolean(new Parameter(EVAL_PREDATOR), new Parameter(EVAL_PREDATOR + ".default"), false);
        int evaluations = evolutionState.parameters.getInt(new Parameter(EVAL_STEPS), new Parameter(EVAL_STEPS));

        HerdingProblem.evaluationCounter++;
        evolutionState.output.message("Evaluate: " + HerdingProblem.evaluationCounter);

        ArrayList<double[]> shepherd = new ArrayList<>();
        ArrayList<double[]> sheep = new ArrayList<>();
        assert individuals.length == 2;
        DoubleVectorIndividual shepherdIndividual = (DoubleVectorIndividual) individuals[0];
        DoubleVectorIndividual sheepIndividual = (DoubleVectorIndividual) individuals[1];
        for (int i = 0; i < numShepherd; i++) {
            shepherd.add(shepherdIndividual.genome);
        }
        for (int i = 0; i < numSheep; i++) {
            sheep.add(sheepIndividual.genome);
        }

        for (int trial = 0; trial < TRIALS; trial++) {
            EvaluationResults results = getEvaluationResults(evolutionState, predator, evaluations, shepherd, sheep);

            double[] sheepScores = results.getSheepScore();
            double[] shepherdScores = results.getShepherdScore();

            double finalShepherdScore = 0.0;
            for (double shepherdScore : shepherdScores) {
                finalShepherdScore += shepherdScore;
            }
            finalShepherdScore = finalShepherdScore / shepherdScores.length;

            double finalSheepScore = 0.0;
            for (double sheepScore : sheepScores) {
                finalSheepScore += sheepScore;
            }
            finalSheepScore = finalSheepScore / sheepScores.length;

            if (updateFitness[0]) {
                CoESFitness shepherdFitness = (CoESFitness) shepherdIndividual.fitness;
                shepherdFitness.trials.add(finalShepherdScore);
                shepherdFitness.setFitness(evolutionState, finalShepherdScore, false);
                shepherdFitness.sheepStatuses.add(results.getSheepStatus());
            }

            if (updateFitness[1]) {
                CoESFitness sheepFitness = (CoESFitness) sheepIndividual.fitness;
                sheepFitness.trials.add(finalSheepScore);
                sheepFitness.setFitness(evolutionState, finalSheepScore, false);
                sheepFitness.sheepStatuses.add(results.getSheepStatus());
            }
        }
    }

    @Override
    public Replay getReplay(EvolutionState evolutionState, List<DoubleVectorIndividual> bestOfGeneration, int split, int totalSteps) {
        int numSheep = evolutionState.parameters.getInt(SHEEP_NUM, SHEEP_NUM);
        int numShepherd = evolutionState.parameters.getInt(SHEPH_NUM, SHEPH_NUM);
        return new Replay(bestOfGeneration, totalSteps, numShepherd, numSheep);
    }

}
