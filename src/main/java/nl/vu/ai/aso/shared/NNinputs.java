package nl.vu.ai.aso.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by erotundo on 25/11/15.
 */
public class NNinputs extends Object {
    public Double shepherd_b;
    public Double shepherd_r;
    public Double otherShepherd_b;
    public Double otherShepherd_r;
    public Double fox_b;
    public Double fox_r;

    public NNinputs() {

    }

    public List<Double> toList() {
        return Arrays.asList(shepherd_b, shepherd_r, otherShepherd_b, otherShepherd_r, fox_b, fox_r);
    }
}
