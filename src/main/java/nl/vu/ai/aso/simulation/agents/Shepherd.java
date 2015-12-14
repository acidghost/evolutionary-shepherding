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

        // closest sheep coordinates
        Double2D closestSheep = yard.getObjectLocation(neighbors[1]);
        double closestSheep_r = getDistance(yard, this, closestSheep);
        double closestSheep_b = getBearing(yard, this, closestSheep, yard.corralPosition);

        if (herding.shepherds.size() > 1) {
            // closest shepherd coordinates
            Double2D closestShepherd = yard.getObjectLocation(neighbors[0]);
            double closestShep_r = getDistance(yard, this, closestShepherd);
            double closestShep_b = getBearing(yard, this, closestShepherd, yard.corralPosition);
            // log("R - B:\t" + shepherd_r + "\t" + shepherd_b + "\t" + closestShep_r + "\t" + closestShep_b);
            return new ShepherdInputs(closestSheep_r, closestSheep_b, closestShep_r, closestShep_b);
        } else {
            return new ShepherdInputs(closestSheep_r, closestSheep_b, null, null);
        }

    }

}
