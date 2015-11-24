package nl.vu.ai.aso.evolution;

import com.sun.org.apache.bcel.internal.generic.POP;
import ec.*;
import ec.simple.SimpleFitness;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;
import nl.vu.ai.aso.simulation.Herding;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by acidghost on 24/11/15.
 */
public class HerdingProblem extends SimpleGrupedProblem {

    private final String POP_SEPARATOR = "pop.separator";
    private final String EVAL_PREDATOR = "eval.predator";

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

                    fit.setFitness(evolutionState, fitSum / fit.trials.size(), false);
                    individual.evaluated = true;
                }
            }
        }
    }

    public void evaluate(EvolutionState evolutionState, Individual[] individuals, boolean[] updateFitness, boolean countVictoriesOnly, int[] subpops, int threadnum) {
        int split = evolutionState.parameters.getInt(new Parameter(POP_SEPARATOR), new Parameter(POP_SEPARATOR + ".default"));
        boolean predator = evolutionState.parameters.getBoolean(new Parameter(EVAL_PREDATOR), new Parameter(EVAL_PREDATOR + ".default"), false);

        ArrayList<double[]> shepherd = new ArrayList<double[]>();
        ArrayList<double[]> sheep = new ArrayList<double[]>();
        for(int i = 0; i < individuals.length; i++) {
            DoubleVectorIndividual individual = (DoubleVectorIndividual) individuals[i];
            evolutionState.output.message(i + " - " + individual.genotypeToStringForHumans());
            double[] genome = individual.genome;
            if (i < split) {
                shepherd.add(genome);
            } else {
                sheep.add(genome);
            }
        }

        // TODO: use a custom class instead of a Map
        // TODO: use Lists instead of arrays as parameters..
        double[][] shepherdArray = new double[shepherd.size()][];
        double[][] sheepArray = new double[sheep.size()][];
        for (int i = 0; i < shepherd.size(); i++) {
            double[] shepherdIndividual = shepherd.get(i);
            shepherdArray[i] = shepherdIndividual;
        }
        for (int i = 0; i < sheep.size(); i++) {
            double[] sheepIndividual = sheep.get(i);
            sheepArray[i] = sheepIndividual;
        }
        Map<String, Double> results = Herding.runSimulation(shepherdArray, sheepArray, predator);

        for (int i = 0; i < individuals.length; i++) {
            if (updateFitness[i]) {
                Individual individual = individuals[i];
                if (i < split) {
                    double score = results.get("shepherd");
                    individual.fitness.trials.add(score);
                    ((SimpleFitness) individual.fitness).setFitness(evolutionState, score, false);
                } else {
                    double score = results.get("sheep");
                    individual.fitness.trials.add(score);
                    ((SimpleFitness) individual.fitness).setFitness(evolutionState, score, false);
                }
            }
        }
    }

}
