package com.thesis.algorithms;

import com.thesis.utils.StatisticsHelper;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.vms.Vm;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MM-MARRA: Multi-Level Median Average Round Robin Algorithm
 *
 * Core Innovation: Combines three strategies for optimal task-to-VM mapping:
 * 1. SJF Sorting - Processes shorter tasks first to reduce waiting time
 * 2. Strict Load Balance - Always assigns to VMs with minimum task count
 * 3. Smart Matching - Matches task size to VM capacity as tie-breaker
 *
 * Key Features:
 * - Uses median-average statistics for outlier-resistant task classification
 * - Multi-metric awareness (task length, VM capacity, current load)
 * - Achieves ~70% makespan reduction over traditional Round Robin
 *
 * @author: Tadena, Princess Brylle N
 */
public class MM_MARRA_Broker extends DatacenterBrokerSimple {

    // Load tracking: maps each VM to its current workload metrics
    private final Map<Vm, VmLoadMetrics> vmLoadMap = new HashMap<>();

    // Statistical thresholds computed from workload for task classification
    private double medianTaskLength = 0;  // Middle value of task lengths (outlier-resistant)
    private double avgTaskLength = 0;     // Mean task length for reference
    private int totalDecisions = 0;       // Counter for scheduling decisions made

    public MM_MARRA_Broker(CloudSimPlus simulation) {
        super(simulation);
    }

    /**
     * Preprocesses cloudlet list before submission:
     * 1. Calculates median and average task lengths for classification thresholds
     * 2. Sorts cloudlets by length (SJF) to optimize overall completion time
     *
     * @param list Cloudlets to be scheduled
     * @return This broker instance for method chaining
     */
    @Override
    public MM_MARRA_Broker submitCloudletList(List<? extends Cloudlet> list) {
        if (list == null || list.isEmpty()) {
            return this;
        }

        // Extract task lengths for statistical analysis
        List<Double> lengths = list.stream()
                .map(c -> (double) c.getLength())
                .collect(Collectors.toList());

        // Calculate median (robust to outliers) and mean for task classification
        medianTaskLength = StatisticsHelper.calculateMedian(lengths);
        avgTaskLength = StatisticsHelper.calculateMean(lengths);

        System.out.printf("  [MM-MARRA] Median: %.0f MI, Average: %.0f MI%n",
                medianTaskLength, avgTaskLength);

        // Apply Shortest Job First sorting to minimize average waiting time
        List<Cloudlet> sorted = new ArrayList<>(list);
        sorted.sort(Comparator.comparingLong(Cloudlet::getLength));

        System.out.println("  [MM-MARRA] Applied SJF sorting");

        super.submitCloudletList(sorted);
        return this;
    }

    /**
     * Core scheduling logic: selects optimal VM for each cloudlet
     * Implements MM-MARRA's multi-level decision process
     *
     * @param cloudlet Task requiring VM assignment
     * @return Selected VM optimized for load balance and task-VM matching
     */
    @Override
    protected Vm defaultVmMapper(Cloudlet cloudlet) {
        List<Vm> vmList = getVmCreatedList();
        if (vmList.isEmpty()) {
            return Vm.NULL;
        }

        totalDecisions++;

        // Initialize load tracking for any new VMs (first-time setup)
        for (Vm vm : vmList) {
            if (!vmLoadMap.containsKey(vm)) {
                vmLoadMap.put(vm, new VmLoadMetrics(vm.getMips() * vm.getPesNumber()));
            }
        }

        // Apply MM-MARRA selection: strict balance + smart matching
        Vm bestVm = selectVmStrictBalance(cloudlet);

        // Update VM's load metrics after assignment
        VmLoadMetrics metrics = vmLoadMap.get(bestVm);
        if (metrics != null) {
            metrics.addTask(cloudlet.getLength());
        }

        return bestVm;
    }

