package nl.vu.ai.aso.simulation.agents;

import nl.vu.ai.aso.simulation.Herding;
import sim.field.continuous.Continuous2D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by acidghost on 27/11/15.
 */
public abstract class Entity extends OvalPortrayal2D {

    private static final long serialVersionUID = 1;

    public MutableDouble2D loc, velocity, bump;
    public MutableDouble2D force = new MutableDouble2D();
    public MutableDouble2D acceleration = new MutableDouble2D();
    public MutableDouble2D newLoc = new MutableDouble2D();
    public MutableDouble2D sumVector = new MutableDouble2D(0, 0);

    public double speed, radius;
    public double cap;
    public double mass;
    public double agentRadius;

    // Accessors for inspector
    public double getX() {
        return loc.x;
    }

    public void setX(double newX) {
        loc.x = newX;
    }

    public double getY() {
        return loc.y;
    }

    public void setY(double newY) {
        loc.y = newY;
    }

    public double getVelocityX() {
        return velocity.x;
    }

    public void setVelocityX(double newX) {
        velocity.x = newX;
    }

    public double getVelocityY() {
        return velocity.y;
    }

    public void setVelocityY(double newY) {
        velocity.y = newY;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double newSpeed) {
        speed = newSpeed;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double newRadius) {
        radius = newRadius;
        scale = 2 * radius;  // so our ovalportrayal knows how to draw/hit us right
    }

    public double getMass() {
        return mass;
    }

    public void setMass(double newMass) {
        mass = newMass;
    }

    // Constructor
    public Entity(double newX, double newY, double newRadius, Color c, double newAgentRadius) {
        super(c, newRadius * 2);  // scale is twice the radius

        loc = new MutableDouble2D(newX, newY);
        velocity = new MutableDouble2D(0, 0);
        bump = new MutableDouble2D(0, 0);
        radius = newRadius;

        agentRadius = newAgentRadius;

        mass = 1.0;
        cap = 1.0;

        speed = 0.4;
    }

    public Entity(MutableDouble2D loc, double newRadius, Color c, double agentRadius) {
        this(loc.x, loc.y, newRadius, c, agentRadius);
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

    protected Double2D getSheepCenter(Continuous2D yard) {
        Object[] agents = sortAgents(yard);
        ArrayList<Sheep> allSheep = (ArrayList) agents[1];

        Double2D center = new Double2D(0, 0);
        for (Sheep sheep : allSheep) {
            center.add(yard.getObjectLocation(sheep));
        }
        return new Double2D(center.x / allSheep.size(), center.y / allSheep.size());
    }

    public boolean isValidMove(final Herding herding, final MutableDouble2D newLoc) {
        // check collisions with other agents?
        Bag inRadius = herding.yard.getNeighborsExactlyWithinDistance(new Double2D(loc), agentRadius);
        if (inRadius.size() > 1) {
            // velocity = velocity.negate();
            boolean checkNewPos = true;
            Double2D nearestAgentPos = new Double2D(herding.WIDTH + herding.RESOLUTION, herding.HEIGHT + herding.RESOLUTION);
            for (Object agent : inRadius) {
                if (!agent.equals(this)) {
                    Double2D agentPos = herding.yard.getObjectLocation(agent);
                    checkNewPos = checkNewPos && (newLoc.distance(agentPos) > loc.distance(agentPos));
                    if (nearestAgentPos.distance(agentPos) >= loc.distance(agentPos)) {
                        nearestAgentPos = agentPos;
                    }
                }
            }
            if (checkNewPos) {
                log("Valid colliding position: " + newLoc.toCoordinates());
                loc = newLoc;
            } else {
                log("Invalid colliding position: " + newLoc.toCoordinates());
                if (loc.x > nearestAgentPos.x) {
                    loc.x += 1;
                } else if (loc.x < nearestAgentPos.x) {
                    loc.x -= 1;
                }
                if (loc.y > nearestAgentPos.y) {
                    loc.y += 1;
                } else if (loc.y < nearestAgentPos.y) {
                    loc.y -= 1;
                }
                return false;
            }
        }


        // check walls X axis
        boolean checkXAxis = true;
        if (newLoc.x > herding.WIDTH) {
            // if (velocity.x > 0) velocity.x = -velocity.x;
            loc.x = herding.WIDTH - getRadius();
            checkXAxis = false;
        } else if (newLoc.x < 0) {
            // if (velocity.x < 0) velocity.x = -velocity.x;
            loc.x = getRadius();
            checkXAxis = false;
        }

        // check walls Y axis
        boolean checkYAxis = true;
        if (newLoc.y > herding.HEIGHT) {
            // if (velocity.y > 0) velocity.y = -velocity.y;
            loc.y = herding.HEIGHT - getRadius();
            checkYAxis = false;
        } else if (newLoc.y < 0) {
            // if (velocity.y < 0) velocity.y = -velocity.y;
            loc.y = getRadius();
            checkYAxis = false;
        }

        return (checkXAxis && checkYAxis);
    }

    public void capVelocity() {
        if (velocity.length() > cap)
            velocity = velocity.resize(cap);
    }

    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
        // log("Drawing entity");

        // draw the circle
        super.draw(object, graphics, info);

        // draw our line as well
        final double width = info.draw.width * radius * 2;
        final double height = info.draw.height * radius * 2;

        graphics.setColor(Color.white);
        double d = velocity.angle();
        graphics.drawLine((int)info.draw.x,
            (int)info.draw.y,
            (int)(info.draw.x) + (int)(width/2 * /*Strict*/Math.cos(d)),
            (int)(info.draw.y) + (int)(height/2 * /*Strict*/Math.sin(d)));

        // add the agent radius
        graphics.setColor(Color.yellow);
        graphics.drawOval((int) (info.draw.x - agentRadius), (int) (info.draw.y - agentRadius), (int) (agentRadius * 2), (int) (agentRadius * 2));
    }

    protected void log(String string) {
        Date date = Date.from(Instant.now());
        Calendar calendar = Calendar.getInstance();
        System.out.println(date + " : " + System.currentTimeMillis() + " " + this.getClass().getSimpleName() + " >> " + string);
    }

}
