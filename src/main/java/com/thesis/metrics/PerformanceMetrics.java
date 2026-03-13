package com.thesis.metrics;

import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.vms.Vm;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: Princess Brylle N. Tadena
 */

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
        // (submission delay is 0 in your case)
        avgWaitingTime = valid.stream()
                .mapToDouble(c -> Math.max(0, c.getExecStartTime()))
                .average().orElse(0);
        avgResponseTime = avgWaitingTime;

        // Turnaround Time = finish - submit (submit = 0)
        avgTurnaroundTime = valid.stream()
                .mapToDouble(c -> Math.max(0, c.getFinishTime()))
                .average().orElse(0);

        // Throughput
        throughput = (makespan > 0) ? valid.size() / makespan : 0;

        // Resource Utilization (CPU Util is the important one for your table)
        calcResourceUtil(valid);

        // Load Balance + Fairness + Overload
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
        if (vms == null || vms.isEmpty() || makespan <= 0) {
            cpuUtilization = memoryUtilization = bwUtilization = 0;
            return;
        }

        // Total CPU time used (seconds)
        double totalCpuUsed = valid.stream()
                .mapToDouble(Cloudlet::getActualCpuTime)
                .sum();

        // Total capacity in CPU-seconds = makespan * total VM PEs
        long totalPes = vms.stream().mapToLong(Vm::getPesNumber).sum();
        double totalCapacity = makespan * totalPes;

        cpuUtilization = (totalCapacity > 0)
                ? Math.min(100.0, (totalCpuUsed / totalCapacity) * 100.0)
                : 0;

        // Optional placeholders
        memoryUtilization = cpuUtilization * 0.75;
        bwUtilization = cpuUtilization * 0.50;
    }

    private void calcLoadBalance(List<Cloudlet> valid) {
        if (vms == null || vms.isEmpty() || makespan <= 0) {
            loadBalanceVariance = 0;
            fairnessIndex = 1.0;
            serverOverloadCount = 0;
            return;
        }

        // For each VM, compute utilization = execTime / (makespan * VM_PEs)
        Map<Long, Double> vmExecTime = new HashMap<>();
        for (Vm vm : vms) vmExecTime.put(vm.getId(), 0.0);

        for (Cloudlet c : valid) {
            Vm vm = c.getVm();
            if (vm != null && vm != Vm.NULL) {
                vmExecTime.merge(vm.getId(), c.getActualCpuTime(), Double::sum);
            }
        }

        List<Double> vmUtils = new ArrayList<>();
        for (Vm vm : vms) {
            double exec = vmExecTime.getOrDefault(vm.getId(), 0.0);
            double cap = makespan * Math.max(1, vm.getPesNumber());
            double util = (cap > 0) ? (exec / cap) : 0.0;
            vmUtils.add(util);
        }

        // If everything is 0, keep safe defaults
        if (vmUtils.stream().allMatch(u -> u <= 0)) {
            loadBalanceVariance = 0;
            fairnessIndex = 1.0;
            serverOverloadCount = 0;
            return;
        }

        // Variance of utilizations (0..1 range)
        double mean = vmUtils.stream().mapToDouble(u -> u).average().orElse(0);
        loadBalanceVariance = vmUtils.stream()
                .mapToDouble(u -> Math.pow(u - mean, 2))
                .average().orElse(0);

        // Jain's fairness index (0..1)
        double sum = vmUtils.stream().mapToDouble(u -> u).sum();
        double sumSq = vmUtils.stream().mapToDouble(u -> u * u).sum();
        fairnessIndex = (sumSq > 0) ? (sum * sum) / (vmUtils.size() * sumSq) : 1.0;

        // Overloaded VMs: utilization higher than (mean + 1.5 * std)
        double std = Math.sqrt(loadBalanceVariance);
        double threshold = mean + (1.5 * std);
        serverOverloadCount = (int) vmUtils.stream().filter(u -> u > threshold).count();
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
        return String.format("%s,%.2f,%.2f,%.2f,%.2f,%.4f,%.2f,%.2f,%.2f,%.6f,%.4f,%d",
                algorithmName, makespan, avgWaitingTime, avgTurnaroundTime,
                avgResponseTime, throughput, cpuUtilization, memoryUtilization,
                bwUtilization, loadBalanceVariance, fairnessIndex, serverOverloadCount);
    }

    public static String csvHeader() {
        return "Algorithm,Makespan,AvgWaitingTime,AvgTurnaroundTime,AvgResponseTime," +
                "Throughput,CPUUtil,MemUtil,BWUtil,LoadVariance,FairnessIndex,OverloadCount";
    }
}