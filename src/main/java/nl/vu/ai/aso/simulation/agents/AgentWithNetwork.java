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
import java.util.ArrayList;

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

    protected Double2D getNewPosition(INetInputs inputs) {
        // System.out.println("Requesting new position to NN -> " + this.getClass().getSimpleName());
        double[] output = network.feedforward(inputs.toArray());
        System.out.println("NN output is " + output[0] + ", " + output[1]);

        //TODO: transform the out of the NN in x,y coordinates
        return new Double2D(output[0], output[1]); //this returns the actual x,y as new computed position
    }

    abstract protected INetInputs getInputs(Continuous2D yard);

    public MutableDouble2D getForces(Continuous2D yard) {
        MutableDouble2D sumForces = new MutableDouble2D();
        Double2D me = yard.getObjectLocation(this);

        INetInputs inputs = getInputs(yard);
        Double2D newTargetPosition = getNewPosition(inputs);
        sumForces.addIn(newTargetPosition);
        sumForces.addIn(me);

        return sumForces;
    }

    @Override
    public void step(SimState simState) {
        Herding herding = (Herding) simState;
        Continuous2D yard = herding.yard;

        MutableDouble2D force = getForces(yard);
        System.out.println("Force on " + this.getClass().getSimpleName() + " is " + force.toCoordinates());

        // acceleration = f/m
        acceleration.multiply(force, 1 / mass); // resets acceleration
        // v = v + a
        velocity.addIn(acceleration);
        capVelocity();
        // L = L + v
        newLoc.add(loc,velocity);  // resets newLoc

        // is new location valid?
        if(isValidMove(herding, newLoc)) {
            loc = newLoc;
            System.out.println("New agent (" + this.getClass().getSimpleName() + ") location @ " + loc.toCoordinates());
        }

        yard.setObjectLocation(this, new Double2D(loc));
    }

    // returns, in order, closest shepard, sheep and predator. Last one could be null
    protected Object[] detectNearestNeighbors(Continuous2D yard) {
        Bag allAgents = yard.getAllObjects();

        // split agents based on their types
        ArrayList<Shepherd> otherShepherds = new ArrayList<Shepherd>();
        ArrayList<Sheep> otherSheep = new ArrayList<Sheep>();
        Predator nearestPredator = null;

        for (int i = 0; i < allAgents.size(); i++) {
            Object retrivedObj = allAgents.get(i);
            if (retrivedObj instanceof Shepherd){
                otherShepherds.add((Shepherd) retrivedObj);
            } else if (retrivedObj instanceof Sheep) {
                otherSheep.add((Sheep) retrivedObj);
            } else {
                // it is (the only) predator
                nearestPredator = (Predator) retrivedObj;
            }
        }

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
}
