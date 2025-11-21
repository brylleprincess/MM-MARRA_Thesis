package com.thesis.utils;

import com.thesis.metrics.PerformanceMetrics;
import java.io.*;
import java.util.List;

public class ChartGenerator {

    private static final String OUTPUT_DIR = "results/graphs/";

    public static void generateAllCharts(List<PerformanceMetrics> metricsList) {
        new File(OUTPUT_DIR).mkdirs();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("GENERATING CHART DATA FILES");
        System.out.println("=".repeat(80));

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