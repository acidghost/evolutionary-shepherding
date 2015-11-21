package nl.vu.ai.aso;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

public class Shephard implements Steppable {

    public void step(SimState simState) {
        Herding herding = (Herding) simState;
        Continuous2D yard = herding.yard;

        Double2D me = herding.yard.getObjectLocation(this);

        MutableDouble2D sumForces = new MutableDouble2D();

        sumForces.addIn(new Double2D(0.01, 0.01));

        sumForces.addIn(me);

        herding.yard.setObjectLocation(this, new Double2D(sumForces));
    }
}
