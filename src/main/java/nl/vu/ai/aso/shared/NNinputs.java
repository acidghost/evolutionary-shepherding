package nl.vu.ai.aso.shared;

import java.util.Arrays;
import java.util.List;

/**
 * Created by erotundo on 25/11/15.
 */
public class NNinputs {
    public double shepherd_b;
    public double shepherd_r;
    public double otherShepherd_b;
    public double otherShepherd_r;

    public NNinputs(double shepherd_b, double shepherd_r, double otherShepherd_b, double otherShepherd_r) {
        this.shepherd_b = shepherd_b;
        this.shepherd_r = shepherd_r;
        this.otherShepherd_b = otherShepherd_b;
        this.otherShepherd_r = otherShepherd_r;
    }

    public List<Double> toList() {
        return Arrays.asList(shepherd_b, shepherd_r, otherShepherd_b, otherShepherd_r, 1.0);
    }

    public double[] toArray() {
        return new double[] {
            shepherd_b,
            shepherd_r,
            otherShepherd_b,
            otherShepherd_r,
            1.0
        };
    }
}
