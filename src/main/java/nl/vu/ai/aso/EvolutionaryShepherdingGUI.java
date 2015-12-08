package nl.vu.ai.aso;

import com.google.common.base.Optional;
import nl.vu.ai.aso.shared.EvaluationResults;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Window;

import javax.swing.*;
import java.awt.*;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by acidghost on 05/12/15.
 */
public class EvolutionaryShepherdingGUI extends Window implements Application {

    private static final Color BG_COLOR = Color.darkGray;
    private static final Dimensions DIMENSION = new Dimensions(800, 450);
    private static final String TITLE = "Evolutionary Shepherding";
    private static final int LOG_LENGTH = 15;
    private static final Font ITALIC_FONT = new Font("Times", Font.ITALIC, 11);

    private SplitPane mainPanel = new SplitPane();
    private FlowPane leftPanel = new FlowPane();
    private FlowPane rightPanel = new FlowPane();
    private Form form = new Form();
    private Form.Section simulationSection = new Form.Section();
    private Label simulationSectionLabel = new Label();
    private ListButton availableScenarios = new ListButton();
    private TextInput statFileInput = new TextInput();
    private TextInput evoRunNumberInput = new TextInput();
    private PushButton startButton = new PushButton();
    private PushButton stopButton = new PushButton();
    private Form.Section replaySection = new Form.Section();
    private Label replaySectionLabel = new Label();
    private ListButton availableReplays = new ListButton();
    private PushButton startReplay = new PushButton();
    private Slider speedSlider = new Slider();
    private Form.Section statsSection = new Form.Section();
    private Label statsSectionLabel = new Label();
    private ListButton availableStats = new ListButton();
    private PushButton drawChartsButton = new PushButton();
    private ListView logView = new ListView();
    private Label logNotice = new Label();

    private File resourcesFolderFile;
    private Task<Optional<EvaluationResults>> simulationTask;

    public EvolutionaryShepherdingGUI() {
        super();

        initLeftPanel();
        initRightPanel();

        mainPanel.getStyles().put("backgroundColor", BG_COLOR);
        mainPanel.setSplitRatio((float) 0.35);
        setContent(mainPanel);

        setTitle(TITLE);
        setMaximized(true);
        setPreferredSize(DIMENSION);
    }

    private void initAvailableScenarios() {
        URL resourcesFolder = getClass().getClassLoader().getResource("");
        assert resourcesFolder != null;
        resourcesFolderFile = new File(resourcesFolder.getPath());
        List<String> scenarios = new ArrayList<>();
        for (File file : resourcesFolderFile.listFiles(EvolutionaryShepherding.PARAMS_FILENAME_FILTER)) {
            String path = file.getPath();
            scenarios.add(path.split(resourcesFolder.getPath())[1]);
        }
        availableScenarios.setListData(scenarios);
        availableScenarios.getStyles().put("color", BG_COLOR);
    }

    private void initAvailableReplays() {
        List<String> listData = new ArrayList<>();
        File serialized = new File(EvolutionaryShepherding.SERIALIZED_DIR);

        try {
            Files.find(
                Paths.get(serialized.toURI()), 999,
                (p, bfa) -> bfa.isRegularFile() && EvolutionaryShepherding.SERIALIZED_FILENAME_FILTER.accept(p.toFile(), p.getFileName().toString())
            ).forEach(file -> listData.add(file.toFile().getPath().split(serialized.getPath())[1]));
        } catch (IOException e) {
            e.printStackTrace();
            Alert.alert(MessageType.ERROR, e.getClass().getSimpleName() + ": " + e.getMessage(), this);
        }

        availableReplays.setListData(listData);
    }

    private void initAvailableStats() {
        List<String> listData = new ArrayList<>();
        File root = new File("./");
        for (File file : root.listFiles(EvolutionaryShepherding.STATS_FILENAME_FILTER)) {
            listData.add(file.getPath());
        }
        availableStats.setListData(listData);
    }

