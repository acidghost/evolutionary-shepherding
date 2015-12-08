package nl.vu.ai.aso.evolution;

import com.google.common.io.Files;
import ec.*;
import ec.coevolve.GroupedProblemForm;
import ec.simple.SimpleFitness;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;
import nl.vu.ai.aso.EvolutionaryShepherding;
import nl.vu.ai.aso.shared.EvaluationResults;
import nl.vu.ai.aso.shared.Replay;
import nl.vu.ai.aso.simulation.Herding;
import nl.vu.ai.aso.simulation.HerdingGUI;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by acidghost on 05/12/15.
 */
public abstract class HerdingProblem extends Problem implements GroupedProblemForm {

    public static final String POP_SEPARATOR = "pop.separator";
    public static final String EVAL_PREDATOR = "eval.predator";
    public static final String EVAL_STEPS = "eval.evaluations";
    public static final String EVO_FILE = "evo.file";
    public static final String EVO_RUN = "evo.run";
    public static final String STAT_FILE = "stat.file";

    public static final double PROPORTION_NOT_IN_GUI = 1;

    public static int evaluationCounter = 0;

    @Override
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

    protected EvaluationResults getEvaluationResults(EvolutionState evolutionState, boolean predator, int evaluations, List<double[]> shepherd, ArrayList<double[]> sheep) {
        EvaluationResults results;
        if (evolutionState.generation > evolutionState.numGenerations * PROPORTION_NOT_IN_GUI) {
            results = HerdingGUI.runSimulation(evaluations, 30, shepherd, sheep, predator);
        } else {
            results = Herding.runSimulation(evaluations, shepherd, sheep, predator);
        }
        evolutionState.output.message("Evaluation finished.\nGeneration: " + evolutionState.generation + "\n" + results.toString());
        return results;
    }

    @Override
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

        serializeIndividuals(evolutionState);

        evolutionState.output.message("\n");
    }

    protected void serializeIndividuals(EvolutionState evolutionState) {
        int split = evolutionState.parameters.getInt(new Parameter(POP_SEPARATOR), new Parameter(POP_SEPARATOR + ".default"));
        int evaluations = evolutionState.parameters.getInt(new Parameter(EVAL_STEPS), new Parameter(EVAL_STEPS));

        List<DoubleVectorIndividual> bestOfGeneration = new ArrayList<>(evolutionState.population.subpops.length);
        for (int i = 0; i < evolutionState.population.subpops.length; i++) {
            Subpopulation subpop = evolutionState.population.subpops[i];
            Individual bestIndividual = subpop.individuals[0];
            for (int j = 1; j < subpop.individuals.length; j++) {
                Individual individual = subpop.individuals[j];
                if (individual.evaluated && individual.fitness.betterThan(bestIndividual.fitness)) {
                    bestIndividual = individual;
                }
            }
            bestOfGeneration.add((DoubleVectorIndividual) bestIndividual);
        }

        for (int i = 0; i < bestOfGeneration.size(); i++) {
            Individual individual = bestOfGeneration.get(i);
            evolutionState.output.message("Subpop " + i + ": " + individual.fitness.fitnessToStringForHumans());
        }

        Replay replay = getReplay(evolutionState, bestOfGeneration, split, evaluations);

        try {
            final String[] evoFileSplitted = ((String) evolutionState.parameters.get(EVO_FILE)).split("/");
            final String evoFile = evoFileSplitted[evoFileSplitted.length - 1].split(".params")[0];
            final String runNumber = (String) evolutionState.parameters.get(EVO_RUN);
            final String filename = EvolutionaryShepherding.SERIALIZED_DIR + "/" + evoFile + "-" + runNumber + "/best." + evolutionState.generation + ".ser";
            Files.createParentDirs(new File(filename));

            OutputStream file = new FileOutputStream(filename);
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);
            output.writeObject(replay);
            output.close();
            buffer.close();
            file.close();
        } catch (IOException ioe) {
            evolutionState.output.fatal("Error serializing best of generation!");
            ioe.printStackTrace();
        }
    }

    public abstract Replay getReplay(EvolutionState evolutionState, List<DoubleVectorIndividual> bestOfGeneration, int split, int totalSteps);

}
