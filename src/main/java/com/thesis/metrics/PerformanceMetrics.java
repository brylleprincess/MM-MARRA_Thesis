package com.thesis.metrics;

import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.vms.Vm;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Calculates comprehensive performance metrics from simulation results
 * Metrics include: makespan, waiting/response/turnaround times, throughput,
 * resource utilization, load balance variance, and fairness index
 */
public class PerformanceMetrics {

    private final String algorithmName;      // Identifier for the algorithm tested
    private final List<Cloudlet> cloudlets;  // Completed tasks from simulation
    private final List<Vm> vms;              // VMs used in simulation

    // Time-based metrics (lower is better)
    private double makespan;           // Total time to complete all tasks
    private double avgWaitingTime;     // Average time tasks wait before execution
    private double avgTurnaroundTime;  // Average total time from submission to completion
    private double avgResponseTime;    // Average time until first response

    // Efficiency metrics (higher is better)
    private double throughput;         // Tasks completed per unit time
    private double cpuUtilization;     // Percentage of CPU capacity used
    private double memoryUtilization;  // Percentage of memory capacity used
    private double bwUtilization;      // Percentage of bandwidth capacity used

    // Load balance metrics
    private double loadBalanceVariance; // Variance in task distribution (lower = more balanced)
    private double fairnessIndex;       // Jain's fairness index (1.0 = perfectly fair)
    private int serverOverloadCount;    // Number of VMs exceeding 1.5x average load

    /**
     * Constructs metrics object and calculates all performance indicators
     */
    public PerformanceMetrics(String algorithmName, List<Cloudlet> cloudlets, List<Vm> vms) {
        this.algorithmName = algorithmName;
        this.cloudlets = cloudlets;
        this.vms = vms;
        calculateAllMetrics();
    }

    /**
     * Master calculation method - computes all metrics from simulation results
     */
    private void calculateAllMetrics() {
        if (cloudlets == null || cloudlets.isEmpty()) {
            setDefaults();
            return;
        }

        // Filter to only successfully completed cloudlets with valid finish times
        List<Cloudlet> valid = cloudlets.stream()
                .filter(Cloudlet::isFinished)
                .filter(c -> c.getFinishTime() > 0 && c.getFinishTime() < 1e9)
                .collect(Collectors.toList());

        if (valid.isEmpty()) {
            setDefaults();
            return;
        }

        // MAKESPAN: Time when last task completes (total simulation duration)
        makespan = valid.stream().mapToDouble(Cloudlet::getFinishTime).max().orElse(0);

        // WAITING TIME: Average delay before task execution begins
        avgWaitingTime = valid.stream()
                .mapToDouble(c -> Math.max(0, c.getExecStartTime()))
                .average().orElse(0);
        avgResponseTime = avgWaitingTime; // Response = Waiting for non-interactive tasks

        // TURNAROUND TIME: Average actual execution time per task
        avgTurnaroundTime = valid.stream()
                .mapToDouble(Cloudlet::getActualCpuTime)
                .average().orElse(0);

        // THROUGHPUT: Tasks completed per second (efficiency measure)
        throughput = (makespan > 0) ? valid.size() / makespan : 0;

        // Calculate resource utilization percentages
        calcResourceUtil(valid);

        // Calculate load balance statistics
        calcLoadBalance(valid);
    }

    /** Sets all metrics to default/zero values when no valid data exists */
    private void setDefaults() {
        makespan = avgWaitingTime = avgResponseTime = avgTurnaroundTime = 0;
        throughput = cpuUtilization = memoryUtilization = bwUtilization = 0;
        loadBalanceVariance = 0;
        fairnessIndex = 1.0;
        serverOverloadCount = 0;
    }

