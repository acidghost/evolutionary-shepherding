package nl.vu.ai.aso.shared;

import java.util.Arrays;

/**
 * Created by acidghost on 24/11/15.
 */
public class EvaluationResults {

    private double[] shepherdScore;
    private double[] sheepScore;
    private SheepStatus sheepStatus;

    public EvaluationResults(double[] shepherdScore, double[] sheepScore, SheepStatus sheepStatus) {
        this.shepherdScore = shepherdScore;
        this.sheepScore = sheepScore;
        this.sheepStatus = sheepStatus;
    }

    public double[] getShepherdScore() {
        return shepherdScore;
    }

    public double[] getSheepScore() {
        return sheepScore;
    }

    public SheepStatus getSheepStatus() {
        return sheepStatus;
    }

    @Override
    public String toString() {
        return "Shepherd score: " + shepherdScore + "\nSheep score: " + Arrays.toString(sheepScore) + "\nSheep status: " + sheepStatus;
    }

}
