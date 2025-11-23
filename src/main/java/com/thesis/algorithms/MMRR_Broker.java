package com.thesis.algorithms;

import com.thesis.utils.StatisticsHelper;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.vms.Vm;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Modified Multi-Level Round Robin (MMLRR) Broker
 * * CLASSIFICATION STRATEGY:
 * Uses Mean (μ) and Standard Deviation (σ) to create dynamic priority queues.
 * Level 1 (High Priority): Length < μ - σ
 * Level 2 (Medium Priority): μ - σ <= Length <= μ + σ
 * Level 3 (Low Priority): Length > μ + σ
 */
public class MMRR_Broker extends DatacenterBrokerSimple {

    private final Queue<Cloudlet> level1Queue = new LinkedList<>(); // High Priority
    private final Queue<Cloudlet> level2Queue = new LinkedList<>(); // Medium Priority
    private final Queue<Cloudlet> level3Queue = new LinkedList<>(); // Low Priority

    private int currentIndex = 0;
    private final Map<Long, Integer> vmTaskCount = new HashMap<>();

    private double mean = 0;
    private double stdDev = 0;
    private double t1 = 0; // Threshold 1
    private double t2 = 0; // Threshold 2

    public MMRR_Broker(CloudSimPlus simulation) {
        super(simulation);
    }

    @Override
    public MMRR_Broker submitCloudletList(List<? extends Cloudlet> list) {
        if (list == null || list.isEmpty()) {
            return this;
        }

        // 1. Extract Lengths
        List<Double> lengths = list.stream()
                .map(c -> (double) c.getLength())
                .collect(Collectors.toList());

        // 2. Calculate Statistics (Mean and Standard Deviation)
        mean = StatisticsHelper.calculateMean(lengths);
        stdDev = calculateStandardDeviation(lengths, mean);

        // 3. Define Dynamic Thresholds
        // T1 = Mean - Standard Deviation (Handle negative case)
        t1 = Math.max(0, mean - stdDev);
        // T2 = Mean + Standard Deviation
        t2 = mean + stdDev;

        System.out.printf("  [MMLRR] Mean: %.2f, StdDev: %.2f%n", mean, stdDev);
        System.out.printf("  [MMLRR] Thresholds: T1 (%.2f) | T2 (%.2f)%n", t1, t2);

        // 4. Classification (Queueing)
        for (Cloudlet c : list) {
            if (c.getLength() < t1) {
                level1Queue.add(c); // Shortest tasks (High Priority)
            } else if (c.getLength() <= t2) {
                level2Queue.add(c); // Average tasks (Medium Priority)
            } else {
                level3Queue.add(c); // Longest tasks (Low Priority)
            }
        }

        System.out.printf("  [MMLRR] Levels: L1=%d, L2=%d, L3=%d%n",
                level1Queue.size(), level2Queue.size(), level3Queue.size());

        // 5. Reconstruct Submission List based on Priority
        List<Cloudlet> orderedList = new ArrayList<>();
        orderedList.addAll(level1Queue);
        orderedList.addAll(level2Queue);
        orderedList.addAll(level3Queue);

        // 6. Submit ordered list to parent
        super.submitCloudletList(orderedList);
        return this;
    }

    @Override
    protected Vm defaultVmMapper(Cloudlet cloudlet) {
        // Standard Round Robin VM Mapping
        List<Vm> vmList = getVmCreatedList();
        if (vmList.isEmpty()) {
            return Vm.NULL;
        }

        Vm vm = vmList.get(currentIndex);
        currentIndex = (currentIndex + 1) % vmList.size();

        vmTaskCount.merge(vm.getId(), 1, Integer::sum);
        return vm;
    }

    // --- Helper for Standard Deviation (if not in your Utils) ---
    private double calculateStandardDeviation(List<Double> data, double mean) {
        if (data.isEmpty()) return 0.0;
        double variance = data.stream()
                .mapToDouble(num -> Math.pow(num - mean, 2))
                .sum() / data.size();
        return Math.sqrt(variance);
    }

    public void printStatistics() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("MMLRR (MEAN-STD_DEV) STATISTICS");
        System.out.println("=".repeat(80));
        System.out.printf("  Mean: %.2f MI%n", mean);
        System.out.printf("  Std Deviation: %.2f%n", stdDev);
        System.out.printf("  Level 1 ( < %.2f): %d tasks%n", t1, level1Queue.size());
        System.out.printf("  Level 2 ( %.2f - %.2f): %d tasks%n", t1, t2, level2Queue.size());
        System.out.printf("  Level 3 ( > %.2f): %d tasks%n", t2, level3Queue.size());
        System.out.println("=".repeat(80));
    }
}