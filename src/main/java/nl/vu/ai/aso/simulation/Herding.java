package nl.vu.ai.aso.simulation;

import nl.vu.ai.aso.shared.EvaluationResults;
import nl.vu.ai.aso.shared.SheepStatus;
import nl.vu.ai.aso.simulation.agents.Sheep;
import nl.vu.ai.aso.simulation.agents.Shepherd;
import sim.engine.SimState;
import sim.util.Double2D;

import java.util.ArrayList;
import java.util.List;


public class Herding extends SimState {

    private final double TIME_STEP_PERIOD = 1;
    public static final int WIDTH = 37;
    public static final int HEIGHT = 37;
    public static final double RESOLUTION = 1;

    public final int CORRALED_BONUS = 500;
    public final int ESCAPED_BONUS = 500;
    public final int BUMP_BONUS_SCALE = 10;

    public Yard yard = new Yard(RESOLUTION, WIDTH, HEIGHT); //37x37 foot pasture
    public List<double[]> shepherds;
    public List<double[]> sheep;
    private boolean predatorPresent;
    public List<Sheep> sheepAgents;
    public List<Shepherd> shepherdAgents;

    public static double cumulativeSheepDist = 0;
    public static double cumulativeSheepRatio = 0;
    public static SheepStatus sheepStatus = SheepStatus.NORMAL;

    public Herding(long seed, List<double[]> shepherds, List<double[]> sheeps, boolean predatorPresent) {
        super(seed);
        this.shepherds = shepherds;
        this.sheep = sheeps;
        this.predatorPresent = predatorPresent;
    }

    public void start() {
        super.start();

        // clear the yard
        yard.clear();

        //TODO: set another spawn position
        final Double2D[] shepherdsPositions = new Double2D[] {
            new Double2D(yard.getWidth() * 0.5 - 8, yard.getHeight() * 0.5 - Shepherd.AGENT_RADIUS),
            new Double2D(yard.getWidth() * 0.5 - 8, yard.getHeight() * 0.5),
            new Double2D(yard.getWidth() * 0.5 - 8, yard.getHeight() * 0.5 + Shepherd.AGENT_RADIUS)
        };

        // add shepherds to the yard
        shepherdAgents = new ArrayList<>();
        for (int i = 0; i < shepherds.size(); i++) {
            double[] shepherdWeights = shepherds.get(i);
            Shepherd shepherd = new Shepherd(shepherdsPositions[i], shepherdWeights, 4);
            shepherdAgents.add(shepherd);
            yard.setObjectLocation(shepherd, shepherdsPositions[i]);
            schedule.scheduleRepeating(shepherd, TIME_STEP_PERIOD);
        }

        //TODO: set another spawn position
        final Double2D[] sheepPositions = new Double2D[] {
            new Double2D(yard.getWidth() * 0.5 + 8, yard.getHeight() * 0.5 - Sheep.AGENT_RADIUS),
            new Double2D(yard.getWidth() * 0.5 + 8, yard.getHeight() * 0.5),
            new Double2D(yard.getWidth() * 0.5 + 8, yard.getHeight() * 0.5 + Sheep.AGENT_RADIUS)
        };

        // add sheep to the yard
        sheepAgents = new ArrayList<>();

        for (int i = 0; i < sheep.size(); i++) {
            double[] sheepWeights = sheep.get(i);
            Sheep aSheep = new Sheep(sheepPositions[i], sheepWeights, this.sheep.size() > 1 ? 4 : 2);
            sheepAgents.add(aSheep);
            yard.setObjectLocation(aSheep, sheepPositions[i]);
            schedule.scheduleRepeating(aSheep, TIME_STEP_PERIOD);
        }

        System.out.println("Starting simulation with " + shepherds.size() + " shepherds and " + sheep.size() + " sheep.");
    }

    // development's convenient method
    public static void main(String[] args ) {

        List<double[]> fakeShepherd = new ArrayList<>();
        fakeShepherd.add(new double[] { 1.0, 2.0, 4.5, 6.7 });
        fakeShepherd.add(new double[] {2.0, 1.1, 2.2, 1.4 });

        List<double[]> fakeSheep = new ArrayList<>();
        fakeSheep.add(new double[] { 1.0, 2.0, 4.5, 6.7 });
        fakeSheep.add(new double[] {2.0, 1.1, 2.2, 1.4 });

        boolean fakePredatorPresent = false;

        runSimulation(5000, fakeShepherd, fakeSheep, fakePredatorPresent);
        System.exit(0);
    }

    public double[] individualSheepScores() {
        double[] distances = new double[this.sheep.size()];
        for (int i = 0; i < sheepAgents.size(); i++) {
            distances[i] = sheepAgents.get(i).travelledDistance - cumulativeSheepRatio;
            switch (sheepStatus) {
                case CORRALED:
                    distances[i] -= CORRALED_BONUS;
                    break;
                case ESCAPED:
                    distances[i] += ESCAPED_BONUS;
                    break;
            }
        }
        return distances;
    }

    public double[] individualShepherdScores() {
        double[] scores = new double[this.shepherds.size()];

        for (int i = 0; i < shepherdAgents.size(); i++) {
            scores[i] = (shepherdAgents.get(i).numberOfBumpsWithSheep * BUMP_BONUS_SCALE) - cumulativeSheepDist;
        }
        return scores;
    }


    public static EvaluationResults runSimulation(int totalSteps, List<double[]> shepherd, List<double[]> sheep, boolean predator) {
        Herding herding = new Herding(System.currentTimeMillis(), shepherd, sheep, predator);

        herding.loop(totalSteps);

        double[] shepherdFitness = herding.individualShepherdScores();
        double[] sheepFitness = herding.individualSheepScores();

        EvaluationResults results = new EvaluationResults(shepherdFitness, sheepFitness, sheepStatus);
        herding.endLoopStuff();

        return results;
    }

    public boolean insideLoopStuff() {
        cumulativeSheepDist += yard.allSheepDistance();
        cumulativeSheepRatio += yard.getSheepRatio(sheepAgents);

        if (sheep.size() > 1 && sheepAgents.size() < 2) {
            // System.out.println("Not enough sheep.");
            return false;
        }

        List<Sheep> copiedSheep = new ArrayList<>(sheepAgents);
        for (Sheep sheep : copiedSheep) {
            sheepStatus = yard.getSheepStatus(sheep);
            switch (sheepStatus) {
                case CORRALED:
                    cumulativeSheepDist -= CORRALED_BONUS;
                    yard.remove(sheep);
                    return false;
                case ESCAPED:
                    cumulativeSheepDist += ESCAPED_BONUS;
                    yard.remove(sheep);
                    return false;
                case NORMAL:
                    break;
            }
        }

        return true;
    }

    public void endLoopStuff() {
        cumulativeSheepDist = 0;
        cumulativeSheepRatio = 0;
        sheepStatus = SheepStatus.NORMAL;
    }

    public void loop(int totalSteps) {
        setJob(1);
        start();
        do {
            // System.out.println(schedule.getSteps());
            if (!insideLoopStuff()) {
                break;
            }
            if (!schedule.step(this)) break;
        } while(schedule.getSteps() < totalSteps);

        finish();
    }

}