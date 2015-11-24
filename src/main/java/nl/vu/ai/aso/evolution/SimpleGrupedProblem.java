package nl.vu.ai.aso.evolution;

import ec.EvolutionState;
import ec.Population;
import ec.Problem;
import ec.coevolve.GroupedProblemForm;
import ec.simple.SimpleFitness;

import java.util.ArrayList;

/**
 * Created by acidghost on 24/11/15.
 */
abstract public class SimpleGrupedProblem extends Problem implements GroupedProblemForm {

    public void preprocessPopulation(EvolutionState evolutionState, Population pop, boolean[] prepareForAssessment, boolean countVictoriesOnly) {
        for( int i = 0 ; i < pop.subpops.length ; i++ ) {
            if (prepareForAssessment[i]) {
                for( int j = 0 ; j < pop.subpops[i].individuals.length ; j++ ) {
                    ((SimpleFitness)(pop.subpops[i].individuals[j].fitness)).trials = new ArrayList();
                }
            }
        }
    }

}
