package nl.vu.ai.aso.shared;

/**
 * Created by acidghost on 27/11/15.
 */
public class SheepInputs {

    public Double shepherd_r;
    public Double shepherd_b;
    public Double sheep_r;
    public Double sheep_b;

    public SheepInputs(Double shepherd_r, Double shepherd_b, Double sheep_r, Double sheep_b) {
        this.shepherd_r = shepherd_r;
        this.shepherd_b = shepherd_b;
        this.sheep_r = sheep_r;
        this.sheep_b = sheep_b;
    }

    public double[] toArray() {
        if (sheep_r == null) {
            return new double[] {
                shepherd_r,
                shepherd_b
            };
        } else {
            return new double[] {
                shepherd_r,
                shepherd_b,
                sheep_r,
                sheep_b
            };
        }
    }

}
