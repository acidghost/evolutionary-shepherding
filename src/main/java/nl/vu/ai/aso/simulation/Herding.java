package nl.vu.ai.aso.simulation;

import nl.vu.ai.aso.shared.EvaluationResults;
import nl.vu.ai.aso.simulation.agents.Predator;
import nl.vu.ai.aso.simulation.agents.Sheep;
import nl.vu.ai.aso.simulation.agents.Shepherd;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;

import java.util.ArrayList;
import java.util.List;


public class Herding extends SimState {

    private final double TIME_STEP_PERIOD = 1;
    public final int WIDTH = 37;
    public final int HEIGHT = 37;

    public Continuous2D yard = new Continuous2D(0.01, WIDTH, HEIGHT); //37x37 foot pasture
    public Double2D  corralPosition = new Double2D(yard.getWidth(), yard.getHeight() * 0.5); // left centered corral
    public List<double[]> shepherds;
    public List<double[]> sheep;
    private boolean predatorPresent;

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
            new Double2D(yard.getWidth() * 0.5 - 8, yard.getHeight() * 0.5),
            new Double2D(yard.getWidth() * 0.5 - 8, yard.getHeight() * 0.5),
            new Double2D(yard.getWidth() * 0.5 - 8, yard.getHeight() * 0.5)
        };

        // add shepherds to the yard
        for (int i = 0; i < shepherds.size(); i++) {
            double[] shepherdWeights = shepherds.get(i);
            Shepherd shepherd = new Shepherd(shepherdsPositions[i], shepherdWeights, 4);
            yard.setObjectLocation(shepherd, shepherdsPositions[i]);
            schedule.scheduleRepeating(shepherd, TIME_STEP_PERIOD);
        }

        //TODO: set another spawn postion
        final Double2D[] sheepPositions = new Double2D[] {
            new Double2D(yard.getWidth() * 0.5 + 8, yard.getHeight() * 0.5),
            new Double2D(yard.getWidth() * 0.5 + 8, yard.getHeight() * 0.5),
            new Double2D(yard.getWidth() * 0.5 + 8, yard.getHeight() * 0.5)
        };

        // add sheep to the yard
        for (int i = 0; i < sheep.size(); i++) {
            double[] sheepWeights = sheep.get(i);
            Sheep sheep = new Sheep(sheepPositions[i], sheepWeights, this.sheep.size() > 1 && predatorPresent ? 4 : 2);
            yard.setObjectLocation(sheep, sheepPositions[i]);
            schedule.scheduleRepeating(sheep, TIME_STEP_PERIOD);
        }

        System.out.println("Starting simulation with " + shepherds.size() + " shepherds and " + sheep.size() + " sheep.");
    }

    // for fitness function
    public double sheepDistance() {
        double totalDistance = 0.0;
        Object[] agents = sortAgents();
        ArrayList<Sheep> sheepAgents = (ArrayList) agents[1];

        for (int i = 0; i < sheepAgents.size(); i++){
            Double2D sheepPosition = yard.getObjectLocation(sheep);
            //double individualDistance = corralPosition.distance(sheepPosition);
            //totalDistance += individualDistance;
        }
        return totalDistance; //TODO: check it makes sense
    }

    public Object[] sortAgents(){
        Bag allAgents = yard.getAllObjects();

        // split agents based on their types
        ArrayList<Shepherd> shepherds = new ArrayList<Shepherd>();
        ArrayList<Sheep> sheep = new ArrayList<Sheep>();
        Predator predator = null;

        for (int i = 0; i < allAgents.size(); i++) {
            Object retrivedObj = allAgents.get(i);
            if (retrivedObj instanceof Shepherd){
                shepherds.add((Shepherd) retrivedObj);
            } else if (retrivedObj instanceof Sheep) {
                sheep.add((Sheep) retrivedObj);
            } else {
                // it is (the only) predator
                predator = (Predator) retrivedObj;
            }
        }
        return new Object[] {shepherds, sheep, predator}; // last one could be null
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

    public static EvaluationResults runSimulation(int totalSteps, List<double[]> shepherd, List<double[]> sheep, boolean predator) {
        Herding herding = new Herding(System.currentTimeMillis(), shepherd, sheep, predator);

        herding.loop(totalSteps);

        double sheepDist = herding.sheepDistance();
        return new EvaluationResults(0.0, sheepDist);
    }

    public void loop(int totalSteps) {
        setJob(1);
        start();
        do {
            // System.out.println(schedule.getSteps());
            if (!schedule.step(this)) break;
        } while(schedule.getSteps() < totalSteps);
        finish();
    }

}
