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

    protected Double2D getNewPostion(List<Double> inputs, List<Double> weights) {
        //TODO: call the getNewPostion NN

        //TODO: transform the out of the NN in x,y coordinates

        return new Double2D(1.0,1.0); //this returns the actual x,y as new computed position
    }

}
