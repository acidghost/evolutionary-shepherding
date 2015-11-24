package nl.vu.ai.aso.simulation;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Herding extends SimState {
    public Continuous2D _yard = new Continuous2D(0.1, 37, 37); //37x37 foot pasture
    private double[][] _shepherds;
    private double[][] _sheeps;
    private boolean _predatorPresent;

    public Herding(long seed, double[][] shepherds, double[][] sheeps, boolean predatorPresent) {
        super(seed);
        this._shepherds = shepherds;
        this._sheeps = sheeps;
        this._predatorPresent = predatorPresent;
    }

    public void start() {
        super.start();

        // clear the yard
        _yard.clear();

        // add sheperds to the yard
        for(int i = 0; i < _shepherds.length; i++) {
            Shepherd shephard = new Shepherd(_shepherds[i], _shepherds.length > 1 && _predatorPresent ? 5 : 3);
            //TODO: set another spawn postion
            _yard.setObjectLocation(shephard,
                new Double2D(_yard.getWidth() * 0.5 + random.nextDouble() - 0.5,
                    _yard.getHeight() * 0.5 + random.nextDouble() - 0.5));

            schedule.scheduleRepeating(shephard);
        }

        // add sheeps to the yard
        for(int i = 0; i < _sheeps.length; i++) {
            Sheep sheep = new Sheep(_sheeps[i], _sheeps.length > 1 && _predatorPresent ? 5 : 3);
            //TODO: set another spawn postion
            _yard.setObjectLocation(sheep,
                new Double2D(_yard.getWidth() - random.nextDouble() - 0.5,
                    _yard.getHeight() - random.nextDouble() - 0.5));
            schedule.scheduleRepeating(sheep);
        }

        // TODO: add a predator if needed
    }

    public double sheepDistance() {
        return 0.5; //TODO: temp parameter
    }

    // development's convenient method
    public static void main(String[] args ) {

        double[][] fakeShepherd = new double[][] {{1,2}, {1,2}};
        double[][] fakeSheep = new double[][] {{1,2}, {1,2}};
        boolean fakePredatorPresent = false;

        runSimulation(fakeShepherd, fakeSheep, fakePredatorPresent);
        System.exit(0);
    }

    public static Map<String, Double> runSimulation(double[][] shepherd, double[][] sheep, boolean predator) {
        Herding herding = new Herding(System.currentTimeMillis(), shepherd, sheep, predator);

        herding.setJob(1);
        herding.start();
        do
            if (!herding.schedule.step(herding)) break;
        while(herding.schedule.getSteps() < 5000); //TODO: check if 5000 is ok
        herding.finish();

        double sheepDist = herding.sheepDistance();
        Map<String, Double> returnMap = new HashMap<String, Double>();
        returnMap.put("sheep", sheepDist);
        return returnMap;
    }

}