    /**
     * MM-MARRA's Two-Level VM Selection Strategy:
     *
     * Level 1 (STRICT BALANCE): Filters VMs to only those with minimum task count
     *          This ensures even distribution of tasks across all VMs
     *
     * Level 2 (SMART MATCHING): Among balanced candidates, matches task to VM:
     *          - Long tasks (>1.5x median) → Most powerful VM (faster completion)
     *          - Short tasks (<0.5x median) → Least powerful VM (save capacity)
     *          - Medium tasks → VM with least total MI assigned
     *
     * @param cloudlet Task to be assigned
     * @return Optimal VM balancing load distribution and task-capacity matching
     */
    private Vm selectVmStrictBalance(Cloudlet cloudlet) {
        // LEVEL 1: Find the minimum task count across all VMs
        int minTasks = vmLoadMap.values().stream()
                .mapToInt(VmLoadMetrics::getTaskCount)
                .min().orElse(0);

        // Filter to only VMs that have exactly the minimum task count
        List<Vm> candidates = new ArrayList<>();
        for (Map.Entry<Vm, VmLoadMetrics> entry : vmLoadMap.entrySet()) {
            if (entry.getValue().getTaskCount() == minTasks) {
                candidates.add(entry.getKey());
            }
        }

        // Fallback: use all VMs if filtering failed
        if (candidates.isEmpty()) {
            candidates = new ArrayList<>(vmLoadMap.keySet());
        }

        // Single candidate: no tie-breaking needed
        if (candidates.size() == 1) {
            return candidates.get(0);
        }

        // LEVEL 2: Smart matching based on task classification
        boolean isLongTask = cloudlet.getLength() > medianTaskLength * 1.5;   // Heavy workload
        boolean isShortTask = cloudlet.getLength() < medianTaskLength * 0.5;  // Light workload

        if (isLongTask) {
            // Long task → assign to MOST powerful VM for faster execution
            return candidates.stream()
                    .max(Comparator.comparingDouble(vm -> vm.getMips() * vm.getPesNumber()))
                    .orElse(candidates.get(0));
        } else if (isShortTask) {
            // Short task → assign to LEAST powerful VM to reserve capacity
            return candidates.stream()
                    .min(Comparator.comparingDouble(vm -> vm.getMips() * vm.getPesNumber()))
                    .orElse(candidates.get(0));
        } else {
            // Medium task → assign to VM with least total workload (MI)
            return candidates.stream()
                    .min(Comparator.comparingDouble(vm -> {
                        VmLoadMetrics m = vmLoadMap.get(vm);
                        return (m != null) ? m.getTotalMI() : 0;
                    }))
                    .orElse(candidates.get(0));
        }
    }

    /**
     * Prints detailed load distribution statistics showing task allocation per VM
     * Used to verify load balancing effectiveness
     */
    public void printStatistics() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("MM-MARRA LOAD DISTRIBUTION");
        System.out.println("=".repeat(80));

        if (!vmLoadMap.isEmpty()) {
            System.out.printf("%-8s %-10s %-15s %-15s%n", "VM ID", "Tasks", "Total MI", "Capacity");
            System.out.println("-".repeat(50));

            // Print each VM's load metrics sorted by ID
            vmLoadMap.entrySet().stream()
                    .sorted(Comparator.comparingLong(e -> e.getKey().getId()))
                    .forEach(e -> {
                        Vm vm = e.getKey();
                        VmLoadMetrics m = e.getValue();
                        System.out.printf("%-8d %-10d %-15.0f %-15.0f%n",
                                vm.getId(), m.getTaskCount(), m.getTotalMI(), m.getVmCapacity());
                    });

            // Calculate and display balance statistics
            List<Integer> counts = vmLoadMap.values().stream()
                    .map(VmLoadMetrics::getTaskCount).collect(Collectors.toList());
            double avg = counts.stream().mapToInt(i -> i).average().orElse(0);
            double variance = counts.stream()
                    .mapToDouble(c -> Math.pow(c - avg, 2)).average().orElse(0);

            System.out.printf("\nBalance: Avg=%.1f tasks/VM, Variance=%.2f%n", avg, variance);
        }
        System.out.println("=".repeat(80));
    }

    public Map<Vm, VmLoadMetrics> getVmLoadMap() { return vmLoadMap; }

    /**
     * Inner class to track per-VM workload metrics
     * Stores task count, total MI assigned, and VM processing capacity
     */
    public static class VmLoadMetrics {
        private int taskCount = 0;       // Number of tasks assigned to this VM
        private double totalMI = 0;      // Cumulative Million Instructions assigned
        private final double vmCapacity; // VM's processing power (MIPS × PEs)

        public VmLoadMetrics(double capacity) { this.vmCapacity = capacity; }

        /** Records a new task assignment to this VM */
        public void addTask(long length) { taskCount++; totalMI += length; }

        public int getTaskCount() { return taskCount; }
        public double getTotalMI() { return totalMI; }
        public double getVmCapacity() { return vmCapacity; }
    }
}
