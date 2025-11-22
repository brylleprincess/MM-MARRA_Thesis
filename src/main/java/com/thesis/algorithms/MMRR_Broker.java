package com.thesis.algorithms;

import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.vms.Vm;
import java.util.*;

/**
 * Median Mean Round Robin (MMRR) Broker
 * Uses combined median and mean statistics for improved scheduling decisions
 * (Implementation uses basic RR for VM mapping as comparison baseline)
 */
public class MMRR_Broker extends DatacenterBrokerSimple {

    private int currentIndex = 0;                           // VM rotation index
    private final Map<Long, Integer> vmTaskCount = new HashMap<>(); // Per-VM task counter

    public MMRR_Broker(CloudSimPlus simulation) {
        super(simulation);
    }

    /**
     * Maps cloudlet to VM in sequential round-robin fashion
     * MMRR's statistical improvements apply to quantum calculation
     */
    @Override
    protected Vm defaultVmMapper(Cloudlet cloudlet) {
        List<Vm> vmList = getVmCreatedList();
        if (vmList.isEmpty()) return Vm.NULL;

        Vm vm = vmList.get(currentIndex);
        currentIndex = (currentIndex + 1) % vmList.size();

        vmTaskCount.merge(vm.getId(), 1, Integer::sum);
        return vm;
    }

    public Map<Long, Integer> getVmTaskCount() { return vmTaskCount; }
}