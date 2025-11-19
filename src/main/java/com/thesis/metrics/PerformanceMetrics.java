package com.thesis.metrics;

import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.vms.Vm;

import java.util.List;
import java.util.DoubleSummaryStatistics;

/**
 * Performance metrics calculator for algorithm comparison
 * Calculates: Makespan, Response Time, Throughput, Resource Utilization, etc.
 *
 * @author Princess Brylle Tadena
 * @version 1.0
 */
public class PerformanceMetrics {

    private final List<Cloudlet> cloudlets;
    private final List<Vm> vms;
    private final String algorithmName;

    // Calculated metrics
    private double makespan;
    private double avgWaitingTime;
    private double avgTurnaroundTime;
    private double avgResponseTime;
    private double throughput;
    private double cpuUtilization;
    private double memoryUtilization;
    private double bwUtilization;
    private int serverOverloadCount;

    /**
     * Constructor
     */
    public PerformanceMetrics(String algorithmName, List<Cloudlet> cloudlets, List<Vm> vms) {
        this.algorithmName = algorithmName;
        this.cloudlets = cloudlets;
        this.vms = vms;
        calculateMetrics();
    }

    /**
     * Calculate all performance metrics
     */
    private void calculateMetrics() {
        calculateMakespan();
        calculateWaitingTime();
        calculateTurnaroundTime();
        calculateResponseTime();
        calculateThroughput();
        calculateResourceUtilization();
        calculateServerOverload();
    }

    /**
     * Calculate Makespan (total completion time)
     */
    private void calculateMakespan() {
        makespan = cloudlets.stream()
                .mapToDouble(Cloudlet::getFinishTime)
                .max()
                .orElse(0.0);
    }

    /**
     * Calculate Average Waiting Time
     */
    private void calculateWaitingTime() {
        avgWaitingTime = cloudlets.stream()
                .mapToDouble(Cloudlet::getWaitingTime)
                .average()
                .orElse(0.0);
    }

    /**
     * Calculate Average Turnaround Time
     */
    private void calculateTurnaroundTime() {
        avgTurnaroundTime = cloudlets.stream()
                .mapToDouble(c -> c.getFinishTime() - c.getSubmissionDelay())
                .average()
                .orElse(0.0);
    }

    /**
     * Calculate Average Response Time
     */
    private void calculateResponseTime() {
        avgResponseTime = cloudlets.stream()
                .mapToDouble(c -> c.getExecStartTime() - c.getSubmissionDelay())
                .average()
                .orElse(0.0);
    }

    /**
     * Calculate Throughput (tasks per unit time)
     */
    private void calculateThroughput() {
        if (makespan > 0) {
            throughput = cloudlets.size() / makespan;
        } else {
            throughput = 0.0;
        }
    }

    /**
     * Calculate Resource Utilization (CPU, Memory, Bandwidth)
     */
    private void calculateResourceUtilization() {
        if (vms.isEmpty()) {
            cpuUtilization = 0.0;
            memoryUtilization = 0.0;
            bwUtilization = 0.0;
            return;
        }

        DoubleSummaryStatistics cpuStats = vms.stream()
                .mapToDouble(Vm::getCpuPercentUtilization)
                .summaryStatistics();

        DoubleSummaryStatistics ramStats = vms.stream()
                .mapToDouble(vm -> vm.getRam().getPercentUtilization())
                .summaryStatistics();

        DoubleSummaryStatistics bwStats = vms.stream()
                .mapToDouble(vm -> vm.getBw().getPercentUtilization())
                .summaryStatistics();

        cpuUtilization = cpuStats.getAverage() * 100;
        memoryUtilization = ramStats.getAverage() * 100;
        bwUtilization = bwStats.getAverage() * 100;
    }

    /**
     * Calculate Server Overload Count (VMs exceeding 90% CPU)
     */
    private void calculateServerOverload() {
        serverOverloadCount = (int) vms.stream()
                .filter(vm -> vm.getCpuPercentUtilization() > 0.9)
                .count();
    }

    // Getters
    public String getAlgorithmName() { return algorithmName; }
    public double getMakespan() { return makespan; }
    public double getAvgWaitingTime() { return avgWaitingTime; }
    public double getAvgTurnaroundTime() { return avgTurnaroundTime; }
    public double getAvgResponseTime() { return avgResponseTime; }
    public double getThroughput() { return throughput; }
    public double getCpuUtilization() { return cpuUtilization; }
    public double getMemoryUtilization() { return memoryUtilization; }
    public double getBwUtilization() { return bwUtilization; }
    public int getServerOverloadCount() { return serverOverloadCount; }

    /**
     * Print all metrics
     */
    public void printMetrics() {
        System.out.println("\n========================================");
        System.out.println(algorithmName + " PERFORMANCE METRICS");
        System.out.println("========================================");
        System.out.printf("Makespan: %.2f seconds%n", makespan);
        System.out.printf("Avg Waiting Time: %.2f seconds%n", avgWaitingTime);
        System.out.printf("Avg Turnaround Time: %.2f seconds%n", avgTurnaroundTime);
        System.out.printf("Avg Response Time: %.2f seconds%n", avgResponseTime);
        System.out.printf("Throughput: %.4f tasks/second%n", throughput);
        System.out.printf("CPU Utilization: %.2f%%%n", cpuUtilization);
        System.out.printf("Memory Utilization: %.2f%%%n", memoryUtilization);
        System.out.printf("Bandwidth Utilization: %.2f%%%n", bwUtilization);
        System.out.printf("Server Overload Count: %d VMs%n", serverOverloadCount);
        System.out.println("========================================\n");
    }

    /**
     * Get metrics as formatted string for file output
     */
    public String getMetricsAsCSV() {
        return String.format("%s,%.2f,%.2f,%.2f,%.2f,%.4f,%.2f,%.2f,%.2f,%d",
                algorithmName, makespan, avgWaitingTime, avgTurnaroundTime,
                avgResponseTime, throughput, cpuUtilization, memoryUtilization,
                bwUtilization, serverOverloadCount);
    }

    /**
     * Get CSV header
     */
    public static String getCSVHeader() {
        return "Algorithm,Makespan,AvgWaitingTime,AvgTurnaroundTime," +
                "AvgResponseTime,Throughput,CPUUtilization,MemoryUtilization," +
                "BandwidthUtilization,ServerOverloadCount";
    }
}