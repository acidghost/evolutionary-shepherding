package nl.vu.ai.aso.simulation;

import sim.util.Double2D;

import java.util.List;

/**
 * Created by acidghost on 24/11/15.
 */
public abstract class AgentWithNetwork {

    private List<Double> _weights;
    private int _nnInputs;
    private int _nnHidden;

    public AgentWithNetwork(List<Double> weights, int inputs, int hidden) {
        this._weights = weights;
        _nnInputs = inputs;
        _nnHidden = hidden;
    }

    protected Double2D feedforward(List<Double> inputs) {
        //TODO: call the feedforward NN
        return null;
    }

}
