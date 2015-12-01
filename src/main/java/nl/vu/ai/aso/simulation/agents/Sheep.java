package nl.vu.ai.aso.simulation.agents;

import nl.vu.ai.aso.shared.SheepInputs;
import nl.vu.ai.aso.simulation.Yard;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

import java.awt.*;

/**
 * Created by acidghost on 24/11/15.
 */
public class Sheep extends AgentWithNetwork {

    public static final double AGENT_RADIUS = 5;

    public Sheep(double[] weights, int inputs) {
        this(0, 0, weights, inputs);
    }

    public Sheep(double newX, double newY, double[] weights, int inputs) {
        super(newX, newY, 1, Color.lightGray, AGENT_RADIUS, weights, inputs);
    }

    public Sheep(Double2D location, double[] weights, int inputs) {
        this(location.x, location.y, weights, inputs);
    }

    @Override
    protected SheepInputs getInputs(Yard yard, Double2D corralPosition) {
        Double2D sheepCenter = yard.getSheepCenter();
        double sheep_r = getDistanceFromSheep(yard, this, sheepCenter);
        double sheep_b = getBearingFromSheep(yard, this, sheepCenter, corralPosition);

        Object[] neighbors = yard.detectNearestNeighbors(this);
        double closestShep_r = getDistanceFromSheep(yard, (Shepherd) neighbors[0], sheepCenter);
        double closestShep_b = getBearingFromSheep(yard, (Shepherd) neighbors[0], sheepCenter, corralPosition);

        // TODO: check number of inputs
        return new SheepInputs(sheep_r, sheep_b, null, null);
    }
}
