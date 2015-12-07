package nl.vu.ai.aso.evolution;

import ec.EvolutionState;
import ec.simple.SimpleShortStatistics;
import ec.util.Parameter;
import nl.vu.ai.aso.shared.SheepStatus;

/**
 * Created by acidghost on 02/12/15.
 */
public class CoESStatistics extends SimpleShortStatistics {

    private int popSeparator;
    private int totalCorralledCases = 0;
    private int totalEscapedCases = 0;
    private int totalEvaluations = 0;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);

        Parameter popParam = new Parameter(HerdingProblem.POP_SEPARATOR);
        popSeparator = state.parameters.getInt(popParam, popParam);
    }

    @Override
    protected void printExtraPopStatisticsBefore(EvolutionState state) {
        super.printExtraPopStatisticsBefore(state);

        int corralledCases = 0;
        int escapedCases = 0;
        int evaluations = 0;

        for (int i = 0; i < state.population.subpops[popSeparator].individuals.length; i++) {
            // doing only sheep
            CoESFitness fitness = (CoESFitness) state.population.subpops[popSeparator].individuals[i].fitness;
            for (SheepStatus sheepStatus : fitness.sheepStatuses) {
                if (sheepStatus == SheepStatus.CORRALED) {
                    corralledCases++;
                } else if (sheepStatus == SheepStatus.ESCAPED) {
                    escapedCases++;
                }
            }
            evaluations += fitness.trials.size();
        }

        double corralledRatio = ((double) corralledCases) / evaluations;
        double escapedRatio = ((double) escapedCases) / evaluations;
        state.output.print(corralledCases + " " + escapedCases + " " + evaluations + " " + corralledRatio + " " + escapedRatio + " ", statisticslog);

        totalCorralledCases += corralledCases;
        totalEscapedCases += escapedCases;
        totalEvaluations += evaluations;
    }

    @Override
    public void finalStatistics(EvolutionState state, int result) {
        super.finalStatistics(state, result);

        double totalCorralledRatio = ((double) totalCorralledCases) / totalEvaluations;
        double totalEscapedRatio = ((double) totalEscapedCases) / totalEvaluations;
        state.output.println(totalCorralledCases + " " + totalEscapedCases + " " + totalEvaluations + " " + totalCorralledRatio + " " + totalEscapedRatio, statisticslog);
    }
}
