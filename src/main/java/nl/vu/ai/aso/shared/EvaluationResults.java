package nl.vu.ai.aso.shared;

/**
 * Created by acidghost on 24/11/15.
 */
public class EvaluationResults {

    private double shepherdScore;
    private double sheepScore;

    public EvaluationResults(double shepherdScore, double sheepScore) {
        this.shepherdScore = shepherdScore;
        this.sheepScore = sheepScore;
    }

    public double getShepherdScore() {
        return shepherdScore;
    }

    public double getSheepScore() {
        return sheepScore;
    }

    @Override
    public String toString() {
        return "Shepherd score: " + shepherdScore + "\nSheep score: " + sheepScore;
    }
}
