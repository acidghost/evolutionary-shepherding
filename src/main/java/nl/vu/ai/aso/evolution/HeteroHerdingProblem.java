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
 * Created by acidghost on 24/11/15.
 */
public class HeteroHerdingProblem extends HerdingProblem {

    public void evaluate(EvolutionState evolutionState, Individual[] individuals, boolean[] updateFitness, boolean countVictoriesOnly, int[] subpops, int threadnum) {
        int split = evolutionState.parameters.getInt(new Parameter(POP_SEPARATOR), new Parameter(POP_SEPARATOR + ".default"));
        boolean predator = evolutionState.parameters.getBoolean(new Parameter(EVAL_PREDATOR), new Parameter(EVAL_PREDATOR + ".default"), false);
        int evaluations = evolutionState.parameters.getInt(new Parameter(EVAL_STEPS), new Parameter(EVAL_STEPS));

        HerdingProblem.evaluationCounter++;
        evolutionState.output.message("Evaluate: " + HerdingProblem.evaluationCounter);

        ArrayList<double[]> shepherd = new ArrayList<>();
        ArrayList<double[]> sheep = new ArrayList<>();
        for(int i = 0; i < individuals.length; i++) {
            DoubleVectorIndividual individual = (DoubleVectorIndividual) individuals[i];
            // evolutionState.output.message("Doing individual " + individual.hashCode() + " " + individual.genotypeToStringForHumans());
            double[] genome = individual.genome;
            if (i < split) {
                shepherd.add(genome);
            } else {
                sheep.add(genome);
            }
        }

        for (int trial = 0; trial < TRIALS; trial++) {
            EvaluationResults results = getEvaluationResults(evolutionState, predator, evaluations, shepherd, sheep);

            int sheepCounter = 0;
            int shepherdCounter = 0;
            double[] sheepScores = results.getSheepScore();
            double[] shepherdScores = results.getShepherdScore();
            for (int i = 0; i < individuals.length; i++) {
                if (updateFitness[i]) {
                    Individual individual = individuals[i];
                    CoESFitness fitness = (CoESFitness) individual.fitness;
                    if (i < split) {
                        double score = shepherdScores[shepherdCounter];
                        fitness.trials.add(score);
                        fitness.setFitness(evolutionState, score, false);
                        shepherdCounter++;
                    } else {
                        double score = sheepScores[sheepCounter];
                        fitness.trials.add(score);
                        fitness.setFitness(evolutionState, score, false);
                        sheepCounter++;
                    }
                    fitness.sheepStatuses.add(results.getSheepStatus());
                }
            }
        }
    }

    @Override
    public Replay getReplay(EvolutionState evolutionState, List<DoubleVectorIndividual> bestOfGeneration, int split, int totalSteps) {
        return new Replay(bestOfGeneration, split, totalSteps);
    }

}
