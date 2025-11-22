package com.thesis.algorithms;

import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.vms.Vm;
import java.util.*;

/**
 * Traditional Round Robin Broker - Baseline Algorithm
 * Assigns tasks to VMs in strict sequential order without considering
 * VM capacity, current load, or task characteristics
 */
public class TraditionalRRBroker extends DatacenterBrokerSimple {

    private int currentIndex = 0;                           // Tracks current position in VM rotation
    private final Map<Long, Integer> vmTaskCount = new HashMap<>(); // Records tasks assigned per VM

    public TraditionalRRBroker(CloudSimPlus simulation) {
        super(simulation);
    }

    /**
     * Maps cloudlet to VM using simple round-robin rotation
     * Cycles through VMs sequentially: VM0 → VM1 → VM2 → ... → VM0
     *
     * @param cloudlet Task to be assigned
     * @return Next VM in the circular sequence
     */
    protected Vm defaultVmMapper(Cloudlet cloudlet) {
        List<Vm> vmList = getVmCreatedList();
        if (vmList.isEmpty()) return Vm.NULL;

        // Select VM at current index and advance to next position
        Vm vm = vmList.get(currentIndex);
        currentIndex = (currentIndex + 1) % vmList.size(); // Wrap around to 0 at end

        // Track task count for load analysis
        vmTaskCount.merge(vm.getId(), 1, Integer::sum);
        return vm;
    }

    public Map<Long, Integer> getVmTaskCount() { return vmTaskCount; }
}
