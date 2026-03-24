package com.thesis.algorithms;

import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.vms.Vm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Traditional Round Robin Broker - Baseline Algorithm
 * Assigns tasks to VMs in strict sequential order without considering
 * VM capacity, current load, or task characteristics.
 *
 * The time quantum is configurable from the UI so the baseline is no longer
 * hard-coded to a single fixed value for every experiment.
 */
public class TraditionalRRBroker extends DatacenterBrokerSimple {

    public static final double DEFAULT_TIME_QUANTUM = 1000.0;

    private int currentIndex = 0;
    private final Map<Long, Integer> vmTaskCount = new HashMap<>();
    private final double timeQuantum;

    public TraditionalRRBroker(CloudSimPlus simulation) {
        this(simulation, DEFAULT_TIME_QUANTUM);
    }

    public TraditionalRRBroker(CloudSimPlus simulation, double timeQuantum) {
        super(simulation);
        this.timeQuantum = timeQuantum > 0 ? timeQuantum : DEFAULT_TIME_QUANTUM;
    }

    @Override
    public Vm defaultVmMapper(Cloudlet cloudlet) {
        List<Vm> vmList = getVmCreatedList();
        if (vmList.isEmpty()) return Vm.NULL;

        Vm vm = vmList.get(currentIndex);
        currentIndex = (currentIndex + 1) % vmList.size();

        vmTaskCount.merge(vm.getId(), 1, Integer::sum);
        return vm;
    }

    public Map<Long, Integer> getVmTaskCount() {
        return vmTaskCount;
    }

    public double getTimeQuantum() {
        return timeQuantum;
    }

    public void printStatistics() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TRADITIONAL RR STATISTICS");
        System.out.println("=".repeat(80));
        System.out.printf("Configured Time Quantum: %.2f MI%n", timeQuantum);

        System.out.printf("%nVM Task Distribution:%n");
        vmTaskCount.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.printf("  VM %d: %d tasks%n", e.getKey(), e.getValue()));

        System.out.println("=".repeat(80));
    }
}