package nl.vu.ai.aso.simulation;

import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * Created by acidghost on 24/11/15.
 */
public class Sheep extends AgentWithNetwork implements Steppable {

    public Sheep(double[] weights, int inputs) {
        super(weights, inputs, inputs > 3 ? 5 : 3);
    }

    public void step(SimState simState) {

    }

}
