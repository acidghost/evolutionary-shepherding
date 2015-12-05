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
import java.util.List;

/**
 * Created by acidghost on 24/11/15.
 */
public class EvolutionaryShepherding {

    public static final String SERIALIZED_DIR = "serialized";
    static final PatternFilenameFilter PARAMS_FILENAME_FILTER = new PatternFilenameFilter(".*\\.params");
    static final PatternFilenameFilter SERIALIZED_FILENAME_FILTER = new PatternFilenameFilter(".*\\.ser");

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
                Evolve.main(new String[] { "-file", file, "-p", "stat.file=" + statFile });
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

        List<double[]> individuals = replay.getBestGenomesOfGeneration();
        List<double[]> shepherd = individuals.subList(0, replay.getSplit());
        List<double[]> sheep = individuals.subList(replay.getSplit(), individuals.size());

        return new Task<EvaluationResults>() {
            @Override
            public EvaluationResults execute() throws TaskExecutionException {
                return HerdingGUI.runSimulation(replay.getTotalSteps(), speed, shepherd, sheep, false);
            }
        };
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
