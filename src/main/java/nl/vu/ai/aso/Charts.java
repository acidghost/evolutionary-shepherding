package nl.vu.ai.aso;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
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
        Charts charts = new Charts("./hetero.stat");
        JFrame frame = new JFrame("Hetero test");
        frame.setContentPane(charts.getMeanPerSubpopPerGeneration("Heterogeneous"));
        frame.setVisible(true);
        frame.setSize(600, 400);
    }

}