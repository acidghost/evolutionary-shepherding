package nl.vu.ai.aso;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
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
import java.util.stream.Collectors;

/**
 * Created by acidghost on 07/12/15.
 */
public class Charts {

    private static final Paint[] DEFAULT_PALETTE = ChartColor.createDefaultPaintArray();
    private static final Splitter LINE_SPLITTER = Splitter.on(' ').trimResults();
    private final List<StatLine> statLines;

    public Charts(String filename) throws IOException {
        List<String> lines = Files.lines(FileSystems.getDefault().getPath(filename)).collect(Collectors.toList());
        String lastLine = lines.remove(lines.size() - 1);

        statLines = lines.stream().map(StatLine::new).collect(Collectors.toList());
        statLines.stream().forEach(System.out::println);

        System.out.println("Last: " + lastLine);
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

    public static Container getMeanPerGenAcrossRuns(String title, String scenario) throws IOException {
        File scenarioDir = new File(scenario);
        List<Charts> charts = Lists.newArrayList();
        for (File runFile : scenarioDir.listFiles(EvolutionaryShepherding.STATS_FILENAME_FILTER)) {
            charts.add(new Charts(runFile.getPath()));
        }

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

    public static Container getMeanVarPerGenAcrossRuns(String title, String scenario) throws IOException {
        File scenarioDir = new File(scenario);
        List<Charts> charts = Lists.newArrayList();
        for (File runFile : scenarioDir.listFiles(EvolutionaryShepherding.STATS_FILENAME_FILTER)) {
            charts.add(new Charts(runFile.getPath()));
        }

        YIntervalSeriesCollection collection = new YIntervalSeriesCollection();

        for (int subpop = 0; subpop < charts.get(0).statLines.get(0).subpopData.size(); subpop++) {
            YIntervalSeries series = new YIntervalSeries("Subpop " + subpop);
            int totalGenerations = charts.get(0).statLines.size();

            for (int generation = 0; generation < totalGenerations; generation++) {
                SummaryStatistics statistics = new SummaryStatistics();

                for (Charts chart : charts) {
                    statistics.addValue(chart.statLines.get(generation).subpopData.get(subpop).mean);
                }

                final double mean = statistics.getMean();
                final TDistribution tDist = new TDistribution(statistics.getN() - 1);
                final double significance = .95;
                final double criticalValue = tDist.inverseCumulativeProbability(1.0 - significance / 2);
                final double width = criticalValue * statistics.getStandardDeviation() / Math.sqrt(statistics.getN());

                series.add(generation, mean, mean - width, mean + width);
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
        DeviationRenderer deviationrenderer = new DeviationRenderer(true, false);
        for (int i = 0; i < collection.getSeriesCount(); i++) {
            Color paint = (Color) DEFAULT_PALETTE[i];
            deviationrenderer.setSeriesFillPaint(i, paint.darker());
            deviationrenderer.setSeriesStroke(i, new BasicStroke(1F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }
        deviationrenderer.setAlpha(0.2F);
        plot.setRenderer(deviationrenderer);

        return new ChartPanel(chart);
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
        public int corralled;
        public int escaped;
        public int evaluations;
        public double corralledRatio;
        public double escapedRatio;
        public double popMeanFitness;
        public double popBestOfGeneration;
        public double popBestSoFar;

        public StatLine(String line) {
            List<String> splitted = LINE_SPLITTER.splitToList(line);
            generation = Integer.parseInt(splitted.get(0));
            int subpopsEnd = splitted.size() - 9;
            subpopData = SubpopData.getSubpopData(splitted.subList(1, subpopsEnd));
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

    public static void main(String[] args) throws IOException {
        Charts charts = new Charts(EvolutionaryShepherding.STATISTICS_DIR + File.separator + "hetero.2v1" + File.separator + "1.stat");
        JFrame frame = new JFrame("Hetero test");
        frame.setContentPane(charts.getMeanPerSubpopPerGeneration("Heterogeneous"));
        frame.setVisible(true);
        frame.setSize(600, 400);

        JFrame frame2 = new JFrame("Runs demo");
        frame2.setContentPane(Charts.getMeanVarPerGenAcrossRuns("Runs demo", EvolutionaryShepherding.STATISTICS_DIR + File.separator + "hetero.2v1"));
        frame2.setVisible(true);
        frame2.setSize(600, 400);
    }

}
