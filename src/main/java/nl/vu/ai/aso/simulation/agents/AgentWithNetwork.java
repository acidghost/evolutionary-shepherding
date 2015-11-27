package nl.vu.ai.aso.simulation.agents;

import nl.vu.ai.aso.neuralnetwork.Mlp;
import nl.vu.ai.aso.shared.INetInputs;
import sim.util.Double2D;

import java.awt.*;

/**
 * Created by acidghost on 24/11/15.
 */
public abstract class AgentWithNetwork extends Entity {

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

        //TODO: transform the out of the NN in x,y coordinates

        return new Double2D(1.0,1.0); //this returns the actual x,y as new computed position
    }

}
