package nl.vu.ai.aso;

import ec.Evolve;

/**
 * Created by acidghost on 24/11/15.
 */
public class EvolutionaryShepherding {

    public static void main(String[] args) {
        String filename = EvolutionaryShepherding.class.getClassLoader().getResource("ecj.three.shep.three.sheep.params").getPath();
        Evolve.main(new String[] { "-file", filename });
    }

}
