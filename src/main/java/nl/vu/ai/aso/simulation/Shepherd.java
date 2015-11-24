package nl.vu.ai.aso.simulation;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

import java.util.List;

public class Shepherd extends AgentWithNetwork implements Steppable {

    public Shepherd(List<Double> weights, int inputs) {
        super(weights, inputs, inputs > 3 ? 5 : 3);
    }

    public void step(SimState simState) {
        Herding herding = (Herding) simState;
        Continuous2D yard = herding._yard;

        Double2D me = herding._yard.getObjectLocation(this);

        MutableDouble2D sumForces = new MutableDouble2D();

        //TODO: get the right inputs
        Double2D netOut = feedforward(new double[] {});
        //TODO: use netOut which is radius and bearing to move the agent

        sumForces.addIn(new Double2D(0.01, 0.01));

        sumForces.addIn(me);

        herding._yard.setObjectLocation(this, new Double2D(sumForces));
    }

}
