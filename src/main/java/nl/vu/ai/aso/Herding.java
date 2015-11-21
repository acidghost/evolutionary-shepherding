package nl.vu.ai.aso;

import sim.engine.*;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;


public class Herding extends SimState {
    public Continuous2D yard = new Continuous2D(1.0,100,100);
    public int numShepherds= 1;
    private double speed = 0.1;

    public Herding(long seed) {
        super(seed);
    }

    public void start() {
        super.start();

        // clear the yard
        yard.clear();

        // add some students to the yard
        for(int i = 0; i < numShepherds; i++) {
            Shephard shephard = new Shephard();
            yard.setObjectLocation(shephard,
                new Double2D(yard.getWidth() * 0.5 + random.nextDouble() - 0.5,
                    yard.getHeight() * 0.5 + random.nextDouble() - 0.5));

            schedule.scheduleRepeating(shephard);
        }
    }


    public static void main(String[] args ) {
        doLoop(Herding.class, args);
        System.exit(0);
    }

}
