package nl.vu.ai.aso.simulation;

import sim.util.Double2D;

/**
 * Created by acidghost on 24/11/15.
 */
public abstract class AgentWithNetwork {

    private double[] weights;
    private int nin;
    private int nhid;

    public AgentWithNetwork(double[] weights, int inputs, int hidden) {
        this.weights = weights;
        nin = inputs;
        nhid = hidden;
    }

    protected Double2D feedforward(double[] inputs) {
        //TODO: implement NN feedforward
        return null;
    }

}
