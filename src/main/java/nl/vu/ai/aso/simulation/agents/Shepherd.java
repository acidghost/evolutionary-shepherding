package nl.vu.ai.aso.simulation.agents;

import nl.vu.ai.aso.shared.ShepherdInputs;
import nl.vu.ai.aso.simulation.Herding;
import nl.vu.ai.aso.simulation.Yard;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

import java.awt.*;

public class Shepherd extends AgentWithNetwork {

    public static final double AGENT_RADIUS = 3;

    public Shepherd(double[] weights, int inputs) {
        this(0, 0, weights, inputs);
    }

    public Shepherd(double newX, double newY, double[] weights, int inputs) {
        super(newX, newY, 1, Color.blue, AGENT_RADIUS, weights, inputs);
    }

    public Shepherd(Double2D location, double[] weights, int inputs) {
        this(location.x, location.y, weights, inputs);
    }

    @Override
    protected ShepherdInputs getInputs(Herding herding) {
        Yard yard = herding.yard;
        Double2D sheepCenter = yard.getSheepCenter();
        double shepherd_r = getDistanceFromSheep(yard, this, sheepCenter);
        double shepherd_b = getBearingFromSheep(yard, this, sheepCenter, yard.corralPosition);

        Object[] neighbors = yard.detectNearestNeighbors(this);
        double otherShep_r = getDistanceFromSheep(yard, (Shepherd) neighbors[0], sheepCenter);
        double otherShep_b = getBearingFromSheep(yard, (Shepherd) neighbors[0], sheepCenter, yard.corralPosition);

        return new ShepherdInputs(shepherd_r, shepherd_b, otherShep_r, otherShep_b);
    }

}
