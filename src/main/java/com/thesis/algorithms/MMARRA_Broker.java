package com.thesis.algorithms;

import com.thesis.utils.StatisticsHelper;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.vms.Vm;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MMARRA: Multi-Level Median Average Round Robin Algorithm
 *
 * Actual behavior in this implementation:
 * 1) Cloudlets are sorted using SJF (Shortest Job First).
 * 2) VM selection first balances based on workload ratio (Total MI / VM capacity),
 *    allowing a small tolerance band to avoid always selecting only 1 VM.
 * 3) VM selection then uses a projected workload score for task-VM matching.
 *
 * Task classification thresholds:
 * - Long:  > 1.5 * median
 * - Short: < 0.5 * median
 * - Medium: otherwise
 */

public class MMARRA_Broker extends DatacenterBrokerSimple {

    // Load tracking: maps each VM to its current workload metrics
    private final Map<Vm, VmLoadMetrics> vmLoadMap = new HashMap<>();

    // Statistical thresholds computed from workload for task classification
    private double medianTaskLength = 0;  // Middle value of task lengths (outlier-resistant)
    private double avgTaskLength = 0;     // Mean task length for reference
    private int totalDecisions = 0;       // Counter for scheduling decisions made

    public MMARRA_Broker(CloudSimPlus simulation) {
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
    public MMARRA_Broker submitCloudletList(List<? extends Cloudlet> list) {
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

        System.out.printf("  [MMARRA] Median: %.0f MI, Average: %.0f MI%n",
                medianTaskLength, avgTaskLength);

        // Apply Shortest Job First sorting to minimize average waiting time
        List<Cloudlet> sorted = new ArrayList<>(list);
        sorted.sort(Comparator.comparingLong(Cloudlet::getLength));

        System.out.println("  [MMARRA] Applied SJF sorting");

        super.submitCloudletList(sorted);
        return this;
    }

    /**
     * Core scheduling logic: selects optimal VM for each cloudlet
     * Implements MMARRA's multi-level decision process
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

        // Apply MMARRA selection: strict balance + smart matching
        Vm bestVm = selectVmStrictBalance(cloudlet);

        // Update VM's load metrics after assignment
        VmLoadMetrics metrics = vmLoadMap.get(bestVm);
        if (metrics != null) {
            metrics.addTask(cloudlet.getLength());
        }

        return bestVm;
    }

    /**
     * MMARRA's Two-Level VM Selection Strategy:
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

       // --- LEVEL 1: Workload-aware balancing
       // loadRatio = totalMI / capacity
       double minLoadRatio = vmLoadMap.entrySet().stream()
               .mapToDouble(e -> {
                   VmLoadMetrics m = e.getValue();
                   return (m.getVmCapacity() > 0) ? (m.getTotalMI() / m.getVmCapacity()) : Double.MAX_VALUE;
               })
               .min()
               .orElse(0);

       // Candidate set within a tolerance band of the best load ratio
       final double tolerance = Math.max(1e-6, minLoadRatio * 0.10); // 10% band, safer floor than 1e-9

       List<Vm> candidates = new ArrayList<>();
       for (Map.Entry<Vm, VmLoadMetrics> entry : vmLoadMap.entrySet()) {
           VmLoadMetrics m = entry.getValue();
           double ratio = (m.getVmCapacity() > 0)
                   ? (m.getTotalMI() / m.getVmCapacity())
                   : Double.MAX_VALUE;

           if (ratio <= (minLoadRatio + tolerance)) {
               candidates.add(entry.getKey());
           }
       }

       if (candidates.isEmpty()) {
           candidates = new ArrayList<>(vmLoadMap.keySet());
       }
       if (candidates.size() == 1) {
           return candidates.get(0);
       }

       // --- LEVEL 2: MMARRA classification-based matching ---
       boolean isLongTask  = cloudlet.getLength() > medianTaskLength * 1.5;
       boolean isShortTask = cloudlet.getLength() < medianTaskLength * 0.5;

       if (isLongTask) {
           return candidates.stream()
                   .min(Comparator.comparingDouble(vm -> {
                       VmLoadMetrics m = vmLoadMap.get(vm);
                       double cap = (m != null) ? m.getVmCapacity() : 0;
                       double cur = (m != null) ? m.getTotalMI() : 0;
                       return (cap > 0) ? ((cur + cloudlet.getLength()) / cap) : Double.MAX_VALUE;
                   }))
                   .orElse(candidates.get(0));
       }

       if (isShortTask) {
           return candidates.stream()
                   .min(Comparator.comparingDouble(vm -> {
                       VmLoadMetrics m = vmLoadMap.get(vm);
                       double cap = (m != null) ? m.getVmCapacity() : 0;
                       double cur = (m != null) ? m.getTotalMI() : 0;
                       return (cap > 0) ? ((cur + cloudlet.getLength()) / cap) : Double.MAX_VALUE;
                   }))
                   .orElse(candidates.get(0));
       }

       // Medium task: choose VM with lowest projected workload
       return candidates.stream()
               .min(Comparator.comparingDouble(vm -> {
                   VmLoadMetrics m = vmLoadMap.get(vm);
                   double cap = (m != null) ? m.getVmCapacity() : 0;
                   double cur = (m != null) ? m.getTotalMI() : 0;
                   return (cap > 0) ? ((cur + cloudlet.getLength()) / cap) : Double.MAX_VALUE;
               }))
               .orElse(candidates.get(0));
   }

    /**
     * Prints detailed load distribution statistics showing task allocation per VM
     * Used to verify load balancing effectiveness
     */
    public void printStatistics() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("MMARRA LOAD DISTRIBUTION");
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

