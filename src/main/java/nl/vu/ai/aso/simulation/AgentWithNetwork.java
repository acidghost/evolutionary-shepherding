package nl.vu.ai.aso.simulation;

import nl.vu.ai.aso.neuralnetwork.Mlp;
import sim.util.Double2D;

import java.util.List;

/**
 * Created by acidghost on 24/11/15.
 */
public abstract class AgentWithNetwork {

    protected double[] _weights;
    private int _nnInputs;
    private int _nnHidden;

    private Mlp network;

    public AgentWithNetwork(double[] weights, int inputs, int hidden) {
        this._weights = weights;
        _nnInputs = inputs;
        _nnHidden = hidden;

        network = new Mlp(_weights, inputs, hidden);
    }

    protected Double2D getNewPostion(List<Double> inputs) {
        double[] output = network.feedforward(inputs);

        //TODO: transform the out of the NN in x,y coordinates

        return new Double2D(1.0,1.0); //this returns the actual x,y as new computed position
    }

}
