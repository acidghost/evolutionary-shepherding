package nl.vu.ai.aso.simulation.agents;

import nl.vu.ai.aso.simulation.Herding;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

import java.awt.*;
import java.time.Instant;
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

    public boolean isValidMove(final Herding herding, final MutableDouble2D newLoc) {
        // check collisions with other agents?
        Bag inRadius = herding.yard.getNeighborsExactlyWithinDistance(new Double2D(loc), agentRadius);
        if (inRadius.size() > 1) {
            velocity = velocity.negate();
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
                // log("Valid colliding position: " + newLoc.toCoordinates());
                loc = newLoc;
            } else {
                // log("Invalid colliding position: " + newLoc.toCoordinates());
                if (loc.x > nearestAgentPos.x) {
                    loc.x += herding.RESOLUTION;
                } else if (loc.x < nearestAgentPos.x) {
                    loc.x -= herding.RESOLUTION;
                }
                if (loc.y > nearestAgentPos.y) {
                    loc.y += herding.RESOLUTION;
                } else if (loc.y < nearestAgentPos.y) {
                    loc.y -= herding.RESOLUTION;
                }
                return false;
            }
        }


        // check walls X axis
        boolean checkXAxis = true;
        if (newLoc.x > herding.WIDTH) {
            if (velocity.x > 0) velocity.x = -velocity.x;
            loc.x = herding.WIDTH - getRadius();
            checkXAxis = false;
        } else if (newLoc.x < 0) {
            if (velocity.x < 0) velocity.x = -velocity.x;
            loc.x = getRadius();
            checkXAxis = false;
        }

        // check walls Y axis
        boolean checkYAxis = true;
        if (newLoc.y > herding.HEIGHT) {
            if (velocity.y > 0) velocity.y = -velocity.y;
            loc.y = herding.HEIGHT - getRadius();
            checkYAxis = false;
        } else if (newLoc.y < 0) {
            if (velocity.y < 0) velocity.y = -velocity.y;
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
        System.out.println(date + " : " + System.currentTimeMillis() + " " + this.getClass().getSimpleName() + " >> " + string);
    }

}
