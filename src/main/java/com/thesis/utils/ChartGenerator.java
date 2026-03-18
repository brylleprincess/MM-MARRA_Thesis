package com.thesis.utils;

import com.thesis.metrics.PerformanceMetrics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Princess Brylle N. Tadena
 */



/**
 * Generates PNG chart images and CSV exports.
 * Uses all test cases, generates individual per-test-case bar charts,
 * and generates line charts across all test cases.
 */
public class ChartGenerator {

    private static final String OUTPUT_DIR = "results/graphs/";
    private static final int CHART_WIDTH = 1100;
    private static final int CHART_HEIGHT = 650;


    private static final Color COLOR_TRADITIONAL_RR = new Color(76, 175, 80);   // Green
    private static final Color COLOR_MARR = new Color(33, 150, 243);            // Blue
    private static final Color COLOR_MMRR = new Color(156, 39, 176);            // Purple
    private static final Color COLOR_MMARRA = new Color(255, 87, 34);           // Orange

    public static void generateAllCharts(List<PerformanceMetrics> metricsList) {
        new File(OUTPUT_DIR).mkdirs();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("GENERATING AGGREGATED CHART IMAGES");
        System.out.println("=".repeat(80));

        try {
            generateMakespanChart(metricsList);
            generateThroughputChart(metricsList);
            generateTurnaroundChart(metricsList);
            generateLoadBalanceChart(metricsList);
            generateFairnessChart(metricsList);
            generateComprehensiveChart(metricsList);
            generateResourceUtilizationChart(metricsList);

            System.out.println("\n✓ All aggregated chart images generated in: " + OUTPUT_DIR);

        } catch (IOException e) {
            System.err.println("Error generating charts: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=".repeat(80));
    }

    public static void exportDetailedCsv(List<PerformanceMetrics> metricsList,
                                         List<int[]> testCases,
                                         String selectedAlgorithm,
                                         String outputPath) throws IOException {

        int algorithmsPerCase = "Run All Algorithms".equals(selectedAlgorithm) ? 4 : 1;
        List<List<PerformanceMetrics>> perCaseMetrics =
                splitMetricsByCase(metricsList, testCases.size(), algorithmsPerCase);

        File outFile = new File(outputPath);
        File parent = outFile.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }

        try (PrintWriter writer = new PrintWriter(outFile)) {
            writer.println("TestCase,Hosts,VMs,Cloudlets,Algorithm,Makespan,AvgWaitingTime,AvgTurnaroundTime,AvgResponseTime,Throughput,CPUUtil,MemUtil,BWUtil,LoadVariance,FairnessIndex,OverloadCount");

            for (int i = 0; i < testCases.size(); i++) {
                int[] tc = testCases.get(i);
                List<PerformanceMetrics> caseMetrics =
                        i < perCaseMetrics.size() ? sortMetrics(perCaseMetrics.get(i)) : new ArrayList<>();

                for (PerformanceMetrics m : caseMetrics) {
                    writer.printf(
                            "%d,%d,%d,%d,%s,%.2f,%.2f,%.2f,%.2f,%.4f,%.2f,%.2f,%.2f,%.6f,%.4f,%d%n",
                            i + 1,
                            tc[0],
                            tc[1],
                            tc[2],
                            escapeCsv(m.getAlgorithmName()),
                            m.getMakespan(),
                            m.getAvgWaitingTime(),
                            m.getAvgTurnaroundTime(),
                            m.getAvgResponseTime(),
                            m.getThroughput(),
                            m.getCpuUtilization(),
                            m.getMemoryUtilization(),
                            m.getBwUtilization(),
                            m.getLoadBalanceVariance(),
                            m.getFairnessIndex(),
                            m.getServerOverloadCount()
                    );
                }
            }
        }
    }

