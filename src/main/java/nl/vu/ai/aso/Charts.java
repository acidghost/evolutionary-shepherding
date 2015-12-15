package nl.vu.ai.aso;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by acidghost on 07/12/15.
 */
public class Charts {

    private static final Paint[] DEFAULT_PALETTE = ChartColor.createDefaultPaintArray();
    private static final Splitter LINE_SPLITTER = Splitter.on(' ').trimResults();
    private static final Joiner DASH_JOINER = Joiner.on('-');
    private static final Map<String, Integer> SPLITS_MAP = Maps.newHashMap();
    private static final Map<String, Range> RANGES_MAP = Maps.newHashMap();

    static {
        SPLITS_MAP.put("hetero.1v1", 1);
        SPLITS_MAP.put("hetero.2v1", 2);
        SPLITS_MAP.put("hetero.3v1", 3);
        SPLITS_MAP.put("hetero.2v2", 2);
        SPLITS_MAP.put("hetero.3v2", 3);
        SPLITS_MAP.put("hetero.3v3", 3);
        SPLITS_MAP.put("homo.2v1", 1);
        SPLITS_MAP.put("homo.3v1", 1);
        SPLITS_MAP.put("homo.2v2", 1);
        SPLITS_MAP.put("homo.3v2", 1);
        SPLITS_MAP.put("homo.3v3", 1);
        SPLITS_MAP.put("hetero.2v2.ho", 2);
        SPLITS_MAP.put("hetero.3v2.ho", 3);
        SPLITS_MAP.put("hetero.3v3.ho", 3);

        RANGES_MAP.put("mean", new Range(-68000.0, 68000.0));
        RANGES_MAP.put("bestSoFar", new Range(-5000.0, 68000.0));
        RANGES_MAP.put("corralledRatio", new Range(0.0, 1.0));
        RANGES_MAP.put("escapedRatio", new Range(0.0, 1.0));
    }

    private final List<StatLine> statLines;

    public Charts(String filename) throws IOException, IllegalArgumentException {
        int split = findSplit(filename);

        List<String> lines = Files.lines(FileSystems.getDefault().getPath(filename)).collect(Collectors.toList());
        lines.remove(lines.size() - 1);

        statLines = lines.stream().map(line -> new StatLine(line, split)).collect(Collectors.toList());
    }

    public Container getMeanPerSubpopPerGeneration(String title) {
        XYSeriesCollection collection = new XYSeriesCollection();
        for (int i = 0; i < statLines.get(0).subpopData.size(); i++) {
            XYSeries series = new XYSeries("Subpop " + i);
            for (StatLine statLine : statLines) {
                series.add(statLine.generation, statLine.subpopData.get(i).mean);
            }
            collection.addSeries(series);
        }

        JFreeChart chart = ChartFactory.createXYLineChart(
            title,  "Generations", "Mean fitness",
            collection, PlotOrientation.VERTICAL,
            true, true, false
        );

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        plot.setRenderer(renderer);

        return new ChartPanel(chart);
    }

    public static Container getMeanSubpopAcrossRuns(String title, String scenario) throws IOException {
        List<Charts> charts = getRunsCharts(scenario);

        XYSeriesCollection collection = new XYSeriesCollection();

        for (int subpop = 0; subpop < charts.get(0).statLines.get(0).subpopData.size(); subpop++) {
            XYSeries series = new XYSeries("Subpop " + subpop);
            int totalGenerations = charts.get(0).statLines.size();

            for (int generation = 0; generation < totalGenerations; generation++) {
                double sum = 0.0;
                for (Charts chart : charts) {
                    sum += chart.statLines.get(generation).subpopData.get(subpop).mean;
                }
                series.add(generation, sum / charts.size());
            }

            collection.addSeries(series);
        }

        JFreeChart chart = ChartFactory.createXYLineChart(
            title + " - " + charts.size() + " runs",
            "Generations", "Mean fitness",
            collection, PlotOrientation.VERTICAL,
            true, true, false
        );

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        plot.setRenderer(renderer);

        return new ChartPanel(chart);
    }

