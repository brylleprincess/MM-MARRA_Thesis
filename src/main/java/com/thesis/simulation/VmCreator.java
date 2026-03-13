package com.thesis.simulation;

import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;
import org.cloudsimplus.provisioners.ResourceProvisionerSimple;
import org.cloudsimplus.schedulers.vm.VmSchedulerSpaceShared;
import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Princess Brylle N. Tadena
 */

/**
 * Factory class for creating VMs and Hosts with heterogeneous configurations
 * Simulates real cloud environments with varying resource capacities
 */
public class VmCreator {

    /**
     * Creates VMs with three tiers of processing power:
     * - 30% Powerful (1000 MIPS, 4 cores): For heavy workloads
     * - 40% Moderate (500 MIPS, 2 cores): Standard instances
     * - 30% Modest (250 MIPS, 1 core): Light workloads / cost-effective
     *
     * @param numberOfVms Total VMs to create
     * @return List of heterogeneous VMs
     */
    public static List<Vm> createHeterogeneousVms(int numberOfVms) {
        List<Vm> vmList = new ArrayList<>();

        for (int i = 0; i < numberOfVms; i++) {
            long mips;  // Million Instructions Per Second
            int pes;    // Processing Elements (CPU cores)

            // Assign capacity tier based on VM index
            if (i < numberOfVms * 0.3) {
                mips = 1000; pes = 4;  // Powerful tier (first 30%)
            } else if (i < numberOfVms * 0.7) {
                mips = 500; pes = 2;   // Moderate tier (next 40%)
            } else {
                mips = 250; pes = 1;   // Modest tier (last 30%)
            }

            // Create VM with specified capacity and resources
            Vm vm = new VmSimple(i, mips, pes);
            vm.setRam(4096)      // 4GB RAM
                    .setBw(1000)       // 1Gbps bandwidth
                    .setSize(10000)    // 10GB storage
                    .setCloudletScheduler(new CloudletSchedulerSpaceShared());
            vmList.add(vm);
        }

        java.util.Collections.shuffle(vmList, new java.util.Random(42));
        return vmList;
    }

    /**
     * Creates physical hosts (servers) to run VMs
     * Each host has 8 cores at 1500 MIPS, 32GB RAM, 10Gbps BW, 100GB storage
     *
     * @param numberOfHosts Number of physical servers
     * @return List of configured Host instances
     */
    public static List<Host> createHosts(int numberOfHosts) {
        List<Host> hostList = new ArrayList<>();

        for (int i = 0; i < numberOfHosts; i++) {
            // Create 8 Processing Elements (cores) per host
            List<Pe> peList = new ArrayList<>();
            for (int j = 0; j < 8; j++) {
                peList.add(new PeSimple(1500)); // 1500 MIPS per core
            }

            // Configure host with RAM, BW, storage, and PEs
            Host host = new HostSimple(32768, 10000, 100000, peList); // 32GB, 10Gbps, 100GB
            host.setRamProvisioner(new ResourceProvisionerSimple())   // Simple RAM allocation
                    .setBwProvisioner(new ResourceProvisionerSimple())    // Simple BW allocation
                    .setVmScheduler(new VmSchedulerSpaceShared());         // VMs share CPU time
            hostList.add(host);
        }

        return hostList;
    }
}