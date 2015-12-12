package nl.vu.ai.aso.simulation;

import com.google.common.collect.Lists;
import nl.vu.ai.aso.shared.SheepStatus;
import nl.vu.ai.aso.simulation.agents.Entity;
import nl.vu.ai.aso.simulation.agents.Predator;
import nl.vu.ai.aso.simulation.agents.Sheep;
import nl.vu.ai.aso.simulation.agents.Shepherd;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;

import java.util.ArrayList;
import java.util.List;

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
        return corralPosition.x - getSheepCenter().x;
    }

    public double getSheepRatio(List<Sheep> sheepAgents) {
        Double2D sheepCenter = getSheepCenter();
        double ratio = 0.0;

        for (Sheep sheep : sheepAgents){
            double currentSheepDistance = getObjectLocationAsDouble2D(sheep).distance(sheepCenter);
            if (ratio < currentSheepDistance) {
                ratio = currentSheepDistance;
            }
        }
        return ratio;
    }

    // returns, in order, closest shepard, sheep and predator. Last one could be null
    public Object[] detectNearestNeighbors(Object agent) {
        // split agents based on their types
        List<Shepherd> otherShepherds = Lists.newArrayList();
        List<Sheep> otherSheep = Lists.newArrayList();
        Predator nearestPredator = null;

        Bag allAgents = getAllObjects();
        for (Object obj : allAgents) {
            if (!agent.equals(obj)) {
                if (obj instanceof Shepherd) {
                    otherShepherds.add((Shepherd) obj);
                } else if (obj instanceof Sheep) {
                    otherSheep.add((Sheep) obj);
                } else if (obj instanceof Predator) {
                    nearestPredator = (Predator) obj;
                }
            }
        }

        Shepherd nearestShepard;
        if (otherShepherds.size() > 0) {
            // get the nearest shepard
            nearestShepard = otherShepherds.get(0);
            double distanceNearestShepard = getObjectLocationAsDouble2D(agent).distance(getObjectLocationAsDouble2D(nearestShepard));

            for (int i = 1; i < otherShepherds.size(); i++){
                //check if the distance is bigger than nearestShepherds
                double currentShepardDistance = getObjectLocationAsDouble2D(agent).distance(getObjectLocationAsDouble2D(otherShepherds.get(i)));
                if (distanceNearestShepard > currentShepardDistance) {
                    nearestShepard = otherShepherds.get(i);
                    distanceNearestShepard = currentShepardDistance;
                }
            }
        } else {
            nearestShepard = (Shepherd) agent;
        }

        Sheep nearestSheep;
        if (otherSheep.size() > 0) {
            // get the nearest sheep
            nearestSheep = otherSheep.get(0);
            double distanceNearestSheep= getObjectLocationAsDouble2D(agent).distance(getObjectLocationAsDouble2D(nearestSheep));

            for (int i = 1; i < otherSheep.size(); i++){
                //check if the distance is bigger than nearestSheep
                double currentSheepDistance = getObjectLocationAsDouble2D(agent).distance(getObjectLocationAsDouble2D(otherSheep.get(i)));
                if (distanceNearestSheep > currentSheepDistance) {
                    nearestSheep = otherSheep.get(i);
                    distanceNearestSheep = currentSheepDistance;
                }
            }
        } else {
            nearestSheep = (Sheep) agent;
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
            center = center.add(this.getObjectLocation(sheep));
        }
        return new Double2D(center.x / allSheep.size(), center.y / allSheep.size());
    }

    public SheepStatus getSheepStatus(Sheep sheep) {
        if (sheep.loc.x >= getWidth() - Herding.RESOLUTION) {
            return SheepStatus.CORRALED;
        } else if (sheep.loc.x <= Herding.RESOLUTION) {
            return SheepStatus.ESCAPED;
        } else {
            return SheepStatus.NORMAL;
        }
    }

    public boolean isInsideYard(Entity agent) {
        if (agent.loc.x > getWidth() - agent.agentRadius) {
            return false;
        } else if (agent.loc.x < agent.agentRadius) {
            return false;
        } else if (agent.loc.y > getHeight() - agent.agentRadius) {
            return false;
        } else if (agent.loc.y < agent.agentRadius) {
            return false;
        }

        return true;
    }

}