    public static Container getMeanSubpopPerGenAcrossRuns(String title, String scenario, boolean save)
        throws IOException, NoSuchFieldException, IllegalAccessException {

        return getSubpopPerGenAcrossRuns(title, scenario, "mean", "Mean fitness", save);
    }

    public static Container getBestSubpopPerGenAcrossRuns(String title, String scenario, boolean save)
        throws IOException, NoSuchFieldException, IllegalAccessException {

        return getSubpopPerGenAcrossRuns(title, scenario, "bestSoFar", "Best fitness so far", save);
    }

    public static Container getCorralledEscapedAcrossRuns(String title, String scenario, boolean save)
        throws IOException, NoSuchFieldException, IllegalAccessException {

        return getPopPerGenAcrossRuns(title, scenario, "Cases / trials", save, "corralledRatio", "escapedRatio");
    }

    public static Container getAggregatedMeanAcrossRuns(String title, boolean save, String... scenarioFiles)
        throws IllegalAccessException, NoSuchFieldException, IOException {

        return getAggregatedScenarioComparison(title, "mean", "Mean fitness", save, scenarioFiles);
    }

    public static Container getAggregatedBestAcrossRuns(String title, boolean save, String... scenarioFiles)
        throws IllegalAccessException, NoSuchFieldException, IOException {

        return getAggregatedScenarioComparison(title, "bestSoFar", "Best fitness so far", save, scenarioFiles);
    }

    public static Container getCorralledEscapedAcrossRuns(String title, boolean save, String... scenarioFiles)
        throws IllegalAccessException, NoSuchFieldException, IOException {

        return getScenarioComparison(title, new String[] { "corralledRatio", "escapedRatio" }, "Cases / trials", save, scenarioFiles);
    }

