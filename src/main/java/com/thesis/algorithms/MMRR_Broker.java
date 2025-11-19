package com.thesis.algorithms;

import com.thesis.utils.StatisticsHelper;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.vms.Vm;

import java.util.*;

/**
 * MMRR: Median Mean Round Robin
 *
 * Uses median AND mean separately but lacks:
 * - Multi-metric integration
 * - Comprehensive load balancing
 * - Adaptive threshold adjustment
 */
public class MMRR_Broker extends DatacenterBrokerSimple {

    private int currentVmIndex = 0;
    private final Map<Vm, Integer> vmTaskCount = new HashMap<>();
    private final List<Long> cloudletLengths = new ArrayList<>();

    public MMRR_Broker(CloudSimPlus simulation) {
        super(simulation);
    }

    @Override
    protected Vm defaultVmMapper(Cloudlet cloudlet) {
        List<Vm> vmList = getVmCreatedList();
        if (vmList.isEmpty()) return Vm.NULL;

        cloudletLengths.add(cloudlet.getLength());

        // Simple round-robin
        Vm selectedVm = vmList.get(currentVmIndex);
        currentVmIndex = (currentVmIndex + 1) % vmList.size();

        vmTaskCount.put(selectedVm, vmTaskCount.getOrDefault(selectedVm, 0) + 1);
        return selectedVm;
    }

    public void printStatistics() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("MMRR: MEDIAN-MEAN RR (Separate Calculations, No Multi-Metric)");
        System.out.println("=".repeat(80));

        if (!cloudletLengths.isEmpty()) {
            List<Double> lengths = cloudletLengths.stream()
                    .mapToDouble(Long::doubleValue).boxed()
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

            double median = StatisticsHelper.calculateMedian(lengths);
            double mean = StatisticsHelper.calculateMean(lengths);

            System.out.printf("Median Burst Time: %.2f MI%n", median);
            System.out.printf("Mean Burst Time: %.2f MI%n", mean);
        }

        if (!vmTaskCount.isEmpty()) {
            System.out.println("\nTasks per VM (Round-Robin Distribution):");
            vmTaskCount.forEach((vm, count) ->
                    System.out.printf("  VM %d: %d tasks%n", vm.getId(), count));
        }
        System.out.println("=".repeat(80) + "\n");
    }
}
