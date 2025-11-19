package com.thesis.simulation;

import org.cloudsimplus.allocationpolicies.VmAllocationPolicySimple;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.resources.Pe;

import java.util.List;

/**
 * Creates the cloud simulation environment
 * Sets up datacenter, hosts, and simulation parameters
 *
 * @author Princess Brylle Tadena
 * @version 1.0
 */
public class SimulationEnvironment {

    private final CloudSimPlus simulation;
    private Datacenter datacenter;

    /**
     * Constructor - initializes CloudSim Plus
     */
    public SimulationEnvironment() {
        this.simulation = new CloudSimPlus();
    }

    /**
     * Create datacenter with specified number of hosts
     */
    public Datacenter createDatacenter(int numberOfHosts) {
        List<Host> hostList = VmCreator.createHosts(numberOfHosts);

        datacenter = new DatacenterSimple(simulation, hostList, new VmAllocationPolicySimple());

        // Configure datacenter characteristics
        datacenter.getCharacteristics()
                .setCostPerSecond(0.1)
                .setCostPerMem(0.02)
                .setCostPerStorage(0.001)
                .setCostPerBw(0.005);

        System.out.println("\n========================================");
        System.out.println("DATACENTER CREATED");
        System.out.println("========================================");
        System.out.printf("Number of Hosts: %d%n", hostList.size());

        // Calculate total resources - CORRECTED
        long totalMips = hostList.stream()
                .flatMap(host -> host.getPeList().stream())
                .mapToLong(Pe::getCapacity)  // FIXED: Changed from getMips() to getCapacity()
                .sum();

        long totalRam = hostList.stream()
                .mapToLong(host -> host.getRam().getCapacity())
                .sum();

        long totalBw = hostList.stream()
                .mapToLong(host -> host.getBw().getCapacity())
                .sum();

        long totalStorage = hostList.stream()
                .mapToLong(host -> host.getStorage().getCapacity())
                .sum();

        System.out.printf("Total MIPS: %,d%n", totalMips);
        System.out.printf("Total RAM: %,d MB%n", totalRam);
        System.out.printf("Total Bandwidth: %,d Mbps%n", totalBw);
        System.out.printf("Total Storage: %,d MB%n", totalStorage);
        System.out.printf("Total Processing Elements (Cores): %d%n",
                hostList.stream().mapToInt(host -> host.getPeList().size()).sum());
        System.out.println("========================================\n");

        return datacenter;
    }

    /**
     * Get simulation instance
     */
    public CloudSimPlus getSimulation() {
        return simulation;
    }

    /**
     * Get datacenter
     */
    public Datacenter getDatacenter() {
        return datacenter;
    }

    /**
     * Print simulation configuration
     */
    public void printConfiguration() {
        System.out.println("\n========================================");
        System.out.println("SIMULATION CONFIGURATION");
        System.out.println("========================================");
        System.out.printf("CloudSim Plus Version: %s%n", CloudSimPlus.VERSION);
        System.out.printf("Simulation Clock: %.2f seconds%n", simulation.clock());

        // Access datacenter directly instead of through simulation
        if (datacenter != null) {
            System.out.printf("Datacenter ID: %d%n", datacenter.getId());
            System.out.printf("Number of Hosts: %d%n", datacenter.getHostList().size());

            // FIXED: Calculate total VMs across all hosts
            int totalVMs = datacenter.getHostList().stream()
                    .mapToInt(host -> host.getVmList().size())
                    .sum();

            System.out.printf("Number of Active VMs: %d%n", totalVMs);
        }

        System.out.println("========================================\n");
    }
}