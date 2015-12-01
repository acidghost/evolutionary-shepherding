package nl.vu.ai.aso.evolution;

import ec.*;
import ec.coevolve.GroupedProblemForm;
import ec.simple.SimpleFitness;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;
import nl.vu.ai.aso.shared.EvaluationResults;
import nl.vu.ai.aso.simulation.Herding;
import nl.vu.ai.aso.simulation.HerdingGUI;

import java.util.ArrayList;

/**
 * Created by acidghost on 24/11/15.
 */
public class HerdingProblem extends Problem implements GroupedProblemForm {

    private final String POP_SEPARATOR = "pop.separator";
    private final String EVAL_PREDATOR = "eval.predator";
    private final String EVAL_STEPS = "eval.evaluations";

    private static int evaluationCouter = 0;

    public void preprocessPopulation(EvolutionState evolutionState, Population pop, boolean[] prepareForAssessment, boolean countVictoriesOnly) {
        for( int i = 0 ; i < pop.subpops.length ; i++ ) {
            if (prepareForAssessment[i]) {
                for( int j = 0 ; j < pop.subpops[i].individuals.length ; j++ ) {
                    ((SimpleFitness)(pop.subpops[i].individuals[j].fitness)).trials = new ArrayList();
                }
            }
        }
    }

    public void postprocessPopulation(EvolutionState evolutionState, Population pop, boolean[] assessFitness, boolean countVictoriesOnly) {
        for (int i=0; i < pop.subpops.length; i++) {
            if (assessFitness[i]) {
                Subpopulation subpop = pop.subpops[i];
                for (int j=0; j < subpop.individuals.length; j++) {
                    Individual individual = subpop.individuals[j];
                    SimpleFitness fit = (SimpleFitness) individual.fitness;

                    double fitSum = 0;
                    for (Object score : fit.trials) {
                        fitSum += (Double) score;
                    }

                    // evolutionState.output.message("Number of trials: " + fit.trials.size());
                    fit.setFitness(evolutionState, fitSum / fit.trials.size(), false);
                    individual.evaluated = true;
                }
            }
        }
    }

    public void evaluate(EvolutionState evolutionState, Individual[] individuals, boolean[] updateFitness, boolean countVictoriesOnly, int[] subpops, int threadnum) {
        int split = evolutionState.parameters.getInt(new Parameter(POP_SEPARATOR), new Parameter(POP_SEPARATOR + ".default"));
        boolean predator = evolutionState.parameters.getBoolean(new Parameter(EVAL_PREDATOR), new Parameter(EVAL_PREDATOR + ".default"), false);
        int evaluations = evolutionState.parameters.getInt(new Parameter(EVAL_STEPS), new Parameter(EVAL_STEPS));

        evaluationCouter++;
        evolutionState.output.message("Evaluate: " + evaluationCouter);

        ArrayList<double[]> shepherd = new ArrayList<>();
        ArrayList<double[]> sheep = new ArrayList<>();
        for(int i = 0; i < individuals.length; i++) {
            DoubleVectorIndividual individual = (DoubleVectorIndividual) individuals[i];
            evolutionState.output.message("Doing individual " + individual.hashCode() + " " + individual.genotypeToStringForHumans());
            double[] genome = individual.genome;
            if (i < split) {
                shepherd.add(genome);
            } else {
                sheep.add(genome);
            }
        }

        // EvaluationResults results = Herding.runSimulation(evaluations, shepherd, sheep, predator);
        EvaluationResults results = HerdingGUI.runSimulation(evaluations, 10, shepherd, sheep, predator);
        evolutionState.output.message("Evaluation finished.\n" + results.toString());

        int sheepCounter = 0;
        double[] sheepScores = results.getSheepScore();
        for (int i = 0; i < individuals.length; i++) {
            if (updateFitness[i]) {
                Individual individual = individuals[i];
                if (i < split) {
                    double score = results.getShepherdScore();
                    individual.fitness.trials.add(score);
                    ((SimpleFitness) individual.fitness).setFitness(evolutionState, score, false);
                } else {
                    double score = sheepScores[sheepCounter];
                    individual.fitness.trials.add(score);
                    ((SimpleFitness) individual.fitness).setFitness(evolutionState, score, false);
                    sheepCounter++;
                }
            }
        }
    }

}
