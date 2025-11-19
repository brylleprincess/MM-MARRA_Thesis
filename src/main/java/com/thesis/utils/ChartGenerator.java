package com.thesis.utils;

import com.thesis.metrics.PerformanceMetrics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Generates charts and graphs for performance comparison
 * Uses JFreeChart library for visualization
 *
 * @author Princess Brylle Tadena
 * @version 1.0
 */
public class ChartGenerator {

    private static final int CHART_WIDTH = 1200;
    private static final int CHART_HEIGHT = 800;
    private static final String OUTPUT_DIR = "results/graphs/";

    /**
     * Generate all comparison charts
     */
    public static void generateAllCharts(List<PerformanceMetrics> metricsList) {
        // Create output directory if it doesn't exist
        new File(OUTPUT_DIR).mkdirs();

        System.out.println("\n========================================");
        System.out.println("GENERATING PERFORMANCE CHARTS");
        System.out.println("========================================");

        // Generate individual charts
        generateMakespanChart(metricsList);
        generateWaitingTimeChart(metricsList);
        generateResponseTimeChart(metricsList);
        generateThroughputChart(metricsList);
        generateResourceUtilizationChart(metricsList);
        generateServerOverloadChart(metricsList);
        generateComprehensiveComparisonChart(metricsList);

        System.out.println("All charts generated successfully!");
        System.out.println("Charts saved to: " + OUTPUT_DIR);
        System.out.println("========================================\n");
    }

    /**
     * Generate Makespan comparison chart
     */
    private static void generateMakespanChart(List<PerformanceMetrics> metricsList) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (PerformanceMetrics metrics : metricsList) {
            dataset.addValue(metrics.getMakespan(), "Makespan",
                    metrics.getAlgorithmName());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Makespan Comparison (Lower is Better)",
                "Algorithm",
                "Makespan (seconds)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        customizeChart(chart, metricsList, "0.00");
        saveChart(chart, "makespan_comparison.png");
    }

    /**
     * Generate Waiting Time comparison chart
     */
    private static void generateWaitingTimeChart(List<PerformanceMetrics> metricsList) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (PerformanceMetrics metrics : metricsList) {
            dataset.addValue(metrics.getAvgWaitingTime(), "Avg Waiting Time",
                    metrics.getAlgorithmName());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Average Waiting Time Comparison (Lower is Better)",
                "Algorithm",
                "Waiting Time (seconds)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        customizeChart(chart, metricsList, "0.00");
        saveChart(chart, "waiting_time_comparison.png");
    }

    /**
     * Generate Response Time comparison chart
     */
    private static void generateResponseTimeChart(List<PerformanceMetrics> metricsList) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (PerformanceMetrics metrics : metricsList) {
            dataset.addValue(metrics.getAvgResponseTime(), "Avg Response Time",
                    metrics.getAlgorithmName());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Average Response Time Comparison (Lower is Better)",
                "Algorithm",
                "Response Time (seconds)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        customizeChart(chart, metricsList, "0.00");
        saveChart(chart, "response_time_comparison.png");
    }

    /**
     * Generate Throughput comparison chart
     */
    private static void generateThroughputChart(List<PerformanceMetrics> metricsList) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (PerformanceMetrics metrics : metricsList) {
            dataset.addValue(metrics.getThroughput(), "Throughput",
                    metrics.getAlgorithmName());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Throughput Comparison (Higher is Better)",
                "Algorithm",
                "Throughput (tasks/second)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        customizeChart(chart, metricsList, "0.0000");
        saveChart(chart, "throughput_comparison.png");
    }

    /**
     * Generate Resource Utilization comparison chart
     */
    private static void generateResourceUtilizationChart(List<PerformanceMetrics> metricsList) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (PerformanceMetrics metrics : metricsList) {
            dataset.addValue(metrics.getCpuUtilization(), "CPU",
                    metrics.getAlgorithmName());
            dataset.addValue(metrics.getMemoryUtilization(), "Memory",
                    metrics.getAlgorithmName());
            dataset.addValue(metrics.getBwUtilization(), "Bandwidth",
                    metrics.getAlgorithmName());
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

        customizeChartMultiSeries(chart, metricsList, "0.00");
        saveChart(chart, "resource_utilization_comparison.png");
    }

    /**
     * Generate Server Overload comparison chart
     */
    private static void generateServerOverloadChart(List<PerformanceMetrics> metricsList) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (PerformanceMetrics metrics : metricsList) {
            dataset.addValue(metrics.getServerOverloadCount(), "Overload Count",
                    metrics.getAlgorithmName());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Server Overload Incidents (Lower is Better)",
                "Algorithm",
                "Number of Overloaded VMs",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        customizeChart(chart, metricsList, "0");
        saveChart(chart, "server_overload_comparison.png");
    }

    /**
     * Generate comprehensive comparison chart (all metrics normalized)
     */
    private static void generateComprehensiveComparisonChart(List<PerformanceMetrics> metricsList) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Find Traditional RR for normalization
        PerformanceMetrics baseline = metricsList.stream()
                .filter(m -> m.getAlgorithmName().equals("Traditional RR"))
                .findFirst()
                .orElse(null);

