package nl.vu.ai.aso.simulation.agents;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.MutableDouble2D;

import java.awt.*;

/**
 * Created by acidghost on 24/11/15.
 */
public class Sheep extends AgentWithNetwork {

    public Sheep(double[] weights, int inputs) {
        this(0, 0, weights, inputs);
    }

    public Sheep(double newX, double newY, double[] weights, int inputs) {
        super(newX, newY, 2, Color.lightGray, weights, inputs);
    }

    public void step(SimState simState) {
        //TODO: implement
    }

    @Override
    public MutableDouble2D getForces(Continuous2D yard) {
        // TODO: implement me!
        return null;
    }
}