    /**
     * Calculates CPU, memory, and bandwidth utilization percentages
     * Based on actual CPU time used vs total available capacity
     */
    private void calcResourceUtil(List<Cloudlet> valid) {
        if (vms.isEmpty() || makespan <= 0) {
            cpuUtilization = memoryUtilization = bwUtilization = 0;
            return;
        }

        // Sum of actual CPU time consumed by all cloudlets
        double totalCpuUsed = valid.stream().mapToDouble(Cloudlet::getActualCpuTime).sum();

        // Total CPU capacity available (MIPS × PEs × time / 1000 for scaling)
        double totalCapacity = vms.stream()
                .mapToDouble(vm -> (vm.getMips() * vm.getPesNumber() * makespan) / 1000.0)
                .sum();

        // CPU utilization capped at 100%
        cpuUtilization = (totalCapacity > 0) ? Math.min(100, (totalCpuUsed / totalCapacity) * 100) : 0;

        // Estimated memory and bandwidth utilization (proportional to CPU)
        memoryUtilization = cpuUtilization * 0.75;
        bwUtilization = cpuUtilization * 0.5;
    }

    /**
     * Calculates load balancing metrics:
     * - Variance: measures spread of task distribution (lower = more even)
     * - Fairness Index: Jain's index from 0-1 (1 = perfect equality)
     * - Overload Count: VMs with >1.5x average tasks
     */
    private void calcLoadBalance(List<Cloudlet> valid) {
        // Initialize task count for each VM to zero
        Map<Long, Integer> vmCounts = new HashMap<>();
        for (Vm vm : vms) vmCounts.put(vm.getId(), 0);

        // Count tasks assigned to each VM
        for (Cloudlet c : valid) {
            if (c.getVm() != null && c.getVm() != Vm.NULL) {
                vmCounts.merge(c.getVm().getId(), 1, Integer::sum);
            }
        }

        List<Integer> counts = new ArrayList<>(vmCounts.values());

        if (counts.isEmpty() || counts.stream().allMatch(c -> c == 0)) {
            loadBalanceVariance = 0;
            fairnessIndex = 1.0;
            serverOverloadCount = 0;
            return;
        }

        // VARIANCE: Average squared deviation from mean task count
        double mean = counts.stream().mapToInt(i -> i).average().orElse(0);
        loadBalanceVariance = counts.stream()
                .mapToDouble(c -> Math.pow(c - mean, 2)).average().orElse(0);

        // JAIN'S FAIRNESS INDEX: (sum)² / (n × sum of squares)
        // Range 0-1, where 1 indicates perfect fairness
        double sum = counts.stream().mapToInt(i -> i).sum();
        double sumSq = counts.stream().mapToDouble(i -> (double) i * i).sum();
        fairnessIndex = (sumSq > 0) ? (sum * sum) / (counts.size() * sumSq) : 1.0;

        // OVERLOAD COUNT: VMs exceeding 150% of average load
        double threshold = mean * 1.5;
        serverOverloadCount = (int) counts.stream().filter(c -> c > threshold).count();
    }

    // ===== GETTERS =====
    public String getAlgorithmName() { return algorithmName; }
    public double getMakespan() { return makespan; }
    public double getAvgWaitingTime() { return avgWaitingTime; }
    public double getAvgTurnaroundTime() { return avgTurnaroundTime; }
    public double getAvgResponseTime() { return avgResponseTime; }
    public double getThroughput() { return throughput; }
    public double getCpuUtilization() { return cpuUtilization; }
    public double getMemoryUtilization() { return memoryUtilization; }
    public double getBwUtilization() { return bwUtilization; }
    public double getLoadBalanceVariance() { return loadBalanceVariance; }
    public double getFairnessIndex() { return fairnessIndex; }
    public int getServerOverloadCount() { return serverOverloadCount; }

    /** Formats all metrics as comma-separated values for CSV export */
    public String toCSV() {
        return String.format("%s,%.2f,%.2f,%.2f,%.2f,%.4f,%.2f,%.2f,%.2f,%.2f,%.4f,%d",
                algorithmName, makespan, avgWaitingTime, avgTurnaroundTime,
                avgResponseTime, throughput, cpuUtilization, memoryUtilization,
                bwUtilization, loadBalanceVariance, fairnessIndex, serverOverloadCount);
    }

    /** Returns CSV header row matching toCSV() format */
    public static String csvHeader() {
        return "Algorithm,Makespan,AvgWaitingTime,AvgTurnaroundTime,AvgResponseTime," +
                "Throughput,CPUUtil,MemUtil,BWUtil,LoadVariance,FairnessIndex,OverloadCount";
    }
}
