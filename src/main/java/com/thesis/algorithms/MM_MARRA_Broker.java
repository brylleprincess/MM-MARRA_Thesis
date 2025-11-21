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
 * FINAL HYBRID VERSION - Balances ALL metrics
 *
 * Strategy:
 * 1. STRICT task count balance (equal distribution like RR)
 * 2. SMART task-VM matching (long tasks → powerful VMs)
 *
 * This achieves:
 * - Low load balance variance (equal task counts)
 * - High fairness index (equal distribution)
 * - Better makespan (smart matching)
 * - Better throughput (optimal placement)
 *
 * @author: Tadena, Princess Brylle N
 */
public class MM_MARRA_Broker extends DatacenterBrokerSimple {

    private final Map<Vm, VmLoadMetrics> vmLoadMap = new HashMap<>();
    private double medianTaskLength = 0;
    private double avgTaskLength = 0;
    private int totalDecisions = 0;

    // Categorized VM lists
    private List<Vm> powerfulVms = new ArrayList<>();
    private List<Vm> moderateVms = new ArrayList<>();
    private List<Vm> modestVms = new ArrayList<>();
    private boolean vmsInitialized = false;

    public MM_MARRA_Broker(CloudSimPlus simulation) {
        super(simulation);
    }

    @Override
    public MM_MARRA_Broker submitCloudletList(List<? extends Cloudlet> list) {
        if (list == null || list.isEmpty()) {
            return this;
        }

        List<Double> lengths = list.stream()
                .map(c -> (double) c.getLength())
                .collect(Collectors.toList());

        medianTaskLength = StatisticsHelper.calculateMedian(lengths);
        avgTaskLength = StatisticsHelper.calculateMean(lengths);

        System.out.printf("  [MM-MARRA] Median: %.0f MI, Average: %.0f MI%n",
                medianTaskLength, avgTaskLength);

        // Sort cloudlets: SHORT tasks first (SJF for better response time)
        List<Cloudlet> sorted = new ArrayList<>(list);
        sorted.sort(Comparator.comparingLong(Cloudlet::getLength));

        System.out.println("  [MM-MARRA] Applied SJF sorting + Smart VM matching");

        super.submitCloudletList(sorted);
        return this;
    }

    @Override
    protected Vm defaultVmMapper(Cloudlet cloudlet) {
        List<Vm> vmList = getVmCreatedList();
        if (vmList.isEmpty()) {
            return Vm.NULL;
        }

        // Initialize VM categories once
        if (!vmsInitialized) {
            initializeVmCategories(vmList);
            vmsInitialized = true;
        }

        totalDecisions++;

        // Initialize tracking for all VMs
        for (Vm vm : vmList) {
            if (!vmLoadMap.containsKey(vm)) {
                vmLoadMap.put(vm, new VmLoadMetrics(vm.getMips() * vm.getPesNumber()));
            }
        }

        // HYBRID: Equal distribution + Smart matching
        Vm bestVm = selectVmHybrid(vmList, cloudlet);

        // Update metrics
        VmLoadMetrics metrics = vmLoadMap.get(bestVm);
        if (metrics != null) {
            metrics.addTask(cloudlet.getLength());
        }

        return bestVm;
    }

    /**
     * Categorize VMs by capacity
     */
    private void initializeVmCategories(List<Vm> vmList) {
        for (Vm vm : vmList) {
            double capacity = vm.getMips() * vm.getPesNumber();
            if (capacity >= 2000) {
                powerfulVms.add(vm);
            } else if (capacity >= 500) {
                moderateVms.add(vm);
            } else {
                modestVms.add(vm);
            }
        }
        System.out.printf("  [MM-MARRA] VM Categories: %d powerful, %d moderate, %d modest%n",
                powerfulVms.size(), moderateVms.size(), modestVms.size());
    }

