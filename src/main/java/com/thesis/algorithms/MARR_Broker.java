package com.thesis.algorithms;

import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.vms.Vm;
import java.util.*;

/**
 * Median Average Round Robin (MARR) Broker
 * Improvement over traditional RR that uses median-average calculation
 * for time quantum determination (implementation uses basic RR for VM mapping)
 */
public class MARR_Broker extends DatacenterBrokerSimple {

    private int currentIndex = 0;                           // Current VM index in rotation
    private final Map<Long, Integer> vmTaskCount = new HashMap<>(); // Task distribution tracker

    public MARR_Broker(CloudSimPlus simulation) {
        super(simulation);
    }

    /**
     * Maps cloudlet to VM using round-robin rotation
     * MARR's median-average improvement applies to time quantum, not VM selection
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