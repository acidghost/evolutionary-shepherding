package nl.vu.ai.aso.simulation;

import nl.vu.ai.aso.shared.EvaluationResults;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

import java.util.ArrayList;
import java.util.List;


public class Herding extends SimState {

    private final double TIME_STEP_PERIOD = 1;

    public Continuous2D yard = new Continuous2D(0.1, 37, 37); //37x37 foot pasture
    List<double[]> shepherds;
    List<double[]> sheep;
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

        // add shepherds to the yard
        for(int i = 0; i < shepherds.size(); i++) {
            Shepherd shephard = new Shepherd(shepherds.get(i), shepherds.size() > 1 && predatorPresent ? 4 : 2);
            //TODO: set another spawn postion
            yard.setObjectLocation(shephard,
                new Double2D(yard.getWidth() * 0.5 + random.nextDouble() - 0.5,
                    yard.getHeight() * 0.5 + random.nextDouble() - 0.5));

            schedule.scheduleRepeating(shephard, TIME_STEP_PERIOD);
        }

        // add sheep to the yard
        for(int i = 0; i < sheep.size(); i++) {
            Sheep sheep = new Sheep(this.sheep.get(i), this.sheep.size() > 1 && predatorPresent ? 4 : 2);
            //TODO: set another spawn postion
            yard.setObjectLocation(sheep,
                new Double2D(yard.getWidth() - random.nextDouble() - 0.5,
                    yard.getHeight() - random.nextDouble() - 0.5));
            schedule.scheduleRepeating(sheep, TIME_STEP_PERIOD);
        }

        // TODO: add a predator if needed
    }

    public double sheepDistance() {
        return 0.5; //TODO: temp parameter
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

        runSimulation(fakeShepherd, fakeSheep, fakePredatorPresent);
        System.exit(0);
    }

    public static EvaluationResults runSimulation(List<double[]> shepherd, List<double[]> sheep, boolean predator) {
        Herding herding = new Herding(System.currentTimeMillis(), shepherd, sheep, predator);

        herding.setJob(1);
        herding.start();
        do
            if (!herding.schedule.step(herding)) break;
        while(herding.schedule.getSteps() < 5000); //TODO: check if 5000 is ok
        herding.finish();

        double sheepDist = herding.sheepDistance();
        return new EvaluationResults(0.0, sheepDist);
    }

}
