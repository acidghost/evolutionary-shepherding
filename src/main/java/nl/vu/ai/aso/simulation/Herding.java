package nl.vu.ai.aso.simulation;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

import java.util.HashMap;
import java.util.Map;


public class Herding extends SimState {
    public Continuous2D yard = new Continuous2D(1.0,100,100);
    private double[][] shepherds;
    private double[][] sheep;
    private boolean predatorPresent;
    private double speed = 0.1;

    public Herding(long seed, double[][] shepherds, double[][] sheep, boolean predatorPresent) {
        super(seed);
        this.shepherds = shepherds;
        this.sheep = sheep;
        this.predatorPresent = predatorPresent;
    }

    public void start() {
        super.start();

        // clear the yard
        yard.clear();

        for(int i = 0; i < shepherds.length; i++) {
            Shepherd shephard = new Shepherd(shepherds[i], shepherds.length > 1 && predatorPresent ? 5 : 3);
            yard.setObjectLocation(shephard,
                new Double2D(yard.getWidth() * 0.5 + random.nextDouble() - 0.5,
                    yard.getHeight() * 0.5 + random.nextDouble() - 0.5));

            schedule.scheduleRepeating(shephard);
        }

        //TODO: add some sheep to the yard
    }

    public double sheepDistance() {
        return 0.0;
    }


    public static void main(String[] args ) {
        doLoop(Herding.class, args);
        System.exit(0);
    }

    public static Map<String, Double> runSimulation(double[][] shepherd, double[][] sheep, boolean predator) {
        Herding herding = new Herding(System.currentTimeMillis(), shepherd, sheep, predator);

        herding.setJob(0);
        herding.start();
        do
            if (!herding.schedule.step(herding)) break;
        while(herding.schedule.getSteps() < 5000);
        herding.finish();

        double sheepDist = herding.sheepDistance();
        Map<String, Double> returnMap = new HashMap<String, Double>();
        returnMap.put("sheep", sheepDist);
        returnMap.put("shepherd", 0.0);
        return returnMap;
    }

}