    private static int findSplit(String filename) throws IllegalArgumentException {
        for (Map.Entry<String, Integer> entry : SPLITS_MAP.entrySet()) {
            if (filename.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        throw new IllegalArgumentException("Cannot find the subpop split for " + filename + "!");
    }

    private static String findScenario(String filename) throws IllegalArgumentException {
        for (Map.Entry<String, Integer> entry : SPLITS_MAP.entrySet()) {
            if (filename.contains(entry.getKey())) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("Cannot find the subpop scenario for " + filename + "!");
    }

    private static List<Charts> getRunsCharts(String scenario) throws IOException {
        File scenarioDir = new File(scenario);
        List<Charts> charts = Lists.newArrayList();
        for (File runFile : scenarioDir.listFiles(EvolutionaryShepherding.STATS_FILENAME_FILTER)) {
            charts.add(new Charts(runFile.getPath()));
        }
        return charts;
    }

    private static double getConfidenceWidth(SummaryStatistics statistics, double significance) {
        final TDistribution tDist = new TDistribution(statistics.getN() - 1);
        final double criticalValue = tDist.inverseCumulativeProbability(1.0 - significance / 2);
        return criticalValue * statistics.getStandardDeviation() / Math.sqrt(statistics.getN());
    }

    private static YIntervalSeriesCollection getPopCollectionAcrossRuns(List<Charts> charts, String... fields)
        throws IllegalAccessException, NoSuchFieldException {

        YIntervalSeriesCollection collection = new YIntervalSeriesCollection();

        for (String field : fields) {
            YIntervalSeries series = new YIntervalSeries(field);
            int totalGenerations = charts.get(0).statLines.size();

            for (int generation = 0; generation < totalGenerations; generation++) {
                SummaryStatistics statistics = new SummaryStatistics();

                for (Charts chart : charts) {
                    final StatLine statline = chart.statLines.get(generation);
                    statistics.addValue(statline.getClass().getField(field).getDouble(statline));
                }

                final double mean = statistics.getMean();
                final double width = getConfidenceWidth(statistics, .95);

                series.add(generation, mean, mean - width, mean + width);
            }

            collection.addSeries(series);
        }

        return collection;
    }

    private static Container getPopPerGenAcrossRuns(String title, String scenario, String yLabel, boolean save, String... fields)
        throws IOException, NoSuchFieldException, IllegalAccessException {

        List<Charts> charts = getRunsCharts(scenario);
        YIntervalSeriesCollection collection = getPopCollectionAcrossRuns(charts, fields);

        JFreeChart chart = ChartFactory.createXYLineChart(
            title + " - " + charts.size() + " runs",
            "Generations", yLabel,
            collection, PlotOrientation.VERTICAL,
            true, true, false
        );

        XYPlot plot = chart.getXYPlot();
        DeviationRenderer deviationrenderer = new DeviationRenderer(true, false);
        for (int i = 0; i < collection.getSeriesCount(); i++) {
            Color paint = (Color) DEFAULT_PALETTE[i];
            deviationrenderer.setSeriesFillPaint(i, paint.darker());
            deviationrenderer.setSeriesStroke(i, new BasicStroke(1F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }
        deviationrenderer.setAlpha(0.2F);
        plot.setRenderer(deviationrenderer);

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setRange(RANGES_MAP.get(fields[0]));

        if (save) {
            ChartUtilities.saveChartAsJPEG(new File(scenario + File.separator + Joiner.on('-').join(fields) + ".jpeg"), chart, 1024, 800);
        }

        return new ChartPanel(chart);
    }

    private static YIntervalSeriesCollection getSubpopCollectionAcrossRuns(List<Charts> charts, String fieldName)
        throws IllegalAccessException, NoSuchFieldException {

        YIntervalSeriesCollection collection = new YIntervalSeriesCollection();

        for (int subpop = 0; subpop < charts.get(0).statLines.get(0).subpopData.size(); subpop++) {
            YIntervalSeries series = new YIntervalSeries("Subpop " + subpop);
            int totalGenerations = charts.get(0).statLines.size();

            for (int generation = 0; generation < totalGenerations; generation++) {
                SummaryStatistics statistics = new SummaryStatistics();

                for (Charts chart : charts) {
                    final SubpopData subpopData = chart.statLines.get(generation).subpopData.get(subpop);
                    statistics.addValue(subpopData.getClass().getField(fieldName).getDouble(subpopData));
                }

                final double mean = statistics.getMean();
                final double width = getConfidenceWidth(statistics, .95);

                series.add(generation, mean, mean - width, mean + width);
            }

            collection.addSeries(series);
        }

        return collection;
    }

    private static Container getSubpopPerGenAcrossRuns(String title, String scenario, String fieldName, String yLabel, boolean save)
        throws IOException, NoSuchFieldException, IllegalAccessException {

        List<Charts> charts = getRunsCharts(scenario);
        YIntervalSeriesCollection collection = getSubpopCollectionAcrossRuns(charts, fieldName);

        List<String> scenarioNames = Lists.newArrayList();
        scenarioNames.add(findScenario(scenario));
        JFreeChart chart = getDeviationChart(title + " - " + charts.size() + " runs", new String[] { fieldName }, yLabel, save, scenarioNames, collection);

        return new ChartPanel(chart);
    }

    private static YIntervalSeriesCollection mergeCollections(List<YIntervalSeriesCollection> collections, List<String> scenarioNames) {
        YIntervalSeriesCollection collection = new YIntervalSeriesCollection();
        for (int i = 0; i < collections.size(); i++) {
            YIntervalSeriesCollection coll = collections.get(i);
            String scenario = scenarioNames.get(i);
            for (int j = 0; j < coll.getSeriesCount(); j++) {
                final YIntervalSeries series = coll.getSeries(j);
                series.setKey(series.getKey() + " " + scenario);
                collection.addSeries(series);
            }
        }
        return collection;
    }

    private static YIntervalSeriesCollection getAggregatedCollectionAcrossRuns(List<Charts> charts, String fieldName)
        throws IllegalAccessException, NoSuchFieldException {

        YIntervalSeriesCollection collection = new YIntervalSeriesCollection();
        final Field fields[] = { StatLine.class.getField("shepherd"), StatLine.class.getField("sheep") };
        for (Field field : fields) {
            YIntervalSeries series = new YIntervalSeries(field.getName());
            int totalGenerations = charts.get(0).statLines.size();

            for (int generation = 0; generation < totalGenerations; generation++) {
                SummaryStatistics statistics = new SummaryStatistics();

                for (Charts chart : charts) {
                    @SuppressWarnings("unchecked")
                    // get shepherd or sheep data
                    final List<SubpopData> subpopData = (List<SubpopData>) field.get(chart.statLines.get(generation));
                    SummaryStatistics innerStats = new SummaryStatistics();
                    for (SubpopData sd : subpopData) {
                        innerStats.addValue(sd.getClass().getField(fieldName).getDouble(sd));
                    }
                    // store the mean value for this set of agents
                    statistics.addValue(innerStats.getMean());
                }

                final double mean = statistics.getMean();
                final double width = getConfidenceWidth(statistics, .95);

                series.add(generation, mean, mean - width, mean + width);
            }

            collection.addSeries(series);
        }

        return collection;
    }

    private static Container getAggregatedScenarioComparison(String title, String fieldName, String yLabel, boolean save, String... scenarioFiles)
        throws IOException, NoSuchFieldException, IllegalAccessException {

        int runs = 0;
        List<YIntervalSeriesCollection> collections = Lists.newArrayList();
        for (String scenario : scenarioFiles) {
            List<Charts> charts = getRunsCharts(scenario);
            runs = charts.size();
            YIntervalSeriesCollection collection = getAggregatedCollectionAcrossRuns(charts, fieldName);
            collections.add(collection);
        }

        List<String> scenarioNames = Arrays.asList(scenarioFiles).stream().map(Charts::findScenario).collect(Collectors.toList());
        YIntervalSeriesCollection finalCollection = mergeCollections(collections, scenarioNames);

        JFreeChart chart = getDeviationChart(title + " - " + runs + " runs", new String[] { fieldName }, yLabel, save, scenarioNames, finalCollection);

        return new ChartPanel(chart);
    }

    private static Container getScenarioComparison(String title, String[] fieldNames, String yLabel, boolean save, String... scenarioFiles)
        throws IOException, NoSuchFieldException, IllegalAccessException {

        int runs = 0;
        List<YIntervalSeriesCollection> collections = Lists.newArrayList();
        for (String scenario : scenarioFiles) {
            List<Charts> charts = getRunsCharts(scenario);
            runs = charts.size();
            YIntervalSeriesCollection collection = getPopCollectionAcrossRuns(charts, fieldNames);
            collections.add(collection);
        }

        List<String> scenarioNames = Arrays.asList(scenarioFiles).stream().map(Charts::findScenario).collect(Collectors.toList());
        YIntervalSeriesCollection finalCollection = mergeCollections(collections, scenarioNames);

        JFreeChart chart = getDeviationChart(title + " - " + runs + " runs", fieldNames, yLabel, save, scenarioNames, finalCollection);

        return new ChartPanel(chart);
    }

    private static JFreeChart getDeviationChart(String title, String[] fieldNames, String yLabel, boolean save,
                                                List<String> scenarioNames, YIntervalSeriesCollection collection) throws IOException {

        JFreeChart chart = ChartFactory.createXYLineChart(
            title, "Generations", yLabel,
            collection, PlotOrientation.VERTICAL,
            true, true, false
        );

        XYPlot plot = chart.getXYPlot();

        DeviationRenderer deviationrenderer = new DeviationRenderer(true, false);
        for (int i = 0; i < collection.getSeriesCount(); i++) {
            Color paint = (Color) DEFAULT_PALETTE[i];
            deviationrenderer.setSeriesFillPaint(i, paint.darker());
            deviationrenderer.setSeriesStroke(i, new BasicStroke(1F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }
        deviationrenderer.setAlpha(0.2F);
        plot.setRenderer(deviationrenderer);

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setRange(RANGES_MAP.get(fieldNames[0]));

        if (save) {
            File jpgFile;
            final String start = EvolutionaryShepherding.STATISTICS_DIR + File.separator;
            if (scenarioNames.size() > 1) {
                jpgFile = new File(start + DASH_JOINER.join(scenarioNames) + "-" + DASH_JOINER.join(fieldNames) + ".jpeg");
            } else {
                jpgFile = new File(start + scenarioNames.get(0) + File.separator + DASH_JOINER.join(fieldNames) + ".jpeg");
            }
            ChartUtilities.saveChartAsJPEG(jpgFile, chart, 1024, 800);
        }

        return chart;
    }

    private static class SubpopData {
        public double mean;
        public double bestOfGeneration;
        public double bestSoFar;

        public SubpopData(double mean, double bestOfGeneration, double bestSoFar) {
            this.mean = mean;
            this.bestOfGeneration = bestOfGeneration;
            this.bestSoFar = bestSoFar;
        }

        public static List<SubpopData> getSubpopData(List<String> subLine) {
            List<SubpopData> list = Lists.newArrayList();
            for (int i = 0; i < subLine.size(); i += 3) {
                double mean = Double.parseDouble(subLine.get(i));
                double bestOfGeneration = Double.parseDouble(subLine.get(i + 1));
                double bestSoFar = Double.parseDouble(subLine.get(i + 2));
                list.add(new SubpopData(mean, bestOfGeneration, bestSoFar));
            }
            return list;
        }

        @Override
        public String toString() {
            return mean + " " + bestOfGeneration + " " + bestSoFar;
        }
    }

    private class StatLine {
        public int generation;
        public List<SubpopData> subpopData;
        public List<SubpopData> shepherd;
        public List<SubpopData> sheep;
        public int corralled;
        public int escaped;
        public int evaluations;
        public double corralledRatio;
        public double escapedRatio;
        public double popMeanFitness;
        public double popBestOfGeneration;
        public double popBestSoFar;

        public StatLine(String line, int split) {
            List<String> splitted = LINE_SPLITTER.splitToList(line);
            generation = Integer.parseInt(splitted.get(0));

            int subpopsEnd = splitted.size() - 9;
            subpopData = SubpopData.getSubpopData(splitted.subList(1, subpopsEnd));
            shepherd = subpopData.subList(0, split);
            sheep = subpopData.subList(split, subpopData.size());

            corralled = Integer.parseInt(splitted.get(subpopsEnd));
            escaped = Integer.parseInt(splitted.get(subpopsEnd + 1));
            evaluations = Integer.parseInt(splitted.get(subpopsEnd + 2));
            corralledRatio = Double.parseDouble(splitted.get(subpopsEnd + 3));
            escapedRatio = Double.parseDouble(splitted.get(subpopsEnd + 4));
            popMeanFitness = Double.parseDouble(splitted.get(subpopsEnd + 5));
            popBestOfGeneration = Double.parseDouble(splitted.get(subpopsEnd + 6));
            popBestSoFar = Double.parseDouble(splitted.get(subpopsEnd + 7));
        }

        @Override
        public String toString() {
            List<Field> fields = Arrays.asList(getClass().getDeclaredFields());
            return fields.stream().map(field -> {
                try {
                    return field.get(this).toString();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    return "";
                }
            }).collect(Collectors.joining(" "));
        }
    }

    public static void main(String[] args) throws IOException, NoSuchFieldException, IllegalAccessException {
        Charts charts = new Charts(EvolutionaryShepherding.STATISTICS_DIR + File.separator + "hetero.2v1" + File.separator + "1.stat");
        JFrame frame = new JFrame("Hetero test");
        frame.setContentPane(charts.getMeanPerSubpopPerGeneration("Heterogeneous"));
        frame.setVisible(true);
        frame.setSize(600, 400);

        JFrame frame2 = new JFrame("Runs demo");
        frame2.setContentPane(Charts.getMeanSubpopPerGenAcrossRuns("Runs demo", EvolutionaryShepherding.STATISTICS_DIR + File.separator + "hetero.2v1", false));
        frame2.setVisible(true);
        frame2.setSize(600, 400);
    }

}
