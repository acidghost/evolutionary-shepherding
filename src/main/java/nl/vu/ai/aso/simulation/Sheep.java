package nl.vu.ai.aso.simulation;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

import java.util.List;

/**
 * Created by acidghost on 24/11/15.
 */
public class Sheep extends AgentWithNetwork implements Steppable {

    public Sheep(double[] weights, int inputs) {
        super(weights, inputs, inputs > 3 ? 5 : 3);
    }

    public void step(SimState simState) {
        //TODO: implement
    }

}
