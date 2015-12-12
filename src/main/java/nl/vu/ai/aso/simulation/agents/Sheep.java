package nl.vu.ai.aso.simulation.agents;

import nl.vu.ai.aso.shared.SheepInputs;
import nl.vu.ai.aso.simulation.Herding;
import nl.vu.ai.aso.simulation.Yard;
import sim.engine.SimState;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

import java.awt.*;

/**
 * Created by acidghost on 24/11/15.
 */
public class Sheep extends AgentWithNetwork {

    public static final double AGENT_RADIUS = 2;
    public double travelledDistance;

    public Sheep(double[] weights, int inputs) {
        this(0, 0, weights, inputs);
    }

    public Sheep(double newX, double newY, double[] weights, int inputs) {
        super(newX, newY, 2, Color.lightGray, AGENT_RADIUS, weights, inputs);
        travelledDistance = 0;
        cap = 1.0;
    }

    public Sheep(Double2D location, double[] weights, int inputs) {
        this(location.x, location.y, weights, inputs);
    }

    @Override
    protected SheepInputs getInputs(Herding herding) {
        Yard yard = herding.yard;
        Double2D sheepCenter = yard.getSheepCenter();

        Object[] neighbors = yard.detectNearestNeighbors(this);
        double closestShep_r = getDistanceFromSheep(yard, (Shepherd) neighbors[0], sheepCenter);
        double closestShep_b = getBearingFromSheep(yard, (Shepherd) neighbors[0], sheepCenter, yard.corralPosition);
        // log("R - B:\t" + closestShep_r + "\t" + closestShep_b);

        if (herding.sheep.size() > 1) {
            double sheep_r = getDistanceFromSheep(yard, this, sheepCenter);
            double sheep_b = getBearingFromSheep(yard, this, sheepCenter, yard.corralPosition);
            return new SheepInputs(closestShep_r, closestShep_b, sheep_r, sheep_b);
        } else {
            return new SheepInputs(closestShep_r, closestShep_b, null, null);
        }
    }

    @Override
    public boolean isValidMove(Herding herding, MutableDouble2D newLoc) {
        // check if shepherd bumped into a sheep
        Bag inRadius = herding.yard.getNeighborsExactlyWithinDistance(new Double2D(loc), agentRadius + Shepherd.AGENT_RADIUS);

        for (Object agent : inRadius) {
            if (agent instanceof Shepherd) {
                // log("Bump!");
                ((Shepherd) agent).numberOfBumpsWithSheep++;
            }
        }

        return super.isValidMove(herding, newLoc);
    }

    @Override
    public void step(SimState simState) {
        super.step(simState);
        Herding herding = (Herding) simState;
        travelledDistance += herding.yard.corralPosition.x - loc.x;
        // log("Traveled distance: " + travelledDistance);
    }
}
