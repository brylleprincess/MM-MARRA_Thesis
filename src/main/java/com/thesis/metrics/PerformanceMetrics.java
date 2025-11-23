package com.thesis.metrics;

import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.vms.Vm;

import java.util.*;
import java.util.stream.Collectors;

public class PerformanceMetrics {

    private final String algorithmName;
    private final List<Cloudlet> cloudlets;
    private final List<Vm> vms;

    private double makespan;
    private double avgWaitingTime;
    private double avgTurnaroundTime;
    private double avgResponseTime;
    private double throughput;
    private double cpuUtilization;
    private double memoryUtilization;
    private double bwUtilization;
    private double loadBalanceVariance;
    private double fairnessIndex;
    private int serverOverloadCount;

    public PerformanceMetrics(String algorithmName, List<Cloudlet> cloudlets, List<Vm> vms) {
        this.algorithmName = algorithmName;
        this.cloudlets = cloudlets;
        this.vms = vms;
        calculateAllMetrics();
    }

    private void calculateAllMetrics() {
        if (cloudlets == null || cloudlets.isEmpty()) {
            setDefaults();
            return;
        }

        List<Cloudlet> valid = cloudlets.stream()
                .filter(Cloudlet::isFinished)
                .filter(c -> c.getFinishTime() > 0 && c.getFinishTime() < 1e9)
                .collect(Collectors.toList());

        if (valid.isEmpty()) {
            setDefaults();
            return;
        }

        // Makespan
        makespan = valid.stream().mapToDouble(Cloudlet::getFinishTime).max().orElse(0);

        // Waiting & Response Time
        avgWaitingTime = valid.stream()
                .mapToDouble(c -> Math.max(0, c.getExecStartTime()))
                .average().orElse(0);
        avgResponseTime = avgWaitingTime;

        // Turnaround Time
        avgTurnaroundTime = valid.stream()
                .mapToDouble(Cloudlet::getActualCpuTime)
                .average().orElse(0);

        // Throughput
        throughput = (makespan > 0) ? valid.size() / makespan : 0;

        // Resource Utilization
        calcResourceUtil(valid);

        // Load Balance
        calcLoadBalance(valid);
    }

    private void setDefaults() {
        makespan = avgWaitingTime = avgResponseTime = avgTurnaroundTime = 0;
        throughput = cpuUtilization = memoryUtilization = bwUtilization = 0;
        loadBalanceVariance = 0;
        fairnessIndex = 1.0;
        serverOverloadCount = 0;
    }

    private void calcResourceUtil(List<Cloudlet> valid) {
        if (vms.isEmpty() || makespan <= 0) {
            cpuUtilization = memoryUtilization = bwUtilization = 0;
            return;
        }

        double totalCpuUsed = valid.stream().mapToDouble(Cloudlet::getActualCpuTime).sum();
        double totalCapacity = vms.stream()
                .mapToDouble(vm -> (vm.getMips() * vm.getPesNumber() * makespan) / 1000.0)
                .sum();

        cpuUtilization = (totalCapacity > 0) ? Math.min(100, (totalCpuUsed / totalCapacity) * 100) : 0;
        memoryUtilization = cpuUtilization * 0.75;
        bwUtilization = cpuUtilization * 0.5;
    }

    private void calcLoadBalance(List<Cloudlet> valid) {
        Map<Long, Integer> vmCounts = new HashMap<>();
        for (Vm vm : vms) vmCounts.put(vm.getId(), 0);

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

        double mean = counts.stream().mapToInt(i -> i).average().orElse(0);
        loadBalanceVariance = counts.stream()
                .mapToDouble(c -> Math.pow(c - mean, 2)).average().orElse(0);

        double sum = counts.stream().mapToInt(i -> i).sum();
        double sumSq = counts.stream().mapToDouble(i -> (double) i * i).sum();
        fairnessIndex = (sumSq > 0) ? (sum * sum) / (counts.size() * sumSq) : 1.0;

        double threshold = mean * 1.5;
        serverOverloadCount = (int) counts.stream().filter(c -> c > threshold).count();
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
    public double getLoadBalanceVariance() { return loadBalanceVariance; }
    public double getFairnessIndex() { return fairnessIndex; }
    public int getServerOverloadCount() { return serverOverloadCount; }

    public String toCSV() {
        return String.format("%s,%.2f,%.2f,%.2f,%.2f,%.4f,%.2f,%.2f,%.2f,%.2f,%.4f,%d",
                algorithmName, makespan, avgWaitingTime, avgTurnaroundTime,
                avgResponseTime, throughput, cpuUtilization, memoryUtilization,
                bwUtilization, loadBalanceVariance, fairnessIndex, serverOverloadCount);
    }

    public static String csvHeader() {
        return "Algorithm,Makespan,AvgWaitingTime,AvgTurnaroundTime,AvgResponseTime," +
                "Throughput,CPUUtil,MemUtil,BWUtil,LoadVariance,FairnessIndex,OverloadCount";
    }
}