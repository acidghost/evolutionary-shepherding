package nl.vu.ai.aso.simulation.agents;

import nl.vu.ai.aso.shared.ShepherdInputs;
import nl.vu.ai.aso.simulation.Herding;
import nl.vu.ai.aso.simulation.Yard;
import sim.util.Double2D;

import java.awt.*;

public class Shepherd extends AgentWithNetwork {

    public static final double AGENT_RADIUS = 3;
    public int numberOfBumpsWithSheep;

    public Shepherd(double[] weights, int inputs) {
        this(0, 0, weights, inputs);
    }

    public Shepherd(double newX, double newY, double[] weights, int inputs) {
        super(newX, newY, 2, Color.blue, AGENT_RADIUS, weights, inputs);
        numberOfBumpsWithSheep = 0;
    }

    public Shepherd(Double2D location, double[] weights, int inputs) {
        this(location.x, location.y, weights, inputs);
    }

    @Override
    protected ShepherdInputs getInputs(Herding herding) {
        Yard yard = herding.yard;

        Object[] neighbors = yard.detectNearestNeighbors(this);
        // Double2D sheepCenter = yard.getSheepCenter();
        Double2D sheepCenter = yard.getObjectLocation(neighbors[1]);

        double shepherd_r = getDistanceFromSheep(yard, this, sheepCenter);
        double shepherd_b = getBearingFromSheep(yard, this, sheepCenter, yard.corralPosition);

        double otherShep_r = getDistanceFromSheep(yard, (Shepherd) neighbors[0], sheepCenter);
        double otherShep_b = getBearingFromSheep(yard, (Shepherd) neighbors[0], sheepCenter, yard.corralPosition);

        // log("R - B:\t" + shepherd_r + "\t" + shepherd_b + "\t" + otherShep_r + "\t" + otherShep_b);

        return new ShepherdInputs(shepherd_r, shepherd_b, otherShep_r, otherShep_b);
    }

}