    public static void generatePerTestCaseCharts(List<PerformanceMetrics> metricsList,
                                                 List<int[]> testCases,
                                                 String selectedAlgorithm) throws IOException {
        new File(OUTPUT_DIR).mkdirs();

        int algorithmsPerCase = "Run All Algorithms".equals(selectedAlgorithm) ? 4 : 1;
        List<List<PerformanceMetrics>> perCaseMetrics =
                splitMetricsByCase(metricsList, testCases.size(), algorithmsPerCase);

        for (int i = 0; i < testCases.size(); i++) {
            List<PerformanceMetrics> caseMetrics =
                    i < perCaseMetrics.size() ? sortMetrics(perCaseMetrics.get(i)) : new ArrayList<>();

            if (caseMetrics.isEmpty()) {
                continue;
            }

            int[] testCase = testCases.get(i);

            generateSingleTestCaseMetricChart(caseMetrics, testCase, i + 1, "makespan");
            generateSingleTestCaseMetricChart(caseMetrics, testCase, i + 1, "throughput");
            generateSingleTestCaseMetricChart(caseMetrics, testCase, i + 1, "turnaround");
            generateSingleTestCaseMetricChart(caseMetrics, testCase, i + 1, "load");
            generateSingleTestCaseMetricChart(caseMetrics, testCase, i + 1, "fairness");
            generateSingleTestCaseMetricChart(caseMetrics, testCase, i + 1, "overloaded");
            generateSingleTestCaseUtilizationChart(caseMetrics, testCase, i + 1);
            generateSingleTestCaseComprehensiveChart(caseMetrics, testCase, i + 1);
        }

        generateResourceUtilizationLineChart(perCaseMetrics);
        generateComprehensivePerformanceLineChart(perCaseMetrics);
    }

    private static void generateMakespanChart(List<PerformanceMetrics> metricsList) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (PerformanceMetrics m : metricsList) {
            dataset.addValue(m.getMakespan(), "Makespan", m.getAlgorithmName());
        }

        JFreeChart chart = createBarChart(
                "Makespan Comparison (Lower is Better)",
                "Algorithm",
                "Makespan (seconds)",
                dataset
        );

