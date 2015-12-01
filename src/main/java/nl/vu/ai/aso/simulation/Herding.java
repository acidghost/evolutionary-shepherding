package nl.vu.ai.aso.simulation;

import nl.vu.ai.aso.shared.EvaluationResults;
import nl.vu.ai.aso.simulation.agents.Sheep;
import nl.vu.ai.aso.simulation.agents.Shepherd;
import sim.engine.SimState;
import sim.util.Double2D;

import java.util.ArrayList;
import java.util.List;


public class Herding extends SimState {

    public enum EndSimulation {
        SHEEP_ESCAPES,
        SHEEP_CORRALED,
        CONTINUE
    }

    private final double TIME_STEP_PERIOD = 1;
    public final int WIDTH = 37;
    public final int HEIGHT = 37;
    public final double RESOLUTION = 1;

    public Yard yard = new Yard(RESOLUTION, WIDTH, HEIGHT); //37x37 foot pasture
    public List<double[]> shepherds;
    public List<double[]> sheep;
    private boolean predatorPresent;
    public List<Sheep> sheepAgents;
    public List<Shepherd> shepherdAgents;

    public static double cumulativeSheepDist = 0;
    public static double cummulativeSheepRatio = 0;

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

    public double[] individualSheepDistances() {
        double[] distances = new double[this.sheep.size()];
        for (int i = 0; i < sheepAgents.size(); i++) {
            distances[i] = sheepAgents.get(i).travelledDistance;
        }
        return distances;
    }


    public static EvaluationResults runSimulation(int totalSteps, List<double[]> shepherd, List<double[]> sheep, boolean predator) {
        Herding herding = new Herding(System.currentTimeMillis(), shepherd, sheep, predator);

        herding.loop(totalSteps);

        double shepherdFitness = -Herding.cumulativeSheepDist;
        double[] sheepFitness = herding.individualSheepDistances();

        for (int i = 1; i < sheepFitness.length ; i++){
            sheepFitness[i] =- Herding.cummulativeSheepRatio;
        }

        EvaluationResults results = new EvaluationResults(shepherdFitness, sheepFitness);
        herding.endLoopStuff();

        return results;
    }

    public EndSimulation insideLoopStuff() {
        cumulativeSheepDist += yard.allSheepDistance();
        cummulativeSheepRatio += yard.getSheepRatio();
        return EndSimulation.CONTINUE;
    }

    public void endLoopStuff() {
        cumulativeSheepDist = 0;
        cummulativeSheepRatio = 0;
    }

    public void loop(int totalSteps) {
        setJob(1);
        start();
        do {
            // System.out.println(schedule.getSteps());
            insideLoopStuff();
            if (!schedule.step(this)) break;
        } while(schedule.getSteps() < totalSteps);

        finish();
    }

}
