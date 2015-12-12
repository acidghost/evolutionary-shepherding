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
        // log("Loc: " + loc.toCoordinates());

        if (nnCounter == 0) {
            // log("Requesting new position to NN -> " + Arrays.toString(inputs.toArray()));
            double[] output = network.feedforward(inputs.toArray());
            // log("NN output is " + output[0] + ", " + output[1]);

            // output -> re-scale -> cartesian -> absolute centered

            // Sheep-centric, so radius is distance from sheep center
            // and angle is angle from sheep center respect to the corral
            double radius = Herding.WIDTH * output[0];
            double angle = (2 * Math.PI * output[1]) - Math.PI;
            // log("Radius - angle: " + radius + "\t" + angle);

            Double2D sheepCenter = yard.getSheepCenter();
            double sheepCorralAngle = Math.atan2(yard.corralPosition.y - sheepCenter.y, yard.corralPosition.x - sheepCenter.x);
            // log("Sheep-corral angle: " + sheepCorralAngle);

            double xTargetSheepCentric = radius * Math.cos(angle - sheepCorralAngle);
            double yTargetSheepCentric = radius * Math.sin(angle - sheepCorralAngle);
            // log("x - y: " + xTargetSheepCentric + " " + yTargetSheepCentric);

            double xTarget = sheepCenter.x + xTargetSheepCentric;
            double yTarget = sheepCenter.y + yTargetSheepCentric;
            Double2D cartesian = new Double2D(xTarget, yTarget);

            // log("New target: " + cartesian.toCoordinates() + "\n");
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
        final Double2D newPosition = getNewPosition(inputs, herding.yard);
        return new MutableDouble2D(newPosition.x - loc.x, newPosition.y - loc.y);
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