        saveChart(chart, "1_makespan.png");
    }

    private static void generateThroughputChart(List<PerformanceMetrics> metricsList) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (PerformanceMetrics m : metricsList) {
            dataset.addValue(m.getThroughput(), "Throughput", m.getAlgorithmName());
        }

        JFreeChart chart = createBarChart(
                "Throughput Comparison (Higher is Better)",
                "Algorithm",
                "Throughput (tasks/second)",
                dataset
        );

        saveChart(chart, "2_throughput.png");
    }

    private static void generateTurnaroundChart(List<PerformanceMetrics> metricsList) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (PerformanceMetrics m : metricsList) {
            dataset.addValue(m.getAvgTurnaroundTime(), "Avg Turnaround", m.getAlgorithmName());
        }

        JFreeChart chart = createBarChart(
                "Average Turnaround Time Comparison (Lower is Better)",
                "Algorithm",
                "Turnaround Time (seconds)",
                dataset
        );

        saveChart(chart, "3_turnaround_time.png");
    }

    private static void generateLoadBalanceChart(List<PerformanceMetrics> metricsList) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (PerformanceMetrics m : metricsList) {
            dataset.addValue(m.getLoadBalanceVariance(), "Load Variance", m.getAlgorithmName());
        }

        JFreeChart chart = createBarChart(
                "Load Balance Variance Comparison (Lower is Better)",
                "Algorithm",
                "Variance",
                dataset
        );

        saveChart(chart, "4_load_balance_variance.png");
    }

    private static void generateFairnessChart(List<PerformanceMetrics> metricsList) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (PerformanceMetrics m : metricsList) {
            dataset.addValue(m.getFairnessIndex(), "Fairness Index", m.getAlgorithmName());
        }

        JFreeChart chart = createBarChart(
                "Fairness Index Comparison (Higher is Better, Max = 1.0)",
                "Algorithm",
                "Fairness Index",
                dataset
        );

        saveChart(chart, "5_fairness_index.png");
    }

    private static void generateComprehensiveChart(List<PerformanceMetrics> metricsList) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        if (metricsList == null || metricsList.isEmpty()) {
            return;
        }

        for (PerformanceMetrics m : metricsList) {
            dataset.addValue(m.getMakespan(), "Makespan", m.getAlgorithmName());
            dataset.addValue(m.getThroughput(), "Throughput", m.getAlgorithmName());
            dataset.addValue(m.getAvgTurnaroundTime(), "Avg Turnaround", m.getAlgorithmName());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Comprehensive Performance Comparison",
                "Algorithm",
                "Raw Metric Value",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        styleComprehensiveBarChart(chart);
        saveChart(chart, "6_comprehensive_comparison.png");
    }

    private static void generateResourceUtilizationChart(List<PerformanceMetrics> metricsList) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (PerformanceMetrics m : metricsList) {
            dataset.addValue(m.getCpuUtilization(), "CPU", m.getAlgorithmName());
            dataset.addValue(m.getMemoryUtilization(), "Memory", m.getAlgorithmName());
            dataset.addValue(m.getBwUtilization(), "Bandwidth", m.getAlgorithmName());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Resource Utilization Comparison (Higher is Better)",
                "Algorithm",
                "Utilization (%)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        styleBarChart(chart);
        saveChart(chart, "7_resource_utilization.png");
    }

    /**
     * Per test case metric bar chart.
     */
    private static void generateSingleTestCaseMetricChart(List<PerformanceMetrics> caseMetrics,
                                                          int[] testCase,
                                                          int testCaseNumber,
                                                          String metricType) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String categoryLabel = metricDisplayName(metricType);

        for (PerformanceMetrics m : caseMetrics) {
            dataset.addValue(
                    getMetricValue(m, metricType),
                    m.getAlgorithmName(),
                    categoryLabel
            );
        }

        String title = buildTestCaseTitle(testCase, testCaseNumber);

        JFreeChart chart = ChartFactory.createBarChart(
                title,
                "Metric",
                metricAxisLabel(metricType),
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        styleBarChart(chart);
        saveChart(chart, "test_case_" + testCaseNumber + "_" + metricType + ".png");
    }

    /**
     * Per test case utilization bar chart.
     */
    private static void generateSingleTestCaseUtilizationChart(List<PerformanceMetrics> caseMetrics,
                                                               int[] testCase,
                                                               int testCaseNumber) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (PerformanceMetrics m : caseMetrics) {
            dataset.addValue(m.getCpuUtilization(), m.getAlgorithmName(), "CPU");
            dataset.addValue(m.getMemoryUtilization(), m.getAlgorithmName(), "Memory");
            dataset.addValue(m.getBwUtilization(), m.getAlgorithmName(), "Bandwidth");
        }

        String title = buildTestCaseTitle(testCase, testCaseNumber);

        JFreeChart chart = ChartFactory.createBarChart(
                title,
                "Resource",
                "Utilization (%)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        styleBarChart(chart);
        saveChart(chart, "test_case_" + testCaseNumber + "_utilization.png");
    }

    private static void generateSingleTestCaseComprehensiveChart(List<PerformanceMetrics> caseMetrics,
                                                                 int[] testCase,
                                                                 int testCaseNumber) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (PerformanceMetrics m : caseMetrics) {
            dataset.addValue(m.getMakespan(), "Makespan", m.getAlgorithmName());
            dataset.addValue(m.getThroughput(), "Throughput", m.getAlgorithmName());
            dataset.addValue(m.getAvgTurnaroundTime(), "Avg Turnaround", m.getAlgorithmName());
        }

        String title = buildTestCaseTitle(testCase, testCaseNumber);

        JFreeChart chart = ChartFactory.createBarChart(
                title,
                "Algorithm",
                "Raw Metric Value",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        styleComprehensiveBarChart(chart);
        saveChart(chart, "test_case_" + testCaseNumber + "_comprehensive.png");
    }

    /**
     * Line chart across all test cases for resource utilization.
     */
    private static void generateResourceUtilizationLineChart(List<List<PerformanceMetrics>> perCaseMetrics) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < perCaseMetrics.size(); i++) {
            List<PerformanceMetrics> caseMetrics = sortMetrics(perCaseMetrics.get(i));
            String testCaseLabel = "TC-" + (i + 1);

            for (PerformanceMetrics m : caseMetrics) {
                dataset.addValue(m.getCpuUtilization(), m.getAlgorithmName() + " - CPU", testCaseLabel);
                dataset.addValue(m.getMemoryUtilization(), m.getAlgorithmName() + " - Memory", testCaseLabel);
                dataset.addValue(m.getBwUtilization(), m.getAlgorithmName() + " - Bandwidth", testCaseLabel);
            }
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Resource Utilization Across All Test Cases",
                "Test Case",
                "Utilization (%)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        styleLineChart(chart);
        saveChart(chart, "8_resource_utilization_line.png");
    }

    private static void generateComprehensivePerformanceLineChart(List<List<PerformanceMetrics>> perCaseMetrics) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < perCaseMetrics.size(); i++) {
            List<PerformanceMetrics> caseMetrics = sortMetrics(perCaseMetrics.get(i));
            String testCaseLabel = "TC-" + (i + 1);

            for (PerformanceMetrics m : caseMetrics) {
                dataset.addValue(m.getMakespan(), m.getAlgorithmName() + " - Makespan", testCaseLabel);
                dataset.addValue(m.getThroughput(), m.getAlgorithmName() + " - Throughput", testCaseLabel);
                dataset.addValue(m.getAvgTurnaroundTime(), m.getAlgorithmName() + " - Avg Turnaround", testCaseLabel);
            }
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Comprehensive Performance Across All Test Cases",
                "Test Case",
                "Raw Metric Value",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        styleLineChart(chart);
        saveChart(chart, "9_comprehensive_performance_line.png");
    }

    private static String buildTestCaseTitle(int[] testCase, int testCaseNumber) {
        return "Test Case " + testCaseNumber +
                " (Cloudlets: " + testCase[2] +
                ", Hosts: " + testCase[0] +
                ", VMs: " + testCase[1] + ")";
    }

    private static String metricDisplayName(String metricType) {
        switch (metricType) {
            case "makespan":
                return "Makespan";
            case "throughput":
                return "Throughput";
            case "turnaround":
                return "Turnaround";
            case "load":
                return "Load Variance";
            case "fairness":
                return "Fairness";
            case "overloaded":
                return "Overloaded VMs";
            default:
                return "Metric";
        }
    }

    private static String metricAxisLabel(String metricType) {
        switch (metricType) {
            case "makespan":
                return "Makespan (seconds)";
            case "throughput":
                return "Throughput (tasks/sec)";
            case "turnaround":
                return "Turnaround Time (seconds)";
            case "load":
                return "Variance";
            case "fairness":
                return "Fairness Index";
            case "overloaded":
                return "Count";
            default:
                return "Value";
        }
    }

    private static JFreeChart createBarChart(String title, String xLabel, String yLabel,
                                             DefaultCategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createBarChart(
                title,
                xLabel,
                yLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        styleBarChart(chart);
        return chart;
    }

    /**
     * For per-test-case charts:
     */
    private static void styleBarChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);

        TextTitle title = chart.getTitle();
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setPaint(new Color(51, 51, 51));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.setOutlinePaint(null);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setDrawBarOutline(false);

        int seriesCount = plot.getDataset().getRowCount();
        Color[] colors = {
                COLOR_TRADITIONAL_RR,
                COLOR_MARR,
                COLOR_MMRR,
                COLOR_MMARRA
        };

        for (int i = 0; i < seriesCount; i++) {
            renderer.setSeriesPaint(i, colors[i % colors.length]);
        }

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 14));
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 14));
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());

        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(new Font("SansSerif", Font.BOLD, 11));
    }

    /**
     * For comprehensive charts:
     */
    private static void styleComprehensiveBarChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);

        TextTitle title = chart.getTitle();
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setPaint(new Color(51, 51, 51));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.setOutlinePaint(null);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setDrawBarOutline(false);

        renderer.setSeriesPaint(0, new Color(76, 175, 80));   // Makespan
        renderer.setSeriesPaint(1, new Color(33, 150, 243));  // Throughput
        renderer.setSeriesPaint(2, new Color(156, 39, 176));  // Avg Turnaround

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 14));
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 14));
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());

        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(new Font("SansSerif", Font.BOLD, 11));
    }

    /**
     * For line charts across all test cases
     */
    private static void styleLineChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);

        TextTitle title = chart.getTitle();
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setPaint(new Color(51, 51, 51));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.setOutlinePaint(null);

        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultShapesFilled(true);
        renderer.setDefaultItemLabelsVisible(false);

        int seriesCount = plot.getDataset().getRowCount();
        for (int i = 0; i < seriesCount; i++) {
            String seriesName = plot.getDataset().getRowKey(i).toString();

            if (seriesName.startsWith("Traditional RR")) {
                renderer.setSeriesPaint(i, COLOR_TRADITIONAL_RR);
            } else if (seriesName.startsWith("MARR")) {
                renderer.setSeriesPaint(i, COLOR_MARR);
            } else if (seriesName.startsWith("MMRR")) {
                renderer.setSeriesPaint(i, COLOR_MMRR);
            } else if (seriesName.startsWith("MMARRA")) {
                renderer.setSeriesPaint(i, COLOR_MMARRA);
            } else {
                renderer.setSeriesPaint(i, Color.GRAY);
            }

            renderer.setSeriesStroke(i, new BasicStroke(2.0f));
        }

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 14));
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 14));
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
    }

    private static double getMetricValue(PerformanceMetrics m, String metricType) {
        switch (metricType) {
            case "makespan":
                return m.getMakespan();
            case "throughput":
                return m.getThroughput();
            case "turnaround":
                return m.getAvgTurnaroundTime();
            case "load":
                return m.getLoadBalanceVariance();
            case "fairness":
                return m.getFairnessIndex();
            case "overloaded":
                return m.getServerOverloadCount();
            default:
                return 0.0;
        }
    }

    private static List<List<PerformanceMetrics>> splitMetricsByCase(List<PerformanceMetrics> source,
                                                                     int testCaseCount,
                                                                     int algorithmsPerCase) {
        List<List<PerformanceMetrics>> result = new ArrayList<>();
        int index = 0;

        for (int i = 0; i < testCaseCount; i++) {
            List<PerformanceMetrics> oneCase = new ArrayList<>();

            for (int j = 0; j < algorithmsPerCase && index < source.size(); j++) {
                oneCase.add(source.get(index));
                index++;
            }

            result.add(oneCase);
        }

        return result;
    }

    private static List<PerformanceMetrics> sortMetrics(List<PerformanceMetrics> metricsList) {
        List<PerformanceMetrics> ordered = new ArrayList<>();

        addIfExists(ordered, metricsList, "Traditional RR");
        addIfExists(ordered, metricsList, "MARR");
        addIfExists(ordered, metricsList, "MMRR");
        addIfExists(ordered, metricsList, "MMARRA");

        if (!ordered.isEmpty()) {
            return ordered;
        }

        return metricsList;
    }

    private static void addIfExists(List<PerformanceMetrics> ordered,
                                    List<PerformanceMetrics> source,
                                    String algorithmName) {
        for (PerformanceMetrics metric : source) {
            if (metric.getAlgorithmName().equals(algorithmName)) {
                ordered.add(metric);
                return;
            }
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static double safeDivide(double numerator, double denominator) {
        return denominator == 0 ? 0 : numerator / denominator;
    }

    private static void saveChart(JFreeChart chart, String filename) throws IOException {
        File outputFile = new File(OUTPUT_DIR + filename);
        ChartUtils.saveChartAsPNG(outputFile, chart, CHART_WIDTH, CHART_HEIGHT);
    }
}