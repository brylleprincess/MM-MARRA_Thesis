package com.thesis.simulation;

import org.cloudsimplus.allocationpolicies.VmAllocationPolicySimple;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;

import java.util.List;

public class SimulationEnvironment {

    private final CloudSimPlus simulation;
    private Datacenter datacenter;

    public SimulationEnvironment() {
        this.simulation = new CloudSimPlus();
    }

    public Datacenter createDatacenter(int numberOfHosts) {
        List<Host> hostList = VmCreator.createHosts(numberOfHosts);
        datacenter = new DatacenterSimple(simulation, hostList, new VmAllocationPolicySimple());

        datacenter.getCharacteristics()
                .setCostPerSecond(0.1)
                .setCostPerMem(0.02)
                .setCostPerStorage(0.001)
                .setCostPerBw(0.005);

        return datacenter;
    }

    public CloudSimPlus getSimulation() { return simulation; }
    public Datacenter getDatacenter() { return datacenter; }
}