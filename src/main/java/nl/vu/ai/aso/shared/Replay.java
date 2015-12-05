package nl.vu.ai.aso.shared;

import ec.vector.DoubleVectorIndividual;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by acidghost on 05/12/15.
 */
public class Replay implements Serializable {

    private List<DoubleVectorIndividual> bestOfGeneration;
    private int split;
    private int totalSteps;

    public Replay(List<DoubleVectorIndividual> bestOfGeneration, int split, int totalSteps) {
        this.bestOfGeneration = bestOfGeneration;
        this.split = split;
        this.totalSteps = totalSteps;
    }

    public List<DoubleVectorIndividual> getBestOfGeneration() {
        return bestOfGeneration;
    }

    public List<double[]> getBestGenomesOfGeneration() {
        return bestOfGeneration.stream().map(individual -> individual.genome).collect(Collectors.toList());
    }

    public int getSplit() {
        return split;
    }

    public int getTotalSteps() {
        return totalSteps;
    }
}
