package com.thesis.utils;

import com.thesis.metrics.PerformanceMetrics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Generates PNG chart images for thesis visualization
 * Uses JFreeChart library for professional-quality bar charts
 *
 * Required Maven dependency:
 * <dependency>
 *     <groupId>org.jfree</groupId>
 *     <artifactId>jfreechart</artifactId>
 *     <version>1.5.4</version>
 * </dependency>
 */
public class ChartGenerator {

    private static final String OUTPUT_DIR = "results/graphs/";
    private static final int CHART_WIDTH = 900;
    private static final int CHART_HEIGHT = 600;

    // Color scheme for algorithms
    private static final Color COLOR_TRADITIONAL_RR = new Color(76, 175, 80);   // Green
    private static final Color COLOR_MARR = new Color(33, 150, 243);            // Blue
    private static final Color COLOR_MMRR = new Color(156, 39, 176);            // Purple
    private static final Color COLOR_MM_MARRA = new Color(255, 87, 34);         // Orange

    /**
     * Generates all chart images from collected metrics
     */
    public static void generateAllCharts(List<PerformanceMetrics> metricsList) {
        new File(OUTPUT_DIR).mkdirs();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("GENERATING CHART IMAGES");
        System.out.println("=".repeat(80));

        try {
            // 1. Makespan Comparison Chart
            generateMakespanChart(metricsList);

            // 2. Throughput Comparison Chart
            generateThroughputChart(metricsList);

            // 3. Turnaround Time Chart
            generateTurnaroundChart(metricsList);

            // 4. Load Balance Variance Chart
            generateLoadBalanceChart(metricsList);

            // 5. Fairness Index Chart
            generateFairnessChart(metricsList);

            // 6. Comprehensive Comparison Chart
            generateComprehensiveChart(metricsList);

            // 7. Resource Utilization Chart
            generateResourceUtilizationChart(metricsList);

            System.out.println("\n✓ All chart images generated in: " + OUTPUT_DIR);

        } catch (IOException e) {
            System.err.println("Error generating charts: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=".repeat(80));
    }

    /**
     * Generates Makespan comparison bar chart
     */
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
        System.out.println("  ✓ Generated: 1_makespan.png");
    }

    /**
     * Generates Throughput comparison bar chart
     */
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
        System.out.println("  ✓ Generated: 2_throughput.png");
    }

    /**
     * Generates Average Turnaround Time comparison bar chart
     */
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
        System.out.println("  ✓ Generated: 3_turnaround_time.png");
    }

    /**
     * Generates Load Balance Variance comparison bar chart
     */
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
        System.out.println("  ✓ Generated: 4_load_balance_variance.png");
    }

    /**
     * Generates Fairness Index comparison bar chart
     */
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
        System.out.println("  ✓ Generated: 5_fairness_index.png");
    }

    /**
     * Generates comprehensive comparison chart with multiple metrics normalized
     */
    private static void generateComprehensiveChart(List<PerformanceMetrics> metricsList) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Get baseline (Traditional RR) for normalization
        PerformanceMetrics baseline = metricsList.get(0);
        double baselineMakespan = baseline.getMakespan();
        double baselineThroughput = baseline.getThroughput();
        double baselineTurnaround = baseline.getAvgTurnaroundTime();

        for (PerformanceMetrics m : metricsList) {
            // Normalize: For makespan/turnaround, lower is better, so invert
            // Makespan: baseline/current * 100 (higher = better improvement)
            double makespanScore = (baselineMakespan / m.getMakespan()) * 100;
            double throughputScore = (m.getThroughput() / baselineThroughput) * 100;
            double turnaroundScore = (baselineTurnaround / m.getAvgTurnaroundTime()) * 100;

            dataset.addValue(makespanScore, "Makespan Score", m.getAlgorithmName());
            dataset.addValue(throughputScore, "Throughput Score", m.getAlgorithmName());
            dataset.addValue(turnaroundScore, "Turnaround Score", m.getAlgorithmName());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Comprehensive Performance Comparison (Normalized to Traditional RR = 100%)",
                "Algorithm",
                "Performance Score (%)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        styleChart(chart);
        saveChart(chart, "6_comprehensive_comparison.png");
        System.out.println("  ✓ Generated: 6_comprehensive_comparison.png");
    }

    /**
     * Generates Resource Utilization comparison chart
     */
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

        styleChart(chart);
        saveChart(chart, "7_resource_utilization.png");
        System.out.println("  ✓ Generated: 7_resource_utilization.png");
    }

    /**
     * Creates a styled bar chart
     */
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

        styleChart(chart);
        return chart;
    }

    /**
     * Applies professional styling to the chart
     */
    private static void styleChart(JFreeChart chart) {
        // Set background
        chart.setBackgroundPaint(Color.WHITE);

        // Style title
        TextTitle title = chart.getTitle();
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setPaint(new Color(51, 51, 51));

        // Get plot and style it
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.setOutlinePaint(null);

        // Style the renderer (bars)
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter()); // Flat bars (no gradient)
        renderer.setShadowVisible(false);
        renderer.setDrawBarOutline(false);

        // Set colors for each series/algorithm
        int seriesCount = plot.getDataset().getRowCount();
        Color[] colors = {COLOR_TRADITIONAL_RR, COLOR_MARR, COLOR_MMRR, COLOR_MM_MARRA};

        for (int i = 0; i < seriesCount && i < colors.length; i++) {
            renderer.setSeriesPaint(i, colors[i % colors.length]);
        }

        // If single series, color by category
        if (seriesCount == 1) {
            renderer.setSeriesPaint(0, new Color(76, 175, 80)); // Default green
        }

        // Style axes
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 14));
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 14));
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());

        // Add value labels on bars
        renderer.setDefaultItemLabelGenerator(new org.jfree.chart.labels.StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(new Font("SansSerif", Font.BOLD, 11));
    }

    /**
     * Saves chart as PNG image file
     */
    private static void saveChart(JFreeChart chart, String filename) throws IOException {
        File outputFile = new File(OUTPUT_DIR + filename);
        ChartUtils.saveChartAsPNG(outputFile, chart, CHART_WIDTH, CHART_HEIGHT);
    }
}