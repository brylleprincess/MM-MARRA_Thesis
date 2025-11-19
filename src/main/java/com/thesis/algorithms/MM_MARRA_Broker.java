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
 * THESIS CONTRIBUTION:
 * 1. Unified scheduling + load balancing framework
 * 2. Multi-metric load assessment (CPU, RAM, BW, queue)
 * 3. Median-average calculation for outlier resistance
 * 4. Real-time adaptive VM selection
 *
 * @author: Tadena, Princess Brylle N
 * @version 2.0 - FINAL
 */
public class MM_MARRA_Broker extends DatacenterBrokerSimple {

    private final Map<Vm, VmLoadMetrics> vmLoadMap = new HashMap<>();
    private int totalSchedulingDecisions = 0;
    private int loadBalancingAdjustments = 0;

    public MM_MARRA_Broker(CloudSimPlus simulation) {
        super(simulation);
    }

    @Override
    protected Vm defaultVmMapper(Cloudlet cloudlet) {
        List<Vm> vmList = getVmCreatedList();

        if (vmList.isEmpty()) {
            return Vm.NULL;
        }

        totalSchedulingDecisions++;

        for (Vm vm : vmList) {
            vmLoadMap.putIfAbsent(vm, new VmLoadMetrics());
        }

        // LEVEL 1: Multi-metric load assessment
        Map<Vm, Double> vmCompositeLoad = calculateCompositeLoad(vmList);

        // LEVEL 2: Median-average for outlier resistance
        List<Double> loadValues = new ArrayList<>(vmCompositeLoad.values());
        double medianAvgLoad = StatisticsHelper.calculateMedianAverage(loadValues);

        // LEVEL 3: Intelligent VM selection with load balancing
        Vm selectedVm = selectOptimalVm(vmList, vmCompositeLoad, medianAvgLoad);

        VmLoadMetrics metrics = vmLoadMap.get(selectedVm);
        metrics.incrementTaskCount();
        metrics.addCloudletLength(cloudlet.getLength());

        return selectedVm;
    }

    /**
     * LEVEL 1: Multi-Metric Load Calculation
     * Innovation: Considers 4 metrics simultaneously (vs. 1-3 in literature)
     */
    private Map<Vm, Double> calculateCompositeLoad(List<Vm> vmList) {
        Map<Vm, Double> compositeLoad = new HashMap<>();

        for (Vm vm : vmList) {
            VmLoadMetrics metrics = vmLoadMap.get(vm);

            double cpuLoad = vm.getCpuPercentUtilization();
            double ramLoad = vm.getRam().getPercentUtilization();
            double bwLoad = vm.getBw().getPercentUtilization();
            double queueLoad = Math.min(1.0, metrics.getTaskCount() / 100.0);

            // Equal weighting (can be adjusted based on workload characteristics)
            double composite = (cpuLoad * 0.40 +    // CPU most critical
                    ramLoad * 0.25 +     // Memory important
                    bwLoad * 0.20 +      // Bandwidth for I/O
                    queueLoad * 0.15);   // Queue length indicator

            compositeLoad.put(vm, composite);
        }

        return compositeLoad;
    }

    /**
     * LEVEL 2 & 3: Median-Average Threshold with Intelligent Selection
     * Innovation: Combines statistical robustness with adaptive load balancing
     */
    private Vm selectOptimalVm(List<Vm> vmList, Map<Vm, Double> compositeLoad,
                               double medianAvgThreshold) {

        // Find VMs below median-average threshold (not overloaded)
        List<Vm> candidateVms = vmList.stream()
                .filter(vm -> compositeLoad.get(vm) <= medianAvgThreshold * 1.2) // 20% tolerance
                .collect(Collectors.toList());

        if (candidateVms.isEmpty()) {
            candidateVms = new ArrayList<>(vmList);
            loadBalancingAdjustments++;
        }

        // Select VM with minimum composite load
        Vm selectedVm = candidateVms.stream()
                .min(Comparator.comparingDouble(compositeLoad::get))
                .orElse(vmList.get(0));

        // Additional fairness check: avoid over-utilizing single VM
        VmLoadMetrics selectedMetrics = vmLoadMap.get(selectedVm);
        double avgTaskCount = vmLoadMap.values().stream()
                .mapToInt(VmLoadMetrics::getTaskCount)
                .average()
                .orElse(0.0);

        if (selectedMetrics.getTaskCount() > avgTaskCount * 1.5) {
            // Redistribute to next best VM
            candidateVms.remove(selectedVm);
            if (!candidateVms.isEmpty()) {
                selectedVm = candidateVms.stream()
                        .min(Comparator.comparingDouble(compositeLoad::get))
                        .orElse(selectedVm);
                loadBalancingAdjustments++;
            }
        }

        return selectedVm;
    }

    public void printStatistics() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("MM-MARRA: MULTI-LEVEL LOAD-AWARE STATISTICS");
        System.out.println("=".repeat(80));
        System.out.println("Total Scheduling Decisions: " + totalSchedulingDecisions);
        System.out.println("Load Balancing Adjustments: " + loadBalancingAdjustments);
        System.out.printf("Adaptive Balancing Rate: %.2f%%%n",
                (loadBalancingAdjustments * 100.0) / totalSchedulingDecisions);

        if (!vmLoadMap.isEmpty()) {
            System.out.println("\nVM Load Distribution:");
            System.out.printf("%-8s %-12s %-15s %-15s%n",
                    "VM ID", "Tasks", "Avg Length MI", "Total MI");
            System.out.println("-".repeat(60));

            vmLoadMap.forEach((vm, metrics) -> {
                System.out.printf("%-8d %-12d %-15.2f %-15.0f%n",
                        vm.getId(),
                        metrics.getTaskCount(),
                        metrics.getAverageCloudletLength(),
                        metrics.getTotalMI());
            });

            // Calculate load balance fairness index
            List<Double> taskCounts = vmLoadMap.values().stream()
                    .mapToDouble(m -> (double) m.getTaskCount())
                    .boxed()
                    .collect(Collectors.toList());

            double avgTasks = taskCounts.stream().mapToDouble(d -> d).average().orElse(0.0);
            double stdDev = StatisticsHelper.calculateStandardDeviation(taskCounts);
            double coefficientOfVariation = avgTasks > 0 ? stdDev / avgTasks : 0.0;
            double fairnessIndex = 1.0 / (1.0 + coefficientOfVariation);

            System.out.println("\n--- Load Balance Quality Metrics ---");
            System.out.printf("Average Tasks per VM: %.2f%n", avgTasks);
            System.out.printf("Standard Deviation: %.2f%n", stdDev);
            System.out.printf("Coefficient of Variation: %.4f%n", coefficientOfVariation);
            System.out.printf("Fairness Index: %.4f (1.0 = perfect balance)%n", fairnessIndex);
        }

        System.out.println("=".repeat(80) + "\n");
    }

    private static class VmLoadMetrics {
        private int taskCount = 0;
        private long totalMI = 0;
        private final List<Long> cloudletLengths = new ArrayList<>();

        void incrementTaskCount() { taskCount++; }
        void addCloudletLength(long length) {
            totalMI += length;
            cloudletLengths.add(length);
        }
        int getTaskCount() { return taskCount; }
        long getTotalMI() { return totalMI; }
        double getAverageCloudletLength() {
            return cloudletLengths.isEmpty() ? 0.0 :
                    cloudletLengths.stream().mapToLong(l -> l).average().orElse(0.0);
        }
    }
}