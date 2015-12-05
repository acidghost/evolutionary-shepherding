package nl.vu.ai.aso;

import nl.vu.ai.aso.shared.EvaluationResults;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.Window;

import java.awt.*;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Created by acidghost on 05/12/15.
 */
public class EvolutionaryShepherdingGUI extends Window implements Application {

    private static final Color BG_COLOR = Color.darkGray;
    private static final Dimensions DIMENSION = new Dimensions(800, 300);
    private static final String TITLE = "Evolutionary Shepherding";
    private static final int LOG_LENGTH = 10;

    private SplitPane mainPanel = new SplitPane();
    private FlowPane leftPanel = new FlowPane();
    private FlowPane rightPanel = new FlowPane();
    private Form form = new Form();
    private Form.Section simulationSection = new Form.Section();
    private ListButton availableScenarios = new ListButton();
    private PushButton startButton = new PushButton();
    private PushButton stopButton = new PushButton();
    private Form.Section replaySection = new Form.Section();
    private ListButton availableReplays = new ListButton();
    private PushButton startReplay = new PushButton();
    private ListView logView = new ListView();

    private File resourcesFolderFile;
    private Task<EvaluationResults> simulationTask;

    public EvolutionaryShepherdingGUI() {
        super();

        initAvailableScenarios();
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
        for (File file : serialized.listFiles(EvolutionaryShepherding.SERIALIZED_FILENAME_FILTER)) {
            listData.add(file.getPath().split(serialized.getPath())[1]);
        }
        availableReplays.setListData(listData);
    }

    private void initLeftPanel() {
        startButton.setButtonData("Start simulation");
        startButton.setEnabled(true);
        startButton.getButtonPressListeners().add(button -> {
            String selected = (String) availableScenarios.getSelectedItem();
            if (selected == null || selected.equals("")) {
                Alert.alert(MessageType.WARNING, "No scenario selected!", this);
            } else {
                simulationTask = EvolutionaryShepherding.runEvolution(resourcesFolderFile.getPath() + "/" + selected);
                simulationTask.execute(new TaskListener<EvaluationResults>() {
                    @Override
                    public void taskExecuted(Task<EvaluationResults> task) {
                        log("Simulation " + selected + " ended\n" + task.getResult().toString());
                        setComponentsState(false);
                    }

                    @Override
                    public void executeFailed(Task<EvaluationResults> task) {
                        log("Simulation " + selected + " ended with errors");
                        setComponentsState(false);
                    }
                });
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

        simulationSection.add(availableScenarios);
        simulationSection.add(startButton);
        simulationSection.add(stopButton);

        initAvailableReplays();
        startReplay.setButtonData("Start replay");
        startReplay.getButtonPressListeners().add(button -> {
            String selected = (String) availableReplays.getSelectedItem();
            String filename = new File(EvolutionaryShepherding.SERIALIZED_DIR).getPath() + selected;
            try {
                simulationTask = EvolutionaryShepherding.replaySimulation(filename);
                simulationTask.execute(new TaskListener<EvaluationResults>() {
                    @Override
                    public void taskExecuted(Task<EvaluationResults> task) {
                        log("Replay of " + selected + " ended\n" + task.getResult().toString());
                        setComponentsState(false);
                    }

                    @Override
                    public void executeFailed(Task<EvaluationResults> task) {
                        log("Replay of " + selected + " ended with errors");
                        setComponentsState(false);
                    }
                });
                setComponentsState(true);
                log("Replay of " + selected + " started");
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                Alert.alert(MessageType.ERROR, e.getClass().getCanonicalName() + ": " + e.getMessage(), this);
            }
        });

        replaySection.add(availableReplays);
        replaySection.add(startReplay);

        Form.SectionSequence formSections = form.getSections();
        formSections.add(simulationSection);
        formSections.add(replaySection);
        form.getStyles().put("backgroundColor", BG_COLOR);

        leftPanel.add(form);
        leftPanel.getStyles().put("backgroundColor", BG_COLOR);
        mainPanel.setLeft(leftPanel);
    }

    private void initRightPanel() {
        StyleDictionary logViewStyles = logView.getStyles();
        logViewStyles.put("backgroundColor", Color.black);
        logViewStyles.put("color", Color.green);

        rightPanel.add(logView);
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
