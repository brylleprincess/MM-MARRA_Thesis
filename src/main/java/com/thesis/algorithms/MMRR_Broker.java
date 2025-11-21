package com.thesis.algorithms;

import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.vms.Vm;
import java.util.*;

public class MMRR_Broker extends DatacenterBrokerSimple {
    private int currentIndex = 0;
    private final Map<Long, Integer> vmTaskCount = new HashMap<>();

    public MMRR_Broker(CloudSimPlus simulation) {
        super(simulation);
    }

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