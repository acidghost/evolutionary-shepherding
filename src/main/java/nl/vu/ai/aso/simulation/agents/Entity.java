package nl.vu.ai.aso.simulation.agents;

import nl.vu.ai.aso.simulation.Herding;
import nl.vu.ai.aso.simulation.Yard;
import sim.engine.SimState;
import sim.engine.Steppable;
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
public abstract class Entity extends OvalPortrayal2D implements Steppable {

    private static final long serialVersionUID = 1;

    public MutableDouble2D loc, velocity, bump;
    public MutableDouble2D force = new MutableDouble2D();
    public MutableDouble2D acceleration = new MutableDouble2D();
    public MutableDouble2D newLoc = new MutableDouble2D();

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
        scale = radius;  // so our ovalportrayal knows how to draw/hit us right
    }

    public double getMass() {
        return mass;
    }

    public void setMass(double newMass) {
        mass = newMass;
    }

    // Constructor
    public Entity(double newX, double newY, double newRadius, Color c, double newAgentRadius) {
        super(c, newRadius);  // scale is twice the radius

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
        Bag inRadius = herding.yard.getNeighborsWithinDistance(new Double2D(loc), (agentRadius + Math.max(Shepherd.AGENT_RADIUS, Sheep.AGENT_RADIUS)) * 1.1);
        boolean collided = false;
        for (Object agent : inRadius) {
            if (!agent.equals(this)) {
                Entity entity = (Entity) agent;
                if (entity.loc.distance(newLoc) < entity.agentRadius + agentRadius) {
                    MutableDouble2D tmpVec = new MutableDouble2D();
                    if (agentRadius > entity.agentRadius) {
                        collided = true;

                        tmpVec.subtract(entity.loc, loc);
                        tmpVec.normalize();

                        entity.bump.x = tmpVec.x * 2.0;
                        entity.bump.y = tmpVec.y * 2.0;
                        entity.loc.x += 1.2 * tmpVec.x;
                        entity.loc.y += 1.2 * tmpVec.y;

                        velocity.multiplyIn(0.9);
                    }
                }
            }
        }

        if (collided) {
            // log("Collided at: " + newLoc.toCoordinates());
            // return false;
        }

        // check walls X axis
        boolean checkXAxis = true;
        if (newLoc.x > Herding.WIDTH) {
            if (velocity.x > 0) velocity.x = -velocity.x;
            loc.x = Herding.WIDTH - .5;
            checkXAxis = false;
        } else if (newLoc.x < 0) {
            if (velocity.x < 0) velocity.x = -velocity.x;
            loc.x = .5;
            checkXAxis = false;
        }

        // check walls Y axis
        boolean checkYAxis = true;
        if (newLoc.y > Herding.HEIGHT) {
            if (velocity.y > 0) velocity.y = -velocity.y;
            loc.y = Herding.HEIGHT - .5;
            checkYAxis = false;
        } else if (newLoc.y < 0) {
            if (velocity.y < 0) velocity.y = -velocity.y;
            loc.y = .5;
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
        final double width = info.draw.width * radius;
        final double height = info.draw.height * radius;

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

    abstract public MutableDouble2D getForces(Herding herding);

    @Override
    public void step(SimState simState) {
        Herding herding = (Herding) simState;
        Yard yard = herding.yard;

        MutableDouble2D force = getForces(herding);
        force.addIn(bump);
        bump.x = 0;
        bump.y = 0;
        // log("Force is " + force.toCoordinates());

        // acceleration = f/m
        acceleration.multiply(force, 1 / mass); // resets acceleration
        // log("Acc is " + acceleration.toCoordinates());

        // v = v + a
        velocity.addIn(acceleration);
        capVelocity();
        // log("Vel is " + velocity.toCoordinates());

        // L = L + v
        newLoc = new MutableDouble2D(loc.x + velocity.x, loc.y + velocity.y);
        // log("NewLoc & loc: " + newLoc.toCoordinates() + " " + loc.toCoordinates());

        // is new location valid?
        if (isValidMove(herding, newLoc)) {
            loc = newLoc;
            // log("Valid move: " + loc.toCoordinates());
        }

        // log("Loc final: " + loc.toCoordinates() + "\n");

        yard.setObjectLocation(this, new Double2D(loc));
    }

    protected void log(String string) {
        Date date = Date.from(Instant.now());
        System.out.printf("%-60s %s\t%s\n", date + " : " + System.currentTimeMillis() + " " + this.getClass().getSimpleName(), ">>", string);
    }

}
