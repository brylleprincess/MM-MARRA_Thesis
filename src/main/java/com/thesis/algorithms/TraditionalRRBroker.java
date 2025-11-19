package com.thesis.algorithms;

import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.vms.Vm;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Traditional Round Robin - BASELINE
 * Simple cyclic VM assignment without any load awareness
 */
public class TraditionalRRBroker extends DatacenterBrokerSimple {

    private int currentVmIndex = 0;
    private final Map<Vm, Integer> vmTaskCount = new HashMap<>();

    public TraditionalRRBroker(CloudSimPlus simulation) {
        super(simulation);
    }

    @Override
    protected Vm defaultVmMapper(Cloudlet cloudlet) {
        List<Vm> vmList = getVmCreatedList();
        if (vmList.isEmpty()) return Vm.NULL;

        Vm selectedVm = vmList.get(currentVmIndex);
        currentVmIndex = (currentVmIndex + 1) % vmList.size();

        vmTaskCount.put(selectedVm, vmTaskCount.getOrDefault(selectedVm, 0) + 1);
        return selectedVm;
    }

    public void printStatistics() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TRADITIONAL RR: BASELINE (No Load Awareness)");
        System.out.println("=".repeat(80));

        if (!vmTaskCount.isEmpty()) {
            System.out.println("Tasks per VM (Strict Round-Robin):");
            vmTaskCount.forEach((vm, count) ->
                    System.out.printf("  VM %d: %d tasks%n", vm.getId(), count));
        }
        System.out.println("=".repeat(80) + "\n");
    }
}
