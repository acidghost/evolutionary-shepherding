package nl.vu.ai.aso.simulation;

import nl.vu.ai.aso.neuralnetwork.Mlp;
import nl.vu.ai.aso.shared.ShepherdInputs;
import sim.util.Double2D;

/**
 * Created by acidghost on 24/11/15.
 */
public abstract class AgentWithNetwork {

    private Mlp network;

    public AgentWithNetwork(double[] weights, int inputs, int hidden) {
        network = new Mlp(weights, inputs, hidden);
    }

    public AgentWithNetwork(double[] weights, int inputs) {
        this(weights, inputs, inputs > 2 ? 5 : 3);
    }

    protected Double2D getNewPostion(ShepherdInputs inputs) {
        double[] output = network.feedforward(inputs.toArray());

        //TODO: transform the out of the NN in x,y coordinates

        return new Double2D(1.0,1.0); //this returns the actual x,y as new computed position
    }

}
