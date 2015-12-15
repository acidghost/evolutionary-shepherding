package nl.vu.ai.aso;

import com.google.common.base.Optional;
import nl.vu.ai.aso.shared.EvaluationResults;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Window;

import javax.swing.*;
import java.awt.*;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

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
    private Checkbox runsCheckbox = new Checkbox();
    private TextInput evoRunNumberInput = new TextInput();
    private PushButton startButton = new PushButton();
    private PushButton stopButton = new PushButton();
    private Form.Section replaySection = new Form.Section();
    private Label replaySectionLabel = new Label();
    private ListButton availableReplays = new ListButton();
    private TextInput replayRun = new TextInput();
    private TextInput replayGeneration = new TextInput();
    private PushButton startReplay = new PushButton();
    private Slider speedSlider = new Slider();
    private Form.Section statsSection = new Form.Section();
    private Label statsSectionLabel = new Label();
    private ListButton availableStats = new ListButton();
    private TextInput statsRun = new TextInput();
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
            URL trial = null;
            try {
                trial = file.toURI().toURL();
                assert trial != null;

                String path = trial.toString();
                scenarios.add(path.split(resourcesFolder.getPath())[1]);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        availableScenarios.setListData(scenarios);
        availableScenarios.getStyles().put("color", BG_COLOR);
    }

    private void initAvailableReplays() {
        File serialized = new File(EvolutionaryShepherding.SERIALIZED_DIR);
        List<String> listData = new ArrayList<>();

        final File[] files = serialized.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                listData.add(file.getPath().split(serialized.getPath())[1]);
            }
        }


        availableReplays.setListData(listData);
    }

    private void initAvailableStats() {
        File statsDir = new File(EvolutionaryShepherding.STATISTICS_DIR);
        List<String> scenarios = new ArrayList<>();

        final File[] files = statsDir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scenarios.add(file.getPath().split(statsDir.getPath())[1]);
            }
        }

        availableStats.setListData(scenarios);
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

        runsCheckbox.setButtonData("Multiple runs");
        runsCheckbox.setSelected(true);

        evoRunNumberInput.setPrompt("Run number or number of runs");

        startButton.setButtonData("Start simulation");
        startButton.setEnabled(true);
        startButton.getButtonPressListeners().add(button -> {
            String selected = (String) availableScenarios.getSelectedItem();
            String runNumber = evoRunNumberInput.getText();
            if (selected == null || selected.equals("")) {
                Alert.alert(MessageType.WARNING, "No scenario selected!", this);
            } else if(runNumber == null || runNumber.equals("")) {
                Alert.alert(MessageType.WARNING, "Insert a run number", this);
            } else {
                final boolean multipleRuns = runsCheckbox.getState().equals(Button.State.SELECTED);
                if (multipleRuns) {
                    // multiple runs of the same scenario
                    final int runs = Integer.parseInt(runNumber);
                    for (int run = 1; run <= runs; run++) {
                        simulationTask = EvolutionaryShepherding.runEvolution(resourcesFolderFile.getPath() + File.separator + selected, String.valueOf(run));
                        final int finalRun = run;
                        simulationTask.execute(new TaskAdapter<>(new TaskListener<Optional<EvaluationResults>>() {
                            @Override
                            public void taskExecuted(Task<Optional<EvaluationResults>> task) {
                                log("Run " + finalRun + " of " + selected + " ended");
                                log(task.getResult().get().toString());
                                setComponentsState(false);
                                initAvailableReplays();
                                initAvailableStats();
                            }

                            @Override
                            public void executeFailed(Task<Optional<EvaluationResults>> task) {
                                log("Run " + finalRun + " of " + selected + " ended with errors");
                                setComponentsState(false);
                                initAvailableReplays();
                                initAvailableStats();
                            }
                        }));

                        setComponentsState(true);
                        log("Run " + run + " of " + selected + " started");
                    }
                } else {
                    // single run
                    simulationTask = EvolutionaryShepherding.runEvolution(resourcesFolderFile.getPath() + File.separator + selected, runNumber);
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
        simulationSection.add(runsCheckbox);
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
        replayRun.setPrompt("Replay run");
        replayGeneration.setPrompt("Replay generation");

        speedSlider.setRange(10, 500);
        speedSlider.setValue(50);

        startReplay.setButtonData("Start replay");
        startReplay.getButtonPressListeners().add(button -> {
            String selectedScenario = (String) availableReplays.getSelectedItem();
            String selectedRun = replayRun.getText().trim();
            String selectedGen = replayGeneration.getText().trim();
            if (selectedScenario == null || selectedScenario.equals("")) {
                Alert.alert(MessageType.WARNING, "Select a replay scenario!", this);
                return;
            } else if (selectedRun.equals("")) {
                Alert.alert(MessageType.WARNING, "Select a replay run!", this);
                return;
            } else if (selectedGen.equals("")) {
                Alert.alert(MessageType.WARNING, "Select a replay generation!", this);
                return;
            }
            try {
                simulationTask = EvolutionaryShepherding.replaySimulation(selectedScenario, selectedRun, selectedGen, speedSlider.getValue());
                simulationTask.execute(new TaskAdapter<>(new TaskListener<Optional<EvaluationResults>>() {
                    @Override
                    public void taskExecuted(Task<Optional<EvaluationResults>> task) {
                        log("Replay of " + selectedScenario + " ended");
                        log(task.getResult().get().toString());
                        setComponentsState(false);
                    }

                    @Override
                    public void executeFailed(Task<Optional<EvaluationResults>> task) {
                        log("Replay of " + selectedScenario + " ended with errors");
                        setComponentsState(false);
                    }
                }));
                setComponentsState(true);
                log("Replay of " + selectedScenario + " started");
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                Alert.alert(MessageType.ERROR, e.getClass().getCanonicalName() + ": " + e.getMessage(), this);
            }
        });

        replaySection.add(replaySectionLabel);
        replaySection.add(availableReplays);
        replaySection.add(replayRun);
        replaySection.add(replayGeneration);
        replaySection.add(speedSlider);
        replaySection.add(startReplay);
    }

    private void initStatsSection() {
        statsSectionLabel.setText("Chart statistics");
        statsSectionLabel.getStyles().put("font", ITALIC_FONT);
        statsSectionLabel.getStyles().put("color", Color.white);

        initAvailableStats();
        statsRun.setPrompt("Chart single run (optional)");

        drawChartsButton.setButtonData("Draw charts");
        drawChartsButton.getButtonPressListeners().add(button -> {
            String selectedScenario = (String) availableStats.getSelectedItem();
            String selectedRun = statsRun.getText();
            if (selectedScenario == null || selectedScenario.equals("")) {
                Alert.alert(MessageType.WARNING, "No stat file selected", this);
            } else {
                try {
                    final String scenarioFilename = new File(EvolutionaryShepherding.STATISTICS_DIR).getPath() + selectedScenario;

                    JFrame runsMeanFrame = new JFrame(selectedScenario);
                    runsMeanFrame.setContentPane(Charts.getMeanSubpopPerGenAcrossRuns(selectedScenario, scenarioFilename, true));
                    runsMeanFrame.setVisible(true);
                    runsMeanFrame.setSize(600, 400);

                    JFrame runsBestSoFarFrame = new JFrame(selectedScenario);
                    runsBestSoFarFrame.setContentPane(Charts.getBestSubpopPerGenAcrossRuns(selectedScenario, scenarioFilename, true));
                    runsBestSoFarFrame.setVisible(true);
                    runsBestSoFarFrame.setSize(600, 400);

                    JFrame runsCorralledEscapedFrame = new JFrame(selectedScenario);
                    runsCorralledEscapedFrame.setContentPane(Charts.getCorralledEscapedAcrossRuns(selectedScenario, scenarioFilename, true));
                    runsCorralledEscapedFrame.setVisible(true);
                    runsCorralledEscapedFrame.setSize(600, 400);

                    if (selectedRun != null && !Objects.equals(selectedRun, "")) {
                        Charts runChart = new Charts(scenarioFilename + File.separator + selectedRun + ".stat");
                        JFrame runFrame = new JFrame(selectedScenario + " - run " + selectedRun);
                        runFrame.setContentPane(runChart.getMeanPerSubpopPerGeneration(selectedScenario));
                        runFrame.setVisible(true);
                        runFrame.setSize(600, 400);
                    }
                } catch (IOException | IllegalAccessException | NoSuchFieldException e) {
                    e.printStackTrace();
                    Alert.alert(MessageType.ERROR, e.getClass().getSimpleName() + ": " + e.getMessage(), this);
                }
            }
        });

        statsSection.add(statsSectionLabel);
        statsSection.add(availableStats);
        statsSection.add(statsRun);
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
