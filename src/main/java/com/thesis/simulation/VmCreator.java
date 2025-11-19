package com.thesis.simulation;

import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;
import org.cloudsimplus.provisioners.ResourceProvisionerSimple;
import org.cloudsimplus.schedulers.vm.VmSchedulerTimeShared;
import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerTimeShared;

import java.util.ArrayList;
import java.util.List;

public class VmCreator {

    /**
     * CRITICAL: VM MIPS must match cloudlet scale
     */
    public static List<Vm> createHeterogeneousVms(int numberOfVms) {
        List<Vm> vmList = new ArrayList<>();

        for (int i = 0; i < numberOfVms; i++) {
            long mips;
            int pes;

            // 30% Powerful VMs: 1000 MIPS, 4 cores
            if (i < numberOfVms * 0.3) {
                mips = 1000;
                pes = 4;
            }
            // 30% Moderate VMs: 500 MIPS, 2 cores
            else if (i < numberOfVms * 0.6) {
                mips = 500;
                pes = 2;
            }
            // 40% Modest VMs: 250 MIPS, 1 core
            else {
                mips = 250;
                pes = 1;
            }

            Vm vm = new VmSimple(i, mips, pes);
            vm.setRam(4096)
                    .setBw(1000)
                    .setSize(10000)
                    .setCloudletScheduler(new CloudletSchedulerTimeShared());

            vmList.add(vm);
        }
        System.out.println("\n🔍 DEBUG: VM Details:");
        for (Vm vm : vmList) {
            System.out.printf("  VM %d: MIPS=%.0f, PEs=%d, Total Capacity=%.0f MIPS%n",
                    vm.getId(), vm.getMips(), vm.getPesNumber(),
                    vm.getMips() * vm.getPesNumber());
        }
        System.out.println("Created " + numberOfVms + " VMs (MIPS: 250-1000, PEs: 1-4)");
        return vmList;
    }

    public static List<Host> createHosts(int numberOfHosts) {
        List<Host> hostList = new ArrayList<>();

        for (int i = 0; i < numberOfHosts; i++) {
            List<Pe> peList = new ArrayList<>();

            // Each host: 8 PEs with 1500 MIPS each
            for (int j = 0; j < 8; j++) {
                peList.add(new PeSimple(1500));
            }

            Host host = new HostSimple(32768, 10000, 100000, peList);
            host.setRamProvisioner(new ResourceProvisionerSimple())
                    .setBwProvisioner(new ResourceProvisionerSimple())
                    .setVmScheduler(new VmSchedulerTimeShared());

            hostList.add(host);
        }

        return hostList;
    }

    public static void printVmConfiguration(List<Vm> vmList) {
        System.out.println("\n========================================");
        System.out.println("VM CONFIGURATION");
        System.out.println("========================================");
        System.out.printf("%-6s %-10s %-6s%n", "VM ID", "MIPS", "PEs");
        System.out.println("----------------------------------------");

        for (Vm vm : vmList) {
            System.out.printf("%-6d %-10.0f %-6d%n",
                    vm.getId(), vm.getMips(), vm.getPesNumber());
        }
        System.out.println("========================================\n");
    }
}