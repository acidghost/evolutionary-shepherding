package nl.vu.ai.aso.evolution;

import ec.EvolutionState;
import ec.coevolve.MultiPopCoevolutionaryEvaluator;

/**
 * Created by acidghost on 27/11/15.
 */
public class CoESEvaluator extends MultiPopCoevolutionaryEvaluator {

    @Override
    public boolean shouldEvaluateSubpop(EvolutionState state, int subpop, int threadnum) {
        return ((ICustomBreeder) state.breeder).shouldBreedSubpop(state, subpop, threadnum);
    }
}
