package nl.vu.ai.aso.evolution;

import ec.EvolutionState;
import ec.es.MuPlusLambdaBreeder;

/**
 * Created by acidghost on 27/11/15.
 */
public class CoESBreeder extends MuPlusLambdaBreeder implements ICustomBreeder {

    @Override
    public boolean shouldBreedSubpop(EvolutionState state, int subpop, int threadnum) {
        return true;
    }

}
