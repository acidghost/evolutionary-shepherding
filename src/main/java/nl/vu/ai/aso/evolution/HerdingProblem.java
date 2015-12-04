package nl.vu.ai.aso.evolution;

import ec.*;
import ec.coevolve.GroupedProblemForm;
import ec.simple.SimpleFitness;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;
import nl.vu.ai.aso.shared.EvaluationResults;
import nl.vu.ai.aso.simulation.Herding;
import nl.vu.ai.aso.simulation.HerdingGUI;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by acidghost on 24/11/15.
 */
public class HerdingProblem extends Problem implements GroupedProblemForm {

    public static final String POP_SEPARATOR = "pop.separator";
    public static final String EVAL_PREDATOR = "eval.predator";
    public static final String EVAL_STEPS = "eval.evaluations";

    private static int evaluationCounter = 0;

    public void preprocessPopulation(EvolutionState evolutionState, Population pop, boolean[] prepareForAssessment, boolean countVictoriesOnly) {
        for( int i = 0 ; i < pop.subpops.length ; i++ ) {
            if (prepareForAssessment[i]) {
                for( int j = 0 ; j < pop.subpops[i].individuals.length ; j++ ) {
                    CoESFitness fitness = (CoESFitness) pop.subpops[i].individuals[j].fitness;
                    fitness.trials = new ArrayList();
                    fitness.sheepStatuses = new ArrayList<>();
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

        evolutionState.output.message("\n\nFinished generation " + evolutionState.generation);

        List<Individual> bestOfGeneration = new ArrayList<>(evolutionState.population.subpops.length);
        for (int i = 0; i < evolutionState.population.subpops.length; i++) {
            Subpopulation subpop = evolutionState.population.subpops[i];
            Individual bestIndividual = subpop.individuals[0];
            for (int j = 1; j < subpop.individuals.length; j++) {
                Individual individual = subpop.individuals[j];
                if (individual.evaluated && individual.fitness.betterThan(bestIndividual.fitness)) {
                    bestIndividual = individual;
                }
            }
            bestOfGeneration.add(bestIndividual);
        }

        for (int i = 0; i < bestOfGeneration.size(); i++) {
            Individual individual = bestOfGeneration.get(i);
            evolutionState.output.message("Subpop " + i + ": " + individual.fitness.fitnessToStringForHumans());
        }

        try {
            OutputStream file = new FileOutputStream("serialized/best." + evolutionState.generation + ".ser");
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);
            output.writeObject(bestOfGeneration);
        } catch (IOException ioe) {
            evolutionState.output.fatal("Error serializing best of generation!");
            ioe.printStackTrace();
        }

        evolutionState.output.message("\n");
    }

    public void evaluate(EvolutionState evolutionState, Individual[] individuals, boolean[] updateFitness, boolean countVictoriesOnly, int[] subpops, int threadnum) {
        int split = evolutionState.parameters.getInt(new Parameter(POP_SEPARATOR), new Parameter(POP_SEPARATOR + ".default"));
        boolean predator = evolutionState.parameters.getBoolean(new Parameter(EVAL_PREDATOR), new Parameter(EVAL_PREDATOR + ".default"), false);
        int evaluations = evolutionState.parameters.getInt(new Parameter(EVAL_STEPS), new Parameter(EVAL_STEPS));

        evaluationCounter++;
        evolutionState.output.message("Evaluate: " + evaluationCounter);

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

        EvaluationResults results;
        double proportionInGUI = 3.0 / 4.0;
        if (evolutionState.generation > evolutionState.numGenerations * proportionInGUI) {
            results = HerdingGUI.runSimulation(evaluations, 30, shepherd, sheep, predator);
        } else {
            results = Herding.runSimulation(evaluations, shepherd, sheep, predator);
        }
        evolutionState.output.message("Evaluation finished.\nGeneration: " + evolutionState.generation + "\n" + results.toString());

        int sheepCounter = 0;
        double[] sheepScores = results.getSheepScore();
        for (int i = 0; i < individuals.length; i++) {
            if (updateFitness[i]) {
                Individual individual = individuals[i];
                CoESFitness fitness = (CoESFitness) individual.fitness;
                if (i < split) {
                    double score = results.getShepherdScore();
                    fitness.trials.add(score);
                    fitness.setFitness(evolutionState, score, false);
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