    private void initLeftPanel() {
        initSimulationSection();
        initReplaySection();
        initStatsSection();

        Form.SectionSequence formSections = form.getSections();
        formSections.add(simulationSection);
        formSections.add(replaySection);
        formSections.add(statsSection);
        form.getStyles().put("backgroundColor", BG_COLOR);

        leftPanel.add(form);
        leftPanel.getStyles().put("backgroundColor", BG_COLOR);
        mainPanel.setLeft(leftPanel);
    }

    private void initSimulationSection() {
        simulationSectionLabel.setText("Simulation control panel");
        StyleDictionary simulationSectionLabelStyles = simulationSectionLabel.getStyles();
        simulationSectionLabelStyles.put("color", Color.white);
        simulationSectionLabelStyles.put("font", ITALIC_FONT);

        initAvailableScenarios();

        statFileInput.setPrompt("Filename *.stat for the run");
        evoRunNumberInput.setPrompt("Run number for this scenario");

        startButton.setButtonData("Start simulation");
        startButton.setEnabled(true);
        startButton.getButtonPressListeners().add(button -> {
            String selected = (String) availableScenarios.getSelectedItem();
            String statFile = statFileInput.getText();
            String runNumber = evoRunNumberInput.getText();
            if (selected == null || selected.equals("")) {
                Alert.alert(MessageType.WARNING, "No scenario selected!", this);
            } else if(statFile == null || statFile.equals("")) {
                Alert.alert(MessageType.WARNING, "Insert a meaningful stat filename!", this);
            } else if(runNumber == null || runNumber.equals("")) {
                Alert.alert(MessageType.WARNING, "Insert a run number", this);
            } else {
                simulationTask = EvolutionaryShepherding.runEvolution(resourcesFolderFile.getPath() + "/" + selected, statFile, runNumber);
                simulationTask.execute(new TaskAdapter<>(new TaskListener<Optional<EvaluationResults>>() {
                    @Override
                    public void taskExecuted(Task<Optional<EvaluationResults>> task) {
                        log("Simulation " + selected + " ended");
                        log(task.getResult().get().toString());
                        setComponentsState(false);
                        initAvailableReplays();
                        initAvailableStats();
                    }

                    @Override
                    public void executeFailed(Task<Optional<EvaluationResults>> task) {
                        log("Simulation " + selected + " ended with errors");
                        setComponentsState(false);
                        initAvailableReplays();
                        initAvailableStats();
                    }
                }));
                setComponentsState(true);
                log("Simulation " + selected + " started");
            }
        });

        stopButton.setButtonData("Stop simulation");
        stopButton.setEnabled(false);
        stopButton.getButtonPressListeners().add(button -> {
            simulationTask.abort();
            simulationTask = null;
            setComponentsState(false);
            log("Simulation " + availableScenarios.getSelectedItem() + " stopped");
            initAvailableReplays();
        });

        simulationSection.add(simulationSectionLabel);
        simulationSection.add(availableScenarios);
        simulationSection.add(statFileInput);
        simulationSection.add(evoRunNumberInput);
        simulationSection.add(startButton);
        simulationSection.add(stopButton);
    }

    private void initReplaySection() {
        replaySectionLabel.setText("Replay best individuals");
        StyleDictionary replaySectionLabelStyles = replaySectionLabel.getStyles();
        replaySectionLabelStyles.put("color", Color.white);
        replaySectionLabelStyles.put("font", ITALIC_FONT);

        initAvailableReplays();
        speedSlider.setRange(10, 500);
        speedSlider.setValue(50);
        startReplay.setButtonData("Start replay");
        startReplay.getButtonPressListeners().add(button -> {
            String selected = (String) availableReplays.getSelectedItem();
            if (selected == null || selected.equals("")) {
                Alert.alert(MessageType.WARNING, "Select a replay first!", this);
                return;
            }
            String filename = new File(EvolutionaryShepherding.SERIALIZED_DIR).getPath() + selected;
            try {
                simulationTask = EvolutionaryShepherding.replaySimulation(filename, speedSlider.getValue());
                simulationTask.execute(new TaskAdapter<>(new TaskListener<Optional<EvaluationResults>>() {
                    @Override
                    public void taskExecuted(Task<Optional<EvaluationResults>> task) {
                        log("Replay of " + selected + " ended");
                        log(task.getResult().get().toString());
                        setComponentsState(false);
                    }

                    @Override
                    public void executeFailed(Task<Optional<EvaluationResults>> task) {
                        log("Replay of " + selected + " ended with errors");
                        setComponentsState(false);
                    }
                }));
                setComponentsState(true);
                log("Replay of " + selected + " started");
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                Alert.alert(MessageType.ERROR, e.getClass().getCanonicalName() + ": " + e.getMessage(), this);
            }
        });

        replaySection.add(replaySectionLabel);
        replaySection.add(availableReplays);
        replaySection.add(speedSlider);
        replaySection.add(startReplay);
    }

