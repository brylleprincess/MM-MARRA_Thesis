package com.thesis.simulation;

import org.cloudsimplus.allocationpolicies.VmAllocationPolicySimple;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import java.util.List;

/**
 * @author: Princess Brylle N. Tadena
 */

/**
 * Manages the CloudSimPlus simulation environment
 * Creates and configures datacenter with hosts and pricing characteristics
 */
public class SimulationEnvironment {

    private final CloudSimPlus simulation;  // Core simulation engine
    private Datacenter datacenter;          // Cloud datacenter containing hosts

    /** Initializes a new CloudSimPlus simulation instance */
    public SimulationEnvironment() {
        this.simulation = new CloudSimPlus();
    }

    /**
     * Creates datacenter with specified number of hosts
     * Configures VM allocation policy and cost characteristics
     *
     * @param numberOfHosts Number of physical servers to create
     * @return Configured Datacenter instance
     */
    public Datacenter createDatacenter(int numberOfHosts) {
        // Create physical hosts to run VMs
        List<Host> hostList = VmCreator.createHosts(numberOfHosts);

        // Initialize datacenter with simple VM allocation (first-fit)
        datacenter = new DatacenterSimple(simulation, hostList, new VmAllocationPolicySimple());

        // Set cloud service pricing model
        datacenter.getCharacteristics()
                .setCostPerSecond(0.1)    // CPU time cost
                .setCostPerMem(0.02)      // Memory cost per MB
                .setCostPerStorage(0.001) // Storage cost per MB
                .setCostPerBw(0.005);     // Bandwidth cost per Mbps

        return datacenter;
    }

    public CloudSimPlus getSimulation() { return simulation; }
    public Datacenter getDatacenter() { return datacenter; }
}