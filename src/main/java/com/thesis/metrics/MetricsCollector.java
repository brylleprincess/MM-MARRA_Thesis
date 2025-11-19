package com.thesis.metrics;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Collects and exports performance metrics from multiple algorithm runs
 * Generates CSV files and comparison tables
 *
 * @author Princess Brylle Tadena
 * @version 1.0
 */
public class MetricsCollector {

    private final List<PerformanceMetrics> metricsCollection;

    /**
     * Constructor
     */
    public MetricsCollector() {
        this.metricsCollection = new ArrayList<>();
    }

    /**
     * Add metrics to collection
     */
    public void addMetrics(PerformanceMetrics metrics) {
        metricsCollection.add(metrics);
    }

    /**
     * Print comparison table
     */
    public void printComparisonTable() {
        System.out.println("\n" + "=".repeat(120));
        System.out.println("ALGORITHM PERFORMANCE COMPARISON");
        System.out.println("=".repeat(120));

        // Header
        System.out.printf("%-20s %-12s %-12s %-12s %-12s %-12s %-12s%n",
                "Algorithm", "Makespan", "Avg Wait", "Avg Resp",
                "Throughput", "CPU Util%", "Overloads");
        System.out.println("-".repeat(120));

        // Data
        for (PerformanceMetrics metrics : metricsCollection) {
            System.out.printf("%-20s %-12.2f %-12.2f %-12.2f %-12.4f %-12.2f %-12d%n",
                    metrics.getAlgorithmName(),
                    metrics.getMakespan(),
                    metrics.getAvgWaitingTime(),
                    metrics.getAvgResponseTime(),
                    metrics.getThroughput(),
                    metrics.getCpuUtilization(),
                    metrics.getServerOverloadCount());
        }

        System.out.println("=".repeat(120));

        // ** FIX: This was moved to the Main.java file, but if you want to keep it here,
        // ** we've now made the public method available. **
        // ** The Main.java already calls this, so we don't need to call it from here. **
        // printImprovementAnalysis("Traditional RR", "MM-MARRA"); // Removed from here
    }

    /**
     * ** FIX: Renamed to public 'printImprovementAnalysis' and uses parameters **
     * Print MM-MARRA improvements over baseline
     */
    public void printImprovementAnalysis(String baselineName, String improvedName) {
        PerformanceMetrics improvedMetrics = findMetrics(improvedName);
        PerformanceMetrics baselineMetrics = findMetrics(baselineName);

        if (improvedMetrics == null || baselineMetrics == null) {
            System.out.println("\nCannot calculate improvements - missing data for: "
                    + baselineName + " or " + improvedName);
            return;
        }

        System.out.println("\n" + "=".repeat(120));
        System.out.printf("%s IMPROVEMENTS OVER %s%n",
                improvedName.toUpperCase(), baselineName.toUpperCase());
        System.out.println("=".repeat(120));

        double makespanImprovement = calculateImprovement(
                baselineMetrics.getMakespan(), improvedMetrics.getMakespan());
        double waitingTimeImprovement = calculateImprovement(
                baselineMetrics.getAvgWaitingTime(), improvedMetrics.getAvgWaitingTime());
        double responseTimeImprovement = calculateImprovement(
                baselineMetrics.getAvgResponseTime(), improvedMetrics.getAvgResponseTime());
        double throughputImprovement = calculateImprovement(
                baselineMetrics.getThroughput(), improvedMetrics.getThroughput(), true);
        double cpuUtilImprovement = calculateImprovement(
                baselineMetrics.getCpuUtilization(), improvedMetrics.getCpuUtilization(), true);
        double overloadImprovement = calculateImprovement(
                baselineMetrics.getServerOverloadCount(), improvedMetrics.getServerOverloadCount());

        System.out.printf("Makespan Reduction: %.2f%%%n", makespanImprovement);
        System.out.printf("Waiting Time Reduction: %.2f%%%n", waitingTimeImprovement);
        System.out.printf("Response Time Reduction: %.2f%%%n", responseTimeImprovement);
        System.out.printf("Throughput Improvement: %.2f%%%n", throughputImprovement);
        System.out.printf("CPU Utilization Improvement: %.2f%%%n", cpuUtilImprovement);
        System.out.printf("Server Overload Reduction: %.2f%%%n", overloadImprovement);

        System.out.println("=".repeat(120) + "\n");
    }

    /**
     * Calculate improvement percentage
     */
    private double calculateImprovement(double baseline, double improved) {
        return calculateImprovement(baseline, improved, false);
    }

    /**
     * Calculate improvement percentage (with direction flag)
     */
    private double calculateImprovement(double baseline, double improved, boolean higherIsBetter) {
        if (baseline == 0) {
            if (improved > 0 && higherIsBetter) return 100.0; // Baseline was 0, now it's > 0
            return 0.0; // No change or baseline was 0
        }

        // Handle divide by zero if baseline is zero
        if (Math.abs(baseline) < 0.0001) {
            return 0.0; // Avoid division by zero
        }

        if (higherIsBetter) {
            return ((improved - baseline) / baseline) * 100.0;
        } else {
            return ((baseline - improved) / baseline) * 100.0;
        }
    }

    /**
     * Find metrics by algorithm name
     */
    private PerformanceMetrics findMetrics(String algorithmName) {
        return metricsCollection.stream()
                .filter(m -> m.getAlgorithmName().equals(algorithmName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Export metrics to CSV file
     */
    public void exportToCSV(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Write header
            writer.println(PerformanceMetrics.getCSVHeader());

            // Write data
            for (PerformanceMetrics metrics : metricsCollection) {
                writer.println(metrics.getMetricsAsCSV());
            }

            System.out.println("Metrics exported to: " + filename);
        } catch (IOException e) {
            System.err.println("Error exporting metrics: " + e.getMessage());
        }
    }
}