package nl.vu.ai.aso.simulation.agents;

import sim.engine.SimState;
import sim.engine.Steppable;

import java.awt.*;

/**
 * Created by acidghost on 24/11/15.
 */
public class Sheep extends AgentWithNetwork implements Steppable {

    public Sheep(double[] weights, int inputs) {
        this(0, 0, weights, inputs);
    }

    public Sheep(double newX, double newY, double[] weights, int inputs) {
        super(newX, newY, 10, Color.white, weights, inputs);
    }

    public void step(SimState simState) {
        //TODO: implement
    }

}
