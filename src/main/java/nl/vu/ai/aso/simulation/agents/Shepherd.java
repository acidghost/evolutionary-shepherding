package nl.vu.ai.aso.simulation.agents;

import nl.vu.ai.aso.shared.ShepherdInputs;
import nl.vu.ai.aso.simulation.Herding;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Shepherd extends AgentWithNetwork {

    public Shepherd(double[] weights, int inputs) {
        this(0, 0, weights, inputs);
    }

    public Shepherd(double newX, double newY, double[] weights, int inputs) {
        super(newX, newY, 2, Color.blue, weights, inputs);
    }

    @Override
    public MutableDouble2D getForces(Continuous2D yard) {
        MutableDouble2D sumForces = new MutableDouble2D();
        Double2D me = yard.getObjectLocation(this);

        ShepherdInputs inputs = getInputs(yard);
        Double2D newTargetPosition = getNewPostion(inputs);
        sumForces.addIn(newTargetPosition);
        sumForces.addIn(me);

        return sumForces;
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

    private ShepherdInputs getInputs(Continuous2D yard) {
        Double2D sheepCenter = getSheepCenter(yard);
        double shepherd_r = getSheepDistance(yard, this, sheepCenter);
        double shepherd_b = getSheepBearing(yard, this, sheepCenter);

        Object[] neighbors = detectNearestNeighbors(yard);
        double otherShep_r = getSheepDistance(yard, (Shepherd) neighbors[0], sheepCenter);
        double otherShep_b = getSheepBearing(yard, (Shepherd) neighbors[0], sheepCenter);

        return new ShepherdInputs(shepherd_r, shepherd_b, otherShep_r, otherShep_b);
    }

    // returns, in order, closest shepard, sheep and predator. Last one could be null
    private Object[] detectNearestNeighbors(Continuous2D yard) {
        Bag allAgents = yard.getAllObjects();

        // split agents based on their types
        ArrayList<Shepherd> othersSheperds = new ArrayList<Shepherd>();
        ArrayList<Sheep> othersSheeps = new ArrayList<Sheep>();
        Predator nearestPredator = null;

        for (int i = 0; i < allAgents.size(); i++) {
            Object retrivedObj = allAgents.get(i);
            if (retrivedObj instanceof Shepherd){
                othersSheperds.add((Shepherd) retrivedObj);
            } else if (retrivedObj instanceof Sheep) {
                othersSheeps.add((Sheep) retrivedObj);
            } else {
                // it is (the only) predator
                nearestPredator = (Predator) retrivedObj;
            }
        }

        // get the nearest shepard
        Shepherd nearestShepard = othersSheperds.get(0);
        double distanceNearestShepard = yard.getObjectLocationAsDouble2D(this).distance(yard.getObjectLocationAsDouble2D(nearestShepard));

        for (int i = 1; i < othersSheperds.size(); i++){
            //check if the distance is bigger than nearestSheperds
            double currentShepardDistance = yard.getObjectLocationAsDouble2D(this).distance(yard.getObjectLocationAsDouble2D(othersSheperds.get(i)));
            if (distanceNearestShepard > currentShepardDistance) {
                nearestShepard = othersSheperds.get(i);
                distanceNearestShepard = currentShepardDistance;
            }
        }

        // get the nearest sheep
        Sheep nearestSheep = othersSheeps.get(0);
        double distanceNearestSheep= yard.getObjectLocationAsDouble2D(this).distance(yard.getObjectLocationAsDouble2D(nearestSheep));

        for (int i = 1; i < othersSheeps.size(); i++){
            //check if the distance is bigger than nearestSheep
            double currentSheepDistance = yard.getObjectLocationAsDouble2D(this).distance(yard.getObjectLocationAsDouble2D(othersSheeps.get(i)));
            if (distanceNearestSheep > currentSheepDistance) {
                nearestSheep = othersSheeps.get(i);
                distanceNearestSheep = currentSheepDistance;
            }
        }

        return new Object[] {nearestShepard, nearestSheep, nearestPredator}; // last one could be null
    }
}
