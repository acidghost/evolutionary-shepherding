package nl.vu.ai.aso.neuralnetwork;

import nl.vu.ai.aso.shared.Utils;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.util.TransferFunctionType;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Selene on 2015-11-24.
 */
public class Mlp {

    private MultiLayerPerceptron mlp;

    public Mlp(double[] weights, int inputs, int hidden) {
        mlp = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, inputs, hidden, 2);
        mlp.setWeights(weights);
    }

    public static void main(String[] args) {
        double[] weights = new double[] { 1, 2, 3 };
        Mlp nn = new Mlp(weights, 5, 3);
    }

        // input, weights
        // output (next position)
    public double[] feedforward(List<Double> inputs) {
        this.mlp.setInput(Utils.toPrimitiveDouble(inputs));
        this.mlp.calculate();
        double[] networkOutput = mlp.getOutput();

        System.out.print("Input: " + Arrays.toString(inputs.toArray()));
        System.out.println(" Output: " + Arrays.toString(networkOutput));

        return networkOutput; //// Array: [0] is distance to the center of mass of the sheep, [1] is bearing relative to center of mass and corral
    }

}
