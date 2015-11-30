package nl.vu.ai.aso.neuralnetwork;

import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.util.TransferFunctionType;

/**
 * Created by Selene on 2015-11-24.
 */
public class Mlp {

    private MultiLayerPerceptron mlp;

    public Mlp(double[] weights, int inputs, int hidden) {
        int numberOfWeights = (inputs + 1) * hidden + (hidden + 1) * 2;
        if (weights.length != numberOfWeights)
            throw new WrongWeightsException("The number of weights (" + weights.length + ") is wrong. Should have been " + numberOfWeights + ".");

        mlp = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, inputs, hidden, 2);
        mlp.setWeights(weights);
    }

    public static void main(String[] args) {
        double[] weights = new double[] { 1, 2, 3 };
        Mlp nn = new Mlp(weights, 5, 3);
    }

    public double[] feedforward(double[] inputs) {
        mlp.setInput(inputs);
        mlp.calculate();

        return mlp.getOutput(); //// Array: [0] is radius from the center of mass of the sheep, [1] is bearing relative to center of mass and corral
    }

}
