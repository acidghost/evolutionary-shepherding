package nl.vu.ai.aso.simulation;

import nl.vu.ai.aso.shared.NNinputs;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

import java.util.ArrayList;
import java.util.List;

public class Shepherd extends AgentWithNetwork implements Steppable {

    public Shepherd(double[] weights, int inputs) {
        super(weights, inputs, inputs > 3 ? 5 : 3);
    }

    public void step(SimState simState) {
        Herding herding = (Herding) simState;
        Continuous2D yard = herding._yard;

        Double2D me = herding._yard.getObjectLocation(this);

        MutableDouble2D sumForces = new MutableDouble2D();


        //TODO: get the right inputs
        NNinputs inputs = getSheepCentricInputs(detectNearestNeighbors(yard), yard);

        //TODO: use netOut which is radius and bearing to move the agent
        Double2D newTargetPosition = getNewPostion(inputs.toList());

        sumForces.addIn(newTargetPosition);

        sumForces.addIn(me);

        herding._yard.setObjectLocation(this, new Double2D(sumForces));
    }

    // takes the neighbors and returns the NN-sheep-centirc-inputs
    private NNinputs getSheepCentricInputs(Object[] neighbors, Continuous2D yard) {

        Shepherd shepherd = (Shepherd) neighbors[0];
        Sheep sheep = (Sheep) neighbors[1];
        Predator predator = (Predator) neighbors[2]; // might be null

        Double2D corralPosition = new Double2D(yard.getHeight() * 0.5, yard.getWidth());//TODO: find better corral positoning system


        NNinputs inputs = new NNinputs();
        return inputs;
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
