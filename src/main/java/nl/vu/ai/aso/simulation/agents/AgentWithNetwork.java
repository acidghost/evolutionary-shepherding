package nl.vu.ai.aso.simulation.agents;

import nl.vu.ai.aso.neuralnetwork.Mlp;
import nl.vu.ai.aso.shared.INetInputs;
import nl.vu.ai.aso.simulation.Herding;
import nl.vu.ai.aso.simulation.Yard;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

import java.awt.*;

/**
 * Created by acidghost on 24/11/15.
 */
public abstract class AgentWithNetwork extends Entity {

    private Mlp network;
    private int nnCounter;
    private static final int FREQUENCY = 0;
    private Double2D nnPosition;

    public AgentWithNetwork(double newX, double newY, double newRadius, Color c, double agentRadius, double[] weights, int inputs, int hidden) {
        super(newX, newY, newRadius, c, agentRadius);
        network = new Mlp(weights, inputs, hidden);
        nnCounter = FREQUENCY;
    }

    public AgentWithNetwork(double newX, double newY, double newRadius, Color c, double agentRadius, double[] weights, int inputs) {
        this(newX, newY, newRadius, c, agentRadius, weights, inputs, inputs > 2 ? 5 : 3);
    }

    protected Double2D getNewPosition(INetInputs inputs, Yard yard) {
        if (nnCounter == 0) {
            // log("Requesting new position to NN -> " + Arrays.toString(inputs.toArray()));
            double[] output = network.feedforward(inputs.toArray());
            // log("NN output is " + output[0] + ", " + output[1]);
            //TODO: check this makes sense
            // output -> re-scale -> cartesian -> absolute centered
            double radius = Herding.WIDTH * output[0];
            double angle = (2 * Math.PI * output[1]) - Math.PI;

            Double2D cartesian = new Double2D((radius * Math.cos(angle)), (radius * Math.sin(angle)));
            // cartesian = cartesian.add(new Double2D(loc));
            // log("New target: " + cartesian.toCoordinates() + "\t" + radius + " " + angle);
            nnPosition = cartesian;
            nnCounter = FREQUENCY;
        } else {
            nnCounter--;
        }

        return nnPosition; //this returns the actual x,y as new computed position
    }

    abstract protected INetInputs getInputs(Herding herding);

    @Override
    public MutableDouble2D getForces(Herding herding) {
        INetInputs inputs = getInputs(herding);
        return new MutableDouble2D(getNewPosition(inputs, herding.yard));
    }

    protected double getDistanceFromSheep(Continuous2D yard, AgentWithNetwork agent, Double2D sheepCenter) {
        Double2D agentPos = yard.getObjectLocation(agent);
        return sheepCenter.distance(agentPos);
    }

    protected double getBearingFromSheep(Continuous2D yard, AgentWithNetwork agent, Double2D sheepCenter, Double2D corralPosition) {
        Double2D agentPos = yard.getObjectLocation(agent);

        // double sheepCorralAngle = Math.atan2(corralPosition.y - sheepCenter.y, corralPosition.x - sheepCenter.x);
        // double agentCorralAngle = Math.atan2(corralPosition.y - agentPos.y, corralPosition.x - agentPos.x);

        // return Math.PI - (agentCorralAngle - sheepCorralAngle);

        double sheepCorralAngle = Math.atan2(corralPosition.y - sheepCenter.y, corralPosition.x - sheepCenter.x);
        double agentSheepAngle = Math.atan2(sheepCenter.y - agentPos.y, sheepCenter.x - agentPos.x);

        return sheepCorralAngle + agentSheepAngle;
    }
}
