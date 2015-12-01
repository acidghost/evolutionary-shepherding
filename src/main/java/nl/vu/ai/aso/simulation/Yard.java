package nl.vu.ai.aso.simulation;

import nl.vu.ai.aso.simulation.agents.Predator;
import nl.vu.ai.aso.simulation.agents.Sheep;
import nl.vu.ai.aso.simulation.agents.Shepherd;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;

import java.util.ArrayList;

/**
 * Created by acidghost on 01/12/15.
 */
public class Yard extends Continuous2D {

    public Double2D corralPosition = new Double2D(getWidth(), getHeight() * 0.5); // left centered corral

    public Yard(double discretization, double width, double height) {
        super(discretization, width, height);
    }

    // for fitness function
    public double allSheepDistance() {
        double totalDistance = 0.0;
        Object[] agents = sortAgents();
        ArrayList<Sheep> allSheep = (ArrayList) agents[1];

        for (Sheep sheep : allSheep){
            Double2D sheepPosition = this.getObjectLocation(sheep);
            double individualDistance = corralPosition.distance(sheepPosition);
            totalDistance += individualDistance;
        }
        return totalDistance; //TODO: check it makes sense
    }

    // returns, in order, closest shepard, sheep and predator. Last one could be null
    public Object[] detectNearestNeighbors(Object agent) {
        // split agents based on their types
        Object[] agents = sortAgents();
        ArrayList<Shepherd> otherShepherds = (ArrayList) agents[0];
        ArrayList<Sheep> otherSheep = (ArrayList) agents[1];
        Predator nearestPredator = (Predator) agents[2];

        // get the nearest shepard
        Shepherd nearestShepard = otherShepherds.get(0);
        double distanceNearestShepard = getObjectLocationAsDouble2D(agent).distance(getObjectLocationAsDouble2D(nearestShepard));

        for (int i = 1; i < otherShepherds.size(); i++){
            //check if the distance is bigger than nearestShepherds
            double currentShepardDistance = getObjectLocationAsDouble2D(agent).distance(getObjectLocationAsDouble2D(otherShepherds.get(i)));
            if (distanceNearestShepard > currentShepardDistance) {
                nearestShepard = otherShepherds.get(i);
                distanceNearestShepard = currentShepardDistance;
            }
        }

        // get the nearest sheep
        Sheep nearestSheep = otherSheep.get(0);
        double distanceNearestSheep= getObjectLocationAsDouble2D(agent).distance(getObjectLocationAsDouble2D(nearestSheep));

        for (int i = 1; i < otherSheep.size(); i++){
            //check if the distance is bigger than nearestSheep
            double currentSheepDistance = getObjectLocationAsDouble2D(agent).distance(getObjectLocationAsDouble2D(otherSheep.get(i)));
            if (distanceNearestSheep > currentSheepDistance) {
                nearestSheep = otherSheep.get(i);
                distanceNearestSheep = currentSheepDistance;
            }
        }

        return new Object[] {nearestShepard, nearestSheep, nearestPredator}; // last one could be null
    }

    // return agents by type, in order shepherds, sheep and predator
    public Object[] sortAgents(){
        Bag allAgents = this.getAllObjects();

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

    public Double2D getSheepCenter() {
        Object[] agents = sortAgents();
        ArrayList<Sheep> allSheep = (ArrayList) agents[1];

        Double2D center = new Double2D(0, 0);
        for (Sheep sheep : allSheep) {
            center.add(this.getObjectLocation(sheep));
        }
        return new Double2D(center.x / allSheep.size(), center.y / allSheep.size());
    }

}