        if (baseline == null) {
            System.err.println("Cannot generate comprehensive chart - baseline not found");
            return;
        }

        // Normalize all metrics relative to Traditional RR
        for (PerformanceMetrics metrics : metricsList) {
            double makespanNorm = (baseline.getMakespan() / metrics.getMakespan()) * 100;
            double waitingNorm = (baseline.getAvgWaitingTime() / metrics.getAvgWaitingTime()) * 100;
            double responseNorm = (baseline.getAvgResponseTime() / metrics.getAvgResponseTime()) * 100;
            double throughputNorm = (metrics.getThroughput() / baseline.getThroughput()) * 100;
            double cpuNorm = (metrics.getCpuUtilization() / baseline.getCpuUtilization()) * 100;

            dataset.addValue(makespanNorm, "Makespan", metrics.getAlgorithmName());
            dataset.addValue(waitingNorm, "Waiting Time", metrics.getAlgorithmName());
            dataset.addValue(responseNorm, "Response Time", metrics.getAlgorithmName());
            dataset.addValue(throughputNorm, "Throughput", metrics.getAlgorithmName());
            dataset.addValue(cpuNorm, "CPU Utilization", metrics.getAlgorithmName());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Comprehensive Performance Comparison (Normalized to Traditional RR = 100%)",
                "Algorithm",
                "Performance (%) - Higher is Better",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        customizeChartMultiSeries(chart, metricsList, "0.00");
        saveChart(chart, "comprehensive_comparison.png");
    }

    /**
     * Customize chart appearance (single series with value labels)
     * SIMPLIFIED VERSION - NO PROBLEMATIC IMPORTS
     */
    private static void customizeChart(JFreeChart chart, List<PerformanceMetrics> metricsList, String labelFormat) {
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        // Enable value labels on top of bars
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(new Font("SansSerif", Font.BOLD, 14));
        renderer.setDefaultItemLabelPaint(Color.BLACK);

        // Set label generator with custom format
        renderer.setDefaultItemLabelGenerator(
                new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat(labelFormat))
        );

        // Set colors for each algorithm
        Color[] colors = {
                new Color(46, 204, 113),   // Green for MM-MARRA
                new Color(52, 152, 219),   // Blue for MMRR
                new Color(155, 89, 182),   // Purple for MARR
                new Color(231, 76, 60)     // Red for Traditional RR
        };

        for (int i = 0; i < Math.min(colors.length, metricsList.size()); i++) {
            renderer.setSeriesPaint(i, colors[i]);
        }

        // Highlight MM-MARRA with thicker border
        renderer.setSeriesOutlinePaint(0, Color.BLACK);
        renderer.setSeriesOutlineStroke(0, new BasicStroke(2.0f));

        // Set font sizes
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 20));
        plot.getDomainAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 16));
        plot.getRangeAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 16));
        plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 14));
        plot.getRangeAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 14));

        // Set background
        chart.setBackgroundPaint(Color.WHITE);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // Add some padding at the top for labels
        plot.getRangeAxis().setUpperMargin(0.20);
    }

    /**
     * Customize chart appearance (multiple series with value labels)
     * SIMPLIFIED VERSION - NO PROBLEMATIC IMPORTS
     */
    private static void customizeChartMultiSeries(JFreeChart chart, List<PerformanceMetrics> metricsList, String labelFormat) {
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        // Enable value labels on top of bars
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(new Font("SansSerif", Font.BOLD, 12));
        renderer.setDefaultItemLabelPaint(Color.BLACK);

        // Set label generator with custom format
        renderer.setDefaultItemLabelGenerator(
                new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat(labelFormat))
        );

        // Set colors for different metrics/series
        Color[] seriesColors = {
                new Color(46, 204, 113),   // Green
                new Color(52, 152, 219),   // Blue
                new Color(155, 89, 182),   // Purple
                new Color(231, 76, 60),    // Red
                new Color(241, 196, 15)    // Yellow
        };

        for (int i = 0; i < seriesColors.length; i++) {
            renderer.setSeriesPaint(i, seriesColors[i]);
        }

        // Set font sizes
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 20));
        plot.getDomainAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 16));
        plot.getRangeAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 16));
        plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 14));
        plot.getRangeAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 14));

        // Set background
        chart.setBackgroundPaint(Color.WHITE);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // Add padding at the top for labels
        plot.getRangeAxis().setUpperMargin(0.20);
    }

    /**
     * Save chart to file
     */
    private static void saveChart(JFreeChart chart, String filename) {
        try {
            File outputFile = new File(OUTPUT_DIR + filename);
            ChartUtils.saveChartAsPNG(outputFile, chart, CHART_WIDTH, CHART_HEIGHT);
            System.out.println("Generated: " + filename);
        } catch (IOException e) {
            System.err.println("Error saving chart " + filename + ": " + e.getMessage());
        }
    }
}