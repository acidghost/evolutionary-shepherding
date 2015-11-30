package nl.vu.ai.aso.simulation.agents;

import nl.vu.ai.aso.shared.ShepherdInputs;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

import java.awt.*;

public class Shepherd extends AgentWithNetwork {

    public Shepherd(double[] weights, int inputs) {
        this(0, 0, weights, inputs);
    }

    public Shepherd(double newX, double newY, double[] weights, int inputs) {
        super(newX, newY, 1, Color.blue, weights, inputs);
    }

    public Shepherd(Double2D location, double[] weights, int inputs) {
        this(location.x, location.y, weights, inputs);
    }

    @Override
    protected ShepherdInputs getInputs(Continuous2D yard, Double2D corralPosition) {
        Double2D sheepCenter = getSheepCenter(yard);
        double shepherd_r = getDistanceFromSheep(yard, this, sheepCenter);
        double shepherd_b = getBearingFromSheep(yard, this, sheepCenter, corralPosition);

        Object[] neighbors = detectNearestNeighbors(yard);
        double otherShep_r = getDistanceFromSheep(yard, (Shepherd) neighbors[0], sheepCenter);
        double otherShep_b = getBearingFromSheep(yard, (Shepherd) neighbors[0], sheepCenter, corralPosition);

        return new ShepherdInputs(shepherd_r, shepherd_b, otherShep_r, otherShep_b);
    }

}
