package nl.vu.ai.aso;

import com.google.common.io.PatternFilenameFilter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Created by acidghost on 04/12/15.
 */
public class EvolutionaryShepherdingGUI extends JPanel {

    private static final Color BG_COLOR = Color.darkGray;
    private static final Dimension WINDOW_DIMENSION = new Dimension(500, 400);

    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel lPanel1;
    private JPanel lPanel2;
    private JPanel lPanel3;
    private JPanel rPanel1;
    private JComboBox<String> cmbBoxScenarios;
    private JButton btnStart;
    private JButton btnStop;

    private File resourcesFolderFile;
    private Thread simulationThread;

    public EvolutionaryShepherdingGUI() throws IOException {
        super(new BorderLayout());

        leftPanel = new JPanel();
        leftPanel.setBackground(BG_COLOR);

        rightPanel = new JPanel();
        rightPanel.setBackground(BG_COLOR);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);

        lPanel1 = new JPanel();
        lPanel1.setBackground(BG_COLOR);
        cmbBoxScenarios = new JComboBox<>();
        initComboBox();
        lPanel1.add(cmbBoxScenarios);

        lPanel2 = new JPanel();
        lPanel2.setBackground(BG_COLOR);

        lPanel3 = new JPanel();
        lPanel3.setBackground(BG_COLOR);

        leftPanel.add(lPanel1, BorderLayout.PAGE_START);
        leftPanel.add(lPanel2, BorderLayout.CENTER);
        leftPanel.add(lPanel3, BorderLayout.PAGE_END);

        rPanel1 = new JPanel();
        rPanel1.setBackground(BG_COLOR);
        btnStart = new JButton();
        btnStart.setText("Start simulation");
        btnStart.addActionListener(actionEvent -> {
            String selected = (String) cmbBoxScenarios.getSelectedItem();
            simulationThread = EvolutionaryShepherding.start(resourcesFolderFile.getPath() + "/" + selected);
            simulationThread.start();
            btnStop.setEnabled(true);
            btnStart.setEnabled(false);
        });
        btnStop = new JButton();
        btnStop.setText("Stop simulation");
        btnStop.setEnabled(false);
        btnStop.addActionListener(actionEvent -> {
            simulationThread.stop();
            btnStop.setEnabled(false);
            btnStart.setEnabled(true);
        });
        rPanel1.add(btnStart);
        rPanel1.add(btnStop);

        rightPanel.add(rPanel1, BorderLayout.CENTER);
    }

    private void initComboBox() {
        URL resourcesFolder = getClass().getClassLoader().getResource("");
        assert resourcesFolder != null;
        resourcesFolderFile = new File(resourcesFolder.getPath());
        for (File file : resourcesFolderFile.listFiles(new PatternFilenameFilter(".*\\.params"))) {
            String path = file.getPath();
            cmbBoxScenarios.addItem(path.split(resourcesFolder.getPath())[1]);
        }
    }

    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("Evolutionary Shepherding");
        frame.setPreferredSize(WINDOW_DIMENSION);
        frame.setBackground(BG_COLOR);

        EvolutionaryShepherdingGUI gui = new EvolutionaryShepherdingGUI();
        frame.setContentPane(gui);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
