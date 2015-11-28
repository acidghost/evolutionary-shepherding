package nl.vu.ai.aso.simulation.agents;

import nl.vu.ai.aso.shared.ShepherdInputs;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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

    private Double2D getSheepCenter(Continuous2D yard) {
        List<Sheep> allSheep = new ArrayList<>();
        for (Object agent : yard.getAllObjects()) {
            if (agent instanceof Sheep) {
                allSheep.add((Sheep) agent);
            }
        }

        Double2D center = new Double2D(0, 0);
        for (Sheep sheep : allSheep) {
            center.add(yard.getObjectLocation(sheep));
        }
        return new Double2D(center.x / allSheep.size(), center.y / allSheep.size());
    }

    private double getSheepDistance(Continuous2D yard, Shepherd shepherd, Double2D sheepCenter) {
        Double2D shepherdPos = yard.getObjectLocation(shepherd);
        return sheepCenter.distance(shepherdPos);
    }

    private double getSheepBearing(Continuous2D yard, Shepherd shepherd, Double2D sheepCenter) {
        Double2D shepherdPos = yard.getObjectLocation(shepherd);
        // TODO: implement me! & find better corral position
        Double2D corralPosition = new Double2D(yard.getHeight() * 0.5, yard.getWidth());
        return 0;
    }

    @Override
    protected ShepherdInputs getInputs(Continuous2D yard) {
        Double2D sheepCenter = getSheepCenter(yard);
        double shepherd_r = getSheepDistance(yard, this, sheepCenter);
        double shepherd_b = getSheepBearing(yard, this, sheepCenter);

        Object[] neighbors = detectNearestNeighbors(yard);
        double otherShep_r = getSheepDistance(yard, (Shepherd) neighbors[0], sheepCenter);
        double otherShep_b = getSheepBearing(yard, (Shepherd) neighbors[0], sheepCenter);

        return new ShepherdInputs(shepherd_r, shepherd_b, otherShep_r, otherShep_b);
    }

}
