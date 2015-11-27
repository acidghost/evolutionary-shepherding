package nl.vu.ai.aso.neuralnetwork;

import nl.vu.ai.aso.shared.Utils;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.util.TransferFunctionType;

import java.util.ArrayList;
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

        List<Double> input = new ArrayList<Double>();
        input.add(1.0);
        input.add(1.0);
        input.add(1.0);
        input.add(1.0);
        input.add(1.0);

        nn.feedforward(input);
    }

        // input, weights
        // output (next position)
    public double[] feedforward(List<Double> inputs) {
        this.mlp.setInput(Utils.toPrimitiveDouble(inputs));
        this.mlp.calculate();
        double[] networkOutput = mlp.getOutput();

        System.out.print("Input: " + Arrays.toString(inputs.toArray()));
        System.out.println(" Output: " + Arrays.toString(networkOutput));

        return networkOutput; //TODO: replace this comment with the specification of the return array
    }

}
