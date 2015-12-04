package nl.vu.ai.aso.simulation.agents;

import nl.vu.ai.aso.neuralnetwork.Mlp;
import nl.vu.ai.aso.shared.INetInputs;
import nl.vu.ai.aso.simulation.Herding;
import nl.vu.ai.aso.simulation.Yard;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

import java.awt.*;

/**
 * Created by acidghost on 24/11/15.
 */
public abstract class AgentWithNetwork extends Entity {

    private Mlp network;

    public AgentWithNetwork(double newX, double newY, double newRadius, Color c, double agentRadius, double[] weights, int inputs, int hidden) {
        super(newX, newY, newRadius, c, agentRadius);
        network = new Mlp(weights, inputs, hidden);
    }

    public AgentWithNetwork(double newX, double newY, double newRadius, Color c, double agentRadius, double[] weights, int inputs) {
        this(newX, newY, newRadius, c, agentRadius, weights, inputs, inputs > 2 ? 5 : 3);
    }

    protected Double2D getNewPosition(INetInputs inputs, Yard yard) {
        // log("Requesting new position to NN -> " + Arrays.toString(inputs.toArray()));
        double[] output = network.feedforward(inputs.toArray());
        // log("NN output is " + output[0] + ", " + output[1]);
        //TODO: check this makes sense
        // output -> re-scale -> cartesian -> absolute centered
        double radius = 37 * output[0];
        double angle = (2 * Math.PI * output[1]) - Math.PI;

        Double2D cartesian = new Double2D((radius * Math.cos(angle)), (radius * Math.sin(angle)));

        Double2D newPosition = cartesian.add(yard.getSheepCenter());

        return newPosition; //this returns the actual x,y as new computed position
    }

    abstract protected INetInputs getInputs(Herding herding);

    @Override
    public MutableDouble2D getForces(Herding herding) {
        MutableDouble2D sumForces = new MutableDouble2D();
        sumForces.setTo(0.0, 0.0);

        INetInputs inputs = getInputs(herding);
        Double2D newTargetPosition = getNewPosition(inputs, herding.yard);
        sumForces.addIn(newTargetPosition);

        return sumForces;
    }

    protected double getDistanceFromSheep(Continuous2D yard, AgentWithNetwork agent, Double2D sheepCenter) {
        Double2D agentPos = yard.getObjectLocation(agent);
        return sheepCenter.distance(agentPos);
    }

    protected double getBearingFromSheep(Continuous2D yard, AgentWithNetwork agent, Double2D sheepCenter, Double2D corralPosition) {
        Double2D agentPos = yard.getObjectLocation(agent);

        double angle1 = Math.atan2((sheepCenter.y - corralPosition.y), (sheepCenter.x - corralPosition.x));
        double angle2 = Math.atan2((agentPos.y - corralPosition.y), (agentPos.x - corralPosition.x));

        return angle1 - angle2;
    }
}
