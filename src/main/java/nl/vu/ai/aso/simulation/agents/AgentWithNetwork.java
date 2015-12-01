package nl.vu.ai.aso.simulation.agents;

import nl.vu.ai.aso.neuralnetwork.Mlp;
import nl.vu.ai.aso.shared.INetInputs;
import nl.vu.ai.aso.simulation.Herding;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

import java.awt.*;
import java.util.*;
import java.lang.*;

/**
 * Created by acidghost on 24/11/15.
 */
public abstract class AgentWithNetwork extends Entity implements Steppable {

    private Mlp network;

    public AgentWithNetwork(double newX, double newY, double newRadius, Color c, double[] weights, int inputs, int hidden) {
        super(newX, newY, newRadius, c);
        network = new Mlp(weights, inputs, hidden);
    }

    public AgentWithNetwork(double newX, double newY, double newRadius, Color c, double[] weights, int inputs) {
        this(newX, newY, newRadius, c, weights, inputs, inputs > 2 ? 5 : 3);
    }

    protected Double2D getNewPosition(INetInputs inputs, Continuous2D yard) {
        // System.out.println("Requesting new position to NN -> " + this.getClass().getSimpleName());
        double[] output = network.feedforward(inputs.toArray());
        // System.out.println("NN output is " + output[0] + ", " + output[1]);
        //TODO: check this makes sense
        // output -> re-scale -> cartesian -> absolute centered
        double radius = 37 * output[0];
        double angle = (2 * Math.PI * output[1]) - Math.PI;

        Double2D cartesian = new Double2D((radius * Math.cos(angle)), (radius * Math.sin(angle)));

        Double2D newPosition = cartesian.add(getSheepCenter(yard));

        return newPosition; //this returns the actual x,y as new computed position
    }

    abstract protected INetInputs getInputs(Continuous2D yard, Double2D corralPosition);

    public MutableDouble2D getForces(Continuous2D yard, Double2D corralPosition) {
        MutableDouble2D sumForces = new MutableDouble2D();
        sumForces.setTo(0.0, 0.0);

        INetInputs inputs = getInputs(yard, corralPosition);
        Double2D newTargetPosition = getNewPosition(inputs, yard);
        sumForces.addIn(newTargetPosition);

        return sumForces;
    }

    @Override
    public void step(SimState simState) {
        Herding herding = (Herding) simState;
        Continuous2D yard = herding.yard;
        Double2D corralPosition = herding.corralPosition;

        MutableDouble2D force = getForces(yard, corralPosition);
        // System.out.println("Force on " + this.getClass().getSimpleName() + " is " + force.toCoordinates());

        // acceleration = f/m
        acceleration.multiply(force, 1 / mass); // resets acceleration
        // System.out.println("Acc on " + this.getClass().getSimpleName() + " is " + acceleration.toCoordinates());
        // v = v + a
        velocity.addIn(acceleration);
        capVelocity();
        // System.out.println("Vel on " + this.getClass().getSimpleName() + " is " + velocity.toCoordinates());
        // L = L + v
        newLoc.add(loc, velocity);  // resets newLoc

        // is new location valid?
        if (isValidMove(herding, newLoc)) {
            loc = newLoc;
            System.out.println("New agent (" + this.getClass().getSimpleName() + ") location @ " + loc.toCoordinates());
        } else {
            System.out.println(this.getClass().getSimpleName() + " hit something @ " + newLoc.toCoordinates());
        }

        yard.setObjectLocation(this, new Double2D(loc));
    }

    // return agents by type, in order shepherds, sheep and predator
    protected Object[] sortAgents(Continuous2D yard){
        Bag allAgents = yard.getAllObjects();

        // split agents based on their types
        ArrayList<Shepherd> shepherds = new ArrayList<Shepherd>();
        ArrayList<Sheep> sheep = new ArrayList<Sheep>();
        Predator predator = null;

        for (int i = 0; i < allAgents.size(); i++) {
            Object retrivedObj = allAgents.get(i);
            if (retrivedObj instanceof Shepherd){
                shepherds.add((Shepherd) retrivedObj);
            } else if (retrivedObj instanceof Sheep) {
                sheep.add((Sheep) retrivedObj);
            } else {
                // it is (the only) predator
                predator = (Predator) retrivedObj;
            }
        }
        return new Object[] {shepherds, sheep, predator}; // last one could be null
    }

    // returns, in order, closest shepard, sheep and predator. Last one could be null
    protected Object[] detectNearestNeighbors(Continuous2D yard) {
        // split agents based on their types
        Object[] agents = sortAgents(yard);
        ArrayList<Shepherd> otherShepherds = (ArrayList) agents[0];
        ArrayList<Sheep> otherSheep = (ArrayList) agents[1];
        Predator nearestPredator = (Predator) agents[2];

        // get the nearest shepard
        Shepherd nearestShepard = otherShepherds.get(0);
        double distanceNearestShepard = yard.getObjectLocationAsDouble2D(this).distance(yard.getObjectLocationAsDouble2D(nearestShepard));

        for (int i = 1; i < otherShepherds.size(); i++){
            //check if the distance is bigger than nearestShepherds
            double currentShepardDistance = yard.getObjectLocationAsDouble2D(this).distance(yard.getObjectLocationAsDouble2D(otherShepherds.get(i)));
            if (distanceNearestShepard > currentShepardDistance) {
                nearestShepard = otherShepherds.get(i);
                distanceNearestShepard = currentShepardDistance;
            }
        }

        // get the nearest sheep
        Sheep nearestSheep = otherSheep.get(0);
        double distanceNearestSheep= yard.getObjectLocationAsDouble2D(this).distance(yard.getObjectLocationAsDouble2D(nearestSheep));

        for (int i = 1; i < otherSheep.size(); i++){
            //check if the distance is bigger than nearestSheep
            double currentSheepDistance = yard.getObjectLocationAsDouble2D(this).distance(yard.getObjectLocationAsDouble2D(otherSheep.get(i)));
            if (distanceNearestSheep > currentSheepDistance) {
                nearestSheep = otherSheep.get(i);
                distanceNearestSheep = currentSheepDistance;
            }
        }

        return new Object[] {nearestShepard, nearestSheep, nearestPredator}; // last one could be null
    }

    protected Double2D getSheepCenter(Continuous2D yard) {
        Object[] agents = sortAgents(yard);
        ArrayList<Sheep> allSheep = (ArrayList) agents[1];

        Double2D center = new Double2D(0, 0);
        for (Sheep sheep : allSheep) {
            center.add(yard.getObjectLocation(sheep));
        }
        return new Double2D(center.x / allSheep.size(), center.y / allSheep.size());
    }

    protected double getDistanceFromSheep(Continuous2D yard, AgentWithNetwork agent, Double2D sheepCenter) {
        Double2D agentPos = yard.getObjectLocation(agent);
        return sheepCenter.distance(agentPos);
    }

    protected double getBearingFromSheep(Continuous2D yard, AgentWithNetwork agent, Double2D sheepCenter, Double2D corralPosition) {
        Double2D agentPos = yard.getObjectLocation(agent);

        double angle1 = Math.atan2((sheepCenter.y - corralPosition.y), (sheepCenter.x - corralPosition.x));
        double angle2 = Math.atan2((agentPos.y - corralPosition.y), (agentPos.x - corralPosition.x));

        return angle1 - angle2;
    }
}
