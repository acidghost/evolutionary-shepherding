package nl.vu.ai.aso.simulation.agents;

import nl.vu.ai.aso.simulation.Herding;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.MutableDouble2D;

import java.awt.*;

/**
 * Created by acidghost on 27/11/15.
 */
public abstract class Entity extends OvalPortrayal2D {

    private static final long serialVersionUID = 1;

    public MutableDouble2D loc, velocity, bump;
    public MutableDouble2D force = new MutableDouble2D();
    public MutableDouble2D accel = new MutableDouble2D();
    public MutableDouble2D newLoc = new MutableDouble2D();
    public MutableDouble2D sumVector = new MutableDouble2D(0, 0);

    public double speed, radius;

    public double cap;

    public double mass;

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
    public Entity(double newX, double newY, double newRadius, Color c) {
        super(c, newRadius * 2);  // scale is twice the radius

        loc = new MutableDouble2D(newX, newY);
        velocity = new MutableDouble2D(0, 0);
        bump = new MutableDouble2D(0, 0);
        radius = newRadius;

        mass = 1.0;
        cap = 1.0;

        speed = 0.4;
    }

    public boolean isValidMove(final Herding herding, final MutableDouble2D newLoc) {
        // TODO: implement me!
        return true;
    }

    public void capVelocity() {
        if (velocity.length() > cap)
            velocity = velocity.resize(cap);
    }

}
