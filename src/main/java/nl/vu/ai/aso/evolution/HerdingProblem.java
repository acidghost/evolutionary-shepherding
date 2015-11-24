package nl.vu.ai.aso.evolution;

import ec.*;
import ec.coevolve.GroupedProblemForm;
import ec.simple.SimpleFitness;
import ec.util.Parameter;
import ec.vector.IntegerVectorIndividual;

import java.util.ArrayList;

/**
 * Created by acidghost on 24/11/15.
 */
public class HerdingProblem extends Problem implements GroupedProblemForm {

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

                    double best = i < 2 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                    if (i < 2) {
                        for (int l=0; l < fit.trials.size(); l++) {
                            best = Math.max(((Double)(fit.trials.get(l))).doubleValue(), best);
                        }
                    } else {
                        for (int l=0; l < fit.trials.size(); l++) {
                            best = Math.min(((Double) (fit.trials.get(l))).doubleValue(), best);
                        }
                    }
                    fit.setFitness(evolutionState, best, i < 2 ? best == 400 : best == -400);
                    individual.evaluated = true;
                }
            }
        }
    }

    public void evaluate(EvolutionState evolutionState, Individual[] individuals, boolean[] updateFitness, boolean countVictoriesOnly, int[] subpops, int threadnum) {
        evolutionState.output.message("Evaluating individuals...");
        double totalSum = 0;
        for(int i = 0; i < individuals.length; i++) {
            IntegerVectorIndividual individual = (IntegerVectorIndividual) individuals[i];
            evolutionState.output.message(i + " - " + individual.genotypeToStringForHumans());
            int[] genome = individual.genome;
            for (int gene : genome) {
                totalSum += gene;
            }
        }

        evolutionState.output.message("Total sum: " + totalSum);

        int split = evolutionState.parameters.getInt(new Parameter("pop.separator"), new Parameter("pop.separator.default"));

        for (int i = 0; i < individuals.length; i++) {
            if (updateFitness[i]) {
                Individual individual = individuals[i];
                if (i < split) {
                    individual.fitness.trials.add(totalSum);
                    ((SimpleFitness) individual.fitness).setFitness(evolutionState, totalSum, false);
                } else {
                    individual.fitness.trials.add(-totalSum);
                    ((SimpleFitness) individual.fitness).setFitness(evolutionState, -totalSum, false);
                }
            }
        }
    }

}
