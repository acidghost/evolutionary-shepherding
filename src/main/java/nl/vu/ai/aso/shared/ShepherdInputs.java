package nl.vu.ai.aso.shared;

/**
 * Created by erotundo on 25/11/15.
 */
public class ShepherdInputs implements INetInputs {

    public Double shepherd_r;
    public Double shepherd_b;
    public Double otherShepherd_r;
    public Double otherShepherd_b;

    public ShepherdInputs(Double shepherd_r, Double shepherd_b, Double otherShepherd_r, Double otherShepherd_b) {
        this.shepherd_b = shepherd_b;
        this.shepherd_r = shepherd_r;
        this.otherShepherd_b = otherShepherd_b;
        this.otherShepherd_r = otherShepherd_r;
    }

    public double[] toArray() {
        if (otherShepherd_r == null) {
            return new double[] {
                shepherd_r,
                shepherd_b
            };
        } else {
            return new double[] {
                shepherd_r,
                shepherd_b,
                otherShepherd_r,
                otherShepherd_b
            };
        }
    }

}