    private void initStatsSection() {
        statsSectionLabel.setText("Chart statistics");
        statsSectionLabel.getStyles().put("font", ITALIC_FONT);
        statsSectionLabel.getStyles().put("color", Color.white);

        initAvailableStats();

        drawChartsButton.setButtonData("Draw charts");
        drawChartsButton.getButtonPressListeners().add(button -> {
            String selected = (String) availableStats.getSelectedItem();
            if (selected == null || selected.equals("")) {
                Alert.alert(MessageType.WARNING, "No stat file selected", this);
            } else {
                try {
                    Charts charts = new Charts(selected);
                    JFrame frame = new JFrame(selected);
                    frame.setContentPane(charts.getMeanPerSubpopPerGeneration(selected));
                    frame.setVisible(true);
                    frame.setSize(600, 400);
                } catch (IOException e) {
                    e.printStackTrace();
                    Alert.alert(MessageType.ERROR, e.getClass().getSimpleName() + ": " + e.getMessage(), this);
                }
            }
        });

        statsSection.add(statsSectionLabel);
        statsSection.add(availableStats);
        statsSection.add(drawChartsButton);
    }

    private void initRightPanel() {
        StyleDictionary logViewStyles = logView.getStyles();
        logViewStyles.put("backgroundColor", Color.black);
        logViewStyles.put("color", Color.green);

        logView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener() {
            @Override
            public boolean mouseDown(Component component, Mouse.Button button, int i, int i1) {
                return false;
            }

            @Override
            public boolean mouseUp(Component component, Mouse.Button button, int i, int i1) {
                return false;
            }

            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int i, int i1, int i2) {
                LocalManifest manifest = new LocalManifest();
                manifest.putText((String) logView.getSelectedItem());
                Clipboard.setContent(manifest);
                return false;
            }
        });

        logNotice.setText("Clicking on a log item copies its content into the clipboard. Be careful.");
        logNotice.getStyles().put("font", new Font("Times", Font.PLAIN, 8));
        logNotice.getStyles().put("color", Color.lightGray);

        rightPanel.add(logView);
        rightPanel.add(logNotice);
        rightPanel.getStyles().put("backgroundColor", BG_COLOR);
        mainPanel.setRight(rightPanel);
    }

    private void setComponentsState(boolean simOn) {
        if (simOn) {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            availableScenarios.setEnabled(false);
            startReplay.setEnabled(false);
        } else {
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            availableScenarios.setEnabled(true);
            startReplay.setEnabled(true);
        }
    }

    @Override
    public void startup(Display display, Map<String, String> map) throws Exception {
        this.open(display);
        java.awt.Frame frame = (Frame) display.getDisplayHost().getParent();
        frame.setBackground(BG_COLOR);
        frame.setSize(DIMENSION.width, DIMENSION.height);
    }

    @Override
    public boolean shutdown(boolean b) throws Exception {
        close();
        return false;
    }

    @Override
    public void suspend() throws Exception {}

    @Override
    public void resume() throws Exception {}

    protected void log(String string) {
        List listData = logView.getListData();
        if (listData.getLength() > LOG_LENGTH - 1) {
            listData.remove(listData.get(0));
        }
        listData.add(System.currentTimeMillis() + " >> " + string + "\n");
        logView.setListData(listData);
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(EvolutionaryShepherdingGUI.class, args);
    }

}
