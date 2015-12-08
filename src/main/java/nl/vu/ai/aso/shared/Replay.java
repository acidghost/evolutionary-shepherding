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
    private EvolutionType evolutionType;
    private int numShepherd;
    private int numSheep;

    private Replay(List<DoubleVectorIndividual> bestOfGeneration, int split, int totalSteps, EvolutionType evolutionType, int numShepherd, int numSheep) {
        this.bestOfGeneration = bestOfGeneration;
        this.split = split;
        this.totalSteps = totalSteps;
        this.evolutionType = evolutionType;
        this.numShepherd = numShepherd;
        this.numSheep = numSheep;
    }

    public Replay(List<DoubleVectorIndividual> bestOfGeneration, int split, int totalSteps) {
        this(bestOfGeneration, split, totalSteps, EvolutionType.HETERO, split, bestOfGeneration.size() - split);
    }

    public Replay(List<DoubleVectorIndividual> bestOfGeneration, int totalSteps, int numShepherd, int numSheep) {
        this(bestOfGeneration, 1, totalSteps, EvolutionType.HOMO, numShepherd, numSheep);
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

    public EvolutionType getEvolutionType() {
        return evolutionType;
    }

    public int getNumShepherd() {
        return numShepherd;
    }

    public int getNumSheep() {
        return numSheep;
    }

}
