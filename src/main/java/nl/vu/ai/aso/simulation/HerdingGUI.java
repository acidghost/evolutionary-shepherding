package nl.vu.ai.aso.simulation;

import nl.vu.ai.aso.EvolutionaryShepherding;
import nl.vu.ai.aso.shared.EvaluationResults;
import nl.vu.ai.aso.simulation.agents.Sheep;
import nl.vu.ai.aso.simulation.agents.Shepherd;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.continuous.ContinuousPortrayal2D;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by acidghost on 27/11/15.
 */
public class HerdingGUI extends GUIState {

    private Display2D display;
    private JFrame displayFrame;
    private ContinuousPortrayal2D yardPortrayal = new ContinuousPortrayal2D();

    public HerdingGUI(long seed, List<double[]> shepherds, List<double[]> sheep, boolean predatorPresent) {
        super(new Herding(seed, shepherds, sheep, predatorPresent));
    }

    public HerdingGUI(SimState state) {
        super(state);
    }

    public static void main(String[] args) {
        double[] fakeWeights = new double[] { 6.673464008475244, -4.408446883478276, 6.96540199276086, 0.6816368254846797, -3.963290615449236, 7.440834549536257, -5.8568433751761075, -3.3983121134635756, -0.6749812325310576, -8.898156585053416, -9.171094268071446, 0.7106209774843295, 1.9887581816067987, -2.7924804118783633, 8.911848736351539, 8.519068465273103, 1.5311006655962132 };

        List<double[]> fakeShepherd = new ArrayList<>();
        fakeShepherd.add(fakeWeights);
        fakeShepherd.add(fakeWeights);

        List<double[]> fakeSheep = new ArrayList<>();
        fakeSheep.add(fakeWeights);
        fakeSheep.add(fakeWeights);

        boolean fakePredatorPresent = false;
        EvaluationResults results = runSimulation(5000, 1000, fakeShepherd, fakeSheep, fakePredatorPresent);
        System.out.println("Got results:\n" + results.toString());
    }

    public static EvaluationResults runSimulation(int totalSteps, int timeToSleep, List<double[]> shepherd, List<double[]> sheep, boolean predator) {
        HerdingGUI herdingGUI = new HerdingGUI(System.currentTimeMillis(), shepherd, sheep, predator);
        Console console = new Console(herdingGUI);
        console.setVisible(true);
        console.setPlaySleep(timeToSleep);

        Herding herding = (Herding) herdingGUI.state;
        System.out.println("Starting GUI simulation for " + totalSteps + " steps");
        for (int i = 0; i < totalSteps; i++) {
            console.pressPlay();
            console.pressPause();
            Herding.cumulativeSheepDist += herding.yard.allSheepDistance();
            try {
                Thread.sleep((long) timeToSleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        console.pressStop();

        herdingGUI.quit();
        console.dispose();

        EvaluationResults results = new EvaluationResults(-Herding.cumulativeSheepDist, 0.0);
        Herding.cumulativeSheepDist = 0;
        return results;
    }

    @Override
    public void start() {
        super.start();
        setupPortrayals();
    }

    @Override
    public void load(SimState state) {
        super.load(state);
        setupPortrayals();
    }

    private void setupPortrayals() {
        Herding herding = (Herding) state;

        yardPortrayal.setField(herding.yard);

        // tell the portrayals what to portray and how to portray them
        yardPortrayal.setPortrayalForClass(Shepherd.class, new sim.portrayal.simple.RectanglePortrayal2D(Color.red));
        yardPortrayal.setPortrayalForClass(Sheep.class, new sim.portrayal.simple.OvalPortrayal2D(Color.white));

        display.reset();
        display.setBackdrop(Color.black);

        display.repaint();
    }

    @Override
    public void init(Controller controller) {
        super.init(controller);
        display = new Display2D(600, 600, this);
        displayFrame = display.createFrame();
        displayFrame.setTitle("Herding Display");
        controller.registerFrame(displayFrame);
        // so the frame appears in the "Display" list
        displayFrame.setVisible(true);
        display.attach(yardPortrayal, "Yard" );
    }

    @Override
    public boolean step() {
        // System.out.println("Doing step in GUI: " + state.schedule.getSteps());
        return super.step();
    }

    @Override
    public void quit() {
        super.quit();
        if (displayFrame!=null) displayFrame.dispose();
        displayFrame = null;
        display = null;
    }

}
