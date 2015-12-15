package nl.vu.ai.aso;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.PatternFilenameFilter;
import ec.Evolve;
import nl.vu.ai.aso.evolution.HerdingProblem;
import nl.vu.ai.aso.shared.EvaluationResults;
import nl.vu.ai.aso.shared.Replay;
import nl.vu.ai.aso.simulation.HerdingGUI;
import nl.vu.ai.aso.simulation.agents.Sheep;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by acidghost on 24/11/15.
 */
public class EvolutionaryShepherding {

    public static final String STATISTICS_DIR = "statistics";
    public static final String SERIALIZED_DIR = "serialized";
    public static final PatternFilenameFilter PARAMS_FILENAME_FILTER = new PatternFilenameFilter(".*\\.params");
    public static final PatternFilenameFilter SERIALIZED_FILENAME_FILTER = new PatternFilenameFilter(".*\\.ser");
    public static final PatternFilenameFilter STATS_FILENAME_FILTER = new PatternFilenameFilter(".*\\.stat");

    public static void clearSerialized(String folder) {
        File serialized = new File(SERIALIZED_DIR + File.separator + folder);
        System.out.println("Cleaning " + serialized.getPath() + " folder...");
        File[] files = serialized.listFiles(SERIALIZED_FILENAME_FILTER);
        for (int i = 0; i < (files != null ? files.length : 0); i++) {
            File file = files[i];
            if (!file.delete()) {
                throw new RuntimeException("Unable to delete file " + file.getPath());
            }
        }
    }

    public static Task<Optional<EvaluationResults>> runEvolution(String file, String runNumber) {
        return new Task<Optional<EvaluationResults>>() {
            @Override
            public Optional<EvaluationResults> execute() throws TaskExecutionException {
                HerdingProblem.evaluationCounter = 0;

                final String[] splitFilename = file.split(".params")[0].split(File.separator);
                final String paramFilename = splitFilename[splitFilename.length - 1];
                clearSerialized(paramFilename + File.separator + runNumber);

                final String statFile = STATISTICS_DIR + File.separator + paramFilename + File.separator + runNumber + ".stat";
                try {
                    Files.createParentDirs(new File(statFile));
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Error creating stat file!");
                    return Optional.absent();
                }

                ExitManager exitManager = ExitManager.disableSystemExit();
                Evolve.main(new String[] {
                    "-file", file,
                    "-p", HerdingProblem.STAT_FILE + "=$" + statFile,
                    "-p", HerdingProblem.EVO_FILE + "=" + file,
                    "-p", HerdingProblem.EVO_RUN + "=" + runNumber
                });
                exitManager.enableSystemExit();
                return Optional.absent();
            }
        };
    }

    public static Task<Optional<EvaluationResults>> replaySimulation(String scenario, String run, String generation, int speed)
        throws IOException, ClassNotFoundException {

        String filename = new File(SERIALIZED_DIR).getPath() + File.separator + scenario + File.separator + run + File.separator + "best." + generation + ".ser";

        FileInputStream inputFileStream = new FileInputStream(filename);
        ObjectInputStream objectInputStream = new ObjectInputStream(inputFileStream);
        Replay replay = (Replay) objectInputStream.readObject();
        objectInputStream.close();
        inputFileStream.close();

        return new Task<Optional<EvaluationResults>>() {
            @Override
            public Optional<EvaluationResults> execute() throws TaskExecutionException {
                List<double[]> individuals = replay.getBestGenomesOfGeneration();
                List<double[]> shepherd;
                List<double[]> sheep;
                EvaluationResults results = null;
                switch (replay.getEvolutionType()) {
                    case HETERO:
                        shepherd = individuals.subList(0, replay.getSplit());

                        // set sheep for homo & hetero cases
                        if (individuals.size() - replay.getSplit() != replay.getNumSheep()) {
                            sheep = Lists.newArrayList();
                            double[] sheepClone = individuals.get(individuals.size() - 1);
                            for (int i = 0; i < replay.getNumSheep(); i++) {
                                sheep.add(sheepClone);
                            }
                        } else {
                            sheep = individuals.subList(replay.getSplit(), individuals.size());
                        }

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
                return Optional.of(results);
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
        String nSheph = "3";
        String nSheep = "1";

        if (args.length == 2) {
            nSheph = args[0];
            nSheep = args[1];
        }

        clearSerialized("homo." + nSheph + "v" + nSheep + File.separator + "0");

        final URL resource = EvolutionaryShepherding.class.getClassLoader().getResource("homo." + nSheph + "v" + nSheep + ".params");
        assert resource != null;
        String filename = resource.getPath();
        Evolve.main(new String[] {
            "-file", filename,
            "-p", HerdingProblem.EVO_FILE + "=" + filename,
            "-p", HerdingProblem.EVO_RUN + "=" + 0,
            "-p", HerdingProblem.STAT_FILE + "=$" + STATISTICS_DIR + File.separator + "homo" + "." + nSheph + "v" + nSheep + File.separator + "0.stat"
        });
    }

}