    /**
     * HYBRID SELECTION:
     * 1. Find VMs with MINIMUM task count (ensures equal distribution)
     * 2. Among those, pick based on task-VM matching (ensures good performance)
     */
    private Vm selectVmHybrid(List<Vm> vmList, Cloudlet cloudlet) {
        // Step 1: Find minimum task count
        int minTasks = vmLoadMap.values().stream()
                .mapToInt(VmLoadMetrics::getTaskCount)
                .min().orElse(0);

        // Step 2: Get all VMs with minimum task count
        List<Vm> candidateVms = vmList.stream()
                .filter(vm -> {
                    VmLoadMetrics m = vmLoadMap.get(vm);
                    return m != null && m.getTaskCount() == minTasks;
                })
                .collect(Collectors.toList());

        if (candidateVms.isEmpty()) {
            candidateVms = vmList;
        }

        // Step 3: Among candidates, pick based on task-VM matching
        return selectBestMatch(candidateVms, cloudlet);
    }

    /**
     * Select best VM from candidates based on task characteristics
     */
    private Vm selectBestMatch(List<Vm> candidates, Cloudlet cloudlet) {
        if (candidates.size() == 1) {
            return candidates.get(0);
        }

        Vm bestVm = null;
        double bestScore = Double.MAX_VALUE;

        // Classify cloudlet
        boolean isLongTask = cloudlet.getLength() > medianTaskLength * 1.5;
        boolean isShortTask = cloudlet.getLength() < medianTaskLength * 0.5;

        for (Vm vm : candidates) {
            double vmCapacity = vm.getMips() * vm.getPesNumber();
            double score;

            if (isLongTask) {
                // Long task: prefer powerful VM (lower capacity = higher score = worse)
                score = 10000.0 / vmCapacity;
            } else if (isShortTask) {
                // Short task: prefer modest VM (higher capacity = higher score = worse)
                score = vmCapacity / 1000.0;
            } else {
                // Medium task: prefer moderate VM, slight preference for less loaded
                VmLoadMetrics m = vmLoadMap.get(vm);
                double load = (m != null) ? m.getTotalMI() : 0;
                score = Math.abs(vmCapacity - 1000) / 1000.0 + load / 100000.0;
            }

            if (score < bestScore) {
                bestScore = score;
                bestVm = vm;
            }
        }

        return (bestVm != null) ? bestVm : candidates.get(0);
    }

    public void printStatistics() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("MM-MARRA HYBRID LOAD DISTRIBUTION");
        System.out.println("=".repeat(80));

        if (!vmLoadMap.isEmpty()) {
            System.out.printf("%-8s %-10s %-15s %-15s%n",
                    "VM ID", "Tasks", "Total MI", "Capacity");
            System.out.println("-".repeat(50));

            vmLoadMap.forEach((vm, m) ->
                    System.out.printf("%-8d %-10d %-15.0f %-15.0f%n",
                            vm.getId(), m.getTaskCount(), m.getTotalMI(), m.getVmCapacity()));

            // Calculate balance metrics
            List<Integer> counts = vmLoadMap.values().stream()
                    .map(VmLoadMetrics::getTaskCount).collect(Collectors.toList());

            double avg = counts.stream().mapToInt(i -> i).average().orElse(0);
            double variance = counts.stream()
                    .mapToDouble(c -> Math.pow(c - avg, 2)).average().orElse(0);

            double sum = counts.stream().mapToInt(i -> i).sum();
            double sumSq = counts.stream().mapToDouble(i -> (double)i * i).sum();
            double fairness = (sumSq > 0) ? (sum * sum) / (counts.size() * sumSq) : 1.0;

            System.out.printf("\nTask Distribution: Avg=%.1f, Variance=%.2f, Fairness=%.4f%n",
                    avg, variance, fairness);
        }
        System.out.println("=".repeat(80));
    }

    public Map<Vm, VmLoadMetrics> getVmLoadMap() { return vmLoadMap; }

    public static class VmLoadMetrics {
        private int taskCount = 0;
        private double totalMI = 0;
        private final double vmCapacity;

        public VmLoadMetrics(double capacity) { this.vmCapacity = capacity; }
        public void addTask(long length) { taskCount++; totalMI += length; }
        public int getTaskCount() { return taskCount; }
        public double getTotalMI() { return totalMI; }
        public double getVmCapacity() { return vmCapacity; }
    }
}