package nl.vu.ai.aso;

import ec.Evolve;
import nl.vu.ai.aso.evolution.HerdingProblem;

/**
 * Created by acidghost on 24/11/15.
 */
public class EvolutionaryShepherding {

    public static Thread getThread(String file) {
        return new Thread() {
            @Override
            public void run() {
                HerdingProblem.evaluationCounter = 0;
                Evolve.main(new String[] { "-file", file });
            }
        };
    }

    public static void main(String[] args) {
        String nSheep = "one";
        String nSheph = "one";

        if (args.length == 2) {
            nSheph = args[0];
            nSheep = args[1];
        }

        String filename = EvolutionaryShepherding.class.getClassLoader().getResource("ecj." + nSheph + ".shep." + nSheep + ".sheep.params").getPath();
        Evolve.main(new String[] { "-file", filename });
    }

}
