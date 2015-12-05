package nl.vu.ai.aso;

import com.google.common.io.PatternFilenameFilter;
import ec.Evolve;
import nl.vu.ai.aso.evolution.HerdingProblem;
import nl.vu.ai.aso.shared.EvaluationResults;
import nl.vu.ai.aso.shared.Replay;
import nl.vu.ai.aso.simulation.HerdingGUI;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by acidghost on 24/11/15.
 */
public class EvolutionaryShepherding {

    public static final String SERIALIZED_DIR = "serialized";
    public static final PatternFilenameFilter PARAMS_FILENAME_FILTER = new PatternFilenameFilter(".*\\.params");
    public static final PatternFilenameFilter SERIALIZED_FILENAME_FILTER = new PatternFilenameFilter(".*\\.ser");

    public static void clearSerialized() {
        File serialized = new File(SERIALIZED_DIR);
        File[] files = serialized.listFiles(SERIALIZED_FILENAME_FILTER);
        for (int i = 0; i < (files != null ? files.length : 0); i++) {
            File file = files[i];
            if (!file.delete()) {
                throw new RuntimeException("Unable to delete file " + file.getPath());
            }
        }
    }

    public static Task runEvolution(String file, String statFile) {
        return new Task() {
            @Override
            public Object execute() throws TaskExecutionException {
                HerdingProblem.evaluationCounter = 0;
                clearSerialized();
                ExitManager exitManager = ExitManager.disableSystemExit();
                Evolve.main(new String[] { "-file", file, "-p", "stat.file=$" + statFile });
                exitManager.enableSystemExit();
                return null;
            }
        };
    }

    public static Task<EvaluationResults> replaySimulation(String filename, int speed) throws IOException, ClassNotFoundException {
        FileInputStream inputFileStream = new FileInputStream(filename);
        ObjectInputStream objectInputStream = new ObjectInputStream(inputFileStream);
        Replay replay = (Replay) objectInputStream.readObject();
        objectInputStream.close();
        inputFileStream.close();

        return new Task<EvaluationResults>() {
            @Override
            public EvaluationResults execute() throws TaskExecutionException {
                List<double[]> individuals = replay.getBestGenomesOfGeneration();
                List<double[]> shepherd;
                List<double[]> sheep;
                EvaluationResults results = null;
                switch (replay.getEvolutionType()) {
                    case HETERO:
                        shepherd = individuals.subList(0, replay.getSplit());
                        sheep = individuals.subList(replay.getSplit(), individuals.size());
                        results = HerdingGUI.runSimulation(replay.getTotalSteps(), speed, shepherd, sheep, false);
                        break;
                    case HOMO:
                        shepherd = new ArrayList<>();
                        for (int i = 0; i < replay.getNumShepherd(); i++) {
                            shepherd.add(individuals.get(0));
                        }
                        sheep = new ArrayList<>();
                        for (int i = 0; i < replay.getNumSheep(); i++) {
                            sheep.add(individuals.get(1));
                        }
                        results = HerdingGUI.runSimulation(replay.getTotalSteps(), speed, shepherd, sheep, false);
                        break;
                }
                return results;
            }
        };
    }

    private static class ExitManager extends SecurityManager {
        private SecurityManager original;

        public ExitManager(SecurityManager original) {
            this.original = original;
        }

        public SecurityManager getOriginal() {
            return original;
        }

        public static ExitManager disableSystemExit() {
            ExitManager exitManager = new ExitManager(System.getSecurityManager());
            System.setSecurityManager(exitManager);
            return exitManager;
        }

        public void enableSystemExit() {
            System.setSecurityManager(original);
        }

        @Override
        public void checkExit(int i) {
            throw new SecurityException();
        }

        @Override
        public void checkPermission(Permission permission) {}
    }

    public static void main(String[] args) {
        String nSheep = "one";
        String nSheph = "one";

        if (args.length == 2) {
            nSheph = args[0];
            nSheep = args[1];
        }

        clearSerialized();

        String filename = EvolutionaryShepherding.class.getClassLoader().getResource("ecj." + nSheph + ".shep." + nSheep + ".sheep.params").getPath();
        Evolve.main(new String[] { "-file", filename });
    }

}
