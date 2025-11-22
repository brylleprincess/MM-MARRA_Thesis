package com.thesis.utils;

import com.thesis.metrics.PerformanceMetrics;
import java.io.*;
import java.util.List;

/**
 * Generates CSV data files for chart creation in external tools
 * Exports separate files for each metric type for easy plotting
 */
public class ChartGenerator {

    private static final String OUTPUT_DIR = "results/graphs/"; // Output directory for CSVs

    /**
     * Generates all chart data files from collected metrics
     * Creates separate CSVs for: makespan, waiting time, throughput,
     * load balance, and CPU utilization
     *
     * @param metricsList Performance metrics from all algorithm runs
     */
    public static void generateAllCharts(List<PerformanceMetrics> metricsList) {
        new File(OUTPUT_DIR).mkdirs(); // Create output directory if needed

        System.out.println("\n" + "=".repeat(80));
        System.out.println("GENERATING CHART DATA FILES");
        System.out.println("=".repeat(80));

        // Export each metric type to separate CSV for charting
        exportData(metricsList, "1_makespan.csv", "Algorithm,Makespan",
                m -> String.format("%s,%.2f", m.getAlgorithmName(), m.getMakespan()));

        exportData(metricsList, "2_waiting_time.csv", "Algorithm,AvgWaitingTime",
                m -> String.format("%s,%.2f", m.getAlgorithmName(), m.getAvgWaitingTime()));

        exportData(metricsList, "3_throughput.csv", "Algorithm,Throughput",
                m -> String.format("%s,%.4f", m.getAlgorithmName(), m.getThroughput()));

        exportData(metricsList, "4_load_balance.csv", "Algorithm,Variance,FairnessIndex",
                m -> String.format("%s,%.2f,%.4f", m.getAlgorithmName(),
                        m.getLoadBalanceVariance(), m.getFairnessIndex()));

        exportData(metricsList, "5_cpu_utilization.csv", "Algorithm,CPUUtil",
                m -> String.format("%s,%.2f", m.getAlgorithmName(), m.getCpuUtilization()));

        System.out.println("\n✓ All chart data generated in: " + OUTPUT_DIR);
        System.out.println("=".repeat(80));
    }

    /**
     * Helper method to export metrics to a CSV file
     *
     * @param list      Metrics to export
     * @param filename  Output filename
     * @param header    CSV header row
     * @param formatter Function to format each metric as CSV row
     */
    private static void exportData(List<PerformanceMetrics> list, String filename,
                                   String header, java.util.function.Function<PerformanceMetrics, String> formatter) {
        try (PrintWriter w = new PrintWriter(new FileWriter(OUTPUT_DIR + filename))) {
            w.println(header);
            for (PerformanceMetrics m : list) w.println(formatter.apply(m));
            System.out.println("  Generated: " + filename);
        } catch (IOException e) {
            System.err.println("  Error: " + e.getMessage());
        }
    }
}