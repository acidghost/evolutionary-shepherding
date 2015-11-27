package nl.vu.ai.aso.evolution;

import ec.EvolutionState;

/**
 * Created by acidghost on 27/11/15.
 */
public interface ICustomBreeder {

    boolean shouldBreedSubpop(EvolutionState state, int subpop, int threadnum);

}
