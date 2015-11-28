package nl.vu.ai.aso.simulation.agents;

import nl.vu.ai.aso.neuralnetwork.Mlp;
import nl.vu.ai.aso.shared.INetInputs;
import nl.vu.ai.aso.simulation.Herding;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

import java.awt.*;

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

    protected Double2D getNewPostion(INetInputs inputs) {
        // System.out.println("Requesting new position to NN -> " + this.getClass().getSimpleName());
        double[] output = network.feedforward(inputs.toArray());
        System.out.println("NN output is " + output[0] + ", " + output[1]);

        //TODO: transform the out of the NN in x,y coordinates
        return new Double2D(output[0], output[1]); //this returns the actual x,y as new computed position
    }

    abstract public MutableDouble2D getForces(Continuous2D yard);

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
}
