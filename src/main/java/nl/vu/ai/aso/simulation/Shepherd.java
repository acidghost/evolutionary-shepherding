package nl.vu.ai.aso.simulation;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

public class Shepherd extends AgentWithNetwork implements Steppable {

    public Shepherd(double[] weights, int inputs) {
        super(weights, inputs, inputs > 3 ? 5 : 3);
    }

    public void step(SimState simState) {
        Herding herding = (Herding) simState;
        Continuous2D yard = herding.yard;

        Double2D me = herding.yard.getObjectLocation(this);

        MutableDouble2D sumForces = new MutableDouble2D();

        //TODO: get the right inputs
        Double2D netOut = feedforward(new Double[] {});
        //TODO: use netOut which is radius and bearing to move the agent

        sumForces.addIn(new Double2D(0.01, 0.01));

        sumForces.addIn(me);

        herding.yard.setObjectLocation(this, new Double2D(sumForces));
    }

}
