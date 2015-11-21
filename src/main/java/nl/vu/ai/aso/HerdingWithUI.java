package nl.vu.ai.aso;

import sim.portrayal.continuous.*;
import sim.engine.*;
import sim.display.*;
import sim.portrayal.simple.*;
import javax.swing.*;
import java.awt.Color;

public class HerdingWithUI extends GUIState {
    public Display2D display;
    public JFrame displayFrame;
    ContinuousPortrayal2D yardPortrayal = new ContinuousPortrayal2D();

    public HerdingWithUI() { super(new Herding(System.currentTimeMillis())); }

    public HerdingWithUI(SimState state) { super(state); }

    public static String getName() { return "Herding task"; }

    public void start() {
        super.start();
        setupPortrayals();
    }

    public void load(SimState state) {
        super.load(state);
        setupPortrayals();
    }

    public void setupPortrayals() {
        Herding herding = (Herding) state;

        // tell the portrayals what to portray and how to portray them
        yardPortrayal.setField( herding.yard );
        yardPortrayal.setPortrayalForAll(new OvalPortrayal2D());

        // reschedule the displayer
        display.reset();
        display.setBackdrop(Color.white);

        // redraw the display
        display.repaint();
    }

    public void init(Controller ctrl) {
        super.init(ctrl);
        display = new Display2D(600,600,this);
        display.setClipping(false);
        displayFrame = display.createFrame();
        displayFrame.setTitle("Herding Display");
        ctrl.registerFrame(displayFrame); // so the frame appears in the "Display" list displayFrame.setVisible(true);
        display.attach( yardPortrayal, "Yard" );
    }

    public void quit() {
        super.quit();
        if (displayFrame!=null)
            displayFrame.dispose();
        displayFrame = null;
        display = null;
    }

    public static void main(String[] args) {
        HerdingWithUI gui = new HerdingWithUI();
        Console c = new Console(gui); c.setVisible(true);
    }
}
