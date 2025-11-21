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

    public static List<Vm> createHeterogeneousVms(int numberOfVms) {
        List<Vm> vmList = new ArrayList<>();

        for (int i = 0; i < numberOfVms; i++) {
            long mips;
            int pes;

            if (i < numberOfVms * 0.3) {
                mips = 1000; pes = 4;  // Powerful
            } else if (i < numberOfVms * 0.7) {
                mips = 500; pes = 2;   // Moderate
            } else {
                mips = 250; pes = 1;   // Modest
            }

            Vm vm = new VmSimple(i, mips, pes);
            vm.setRam(4096).setBw(1000).setSize(10000)
                    .setCloudletScheduler(new CloudletSchedulerTimeShared());
            vmList.add(vm);
        }

        return vmList;
    }

    public static List<Host> createHosts(int numberOfHosts) {
        List<Host> hostList = new ArrayList<>();

        for (int i = 0; i < numberOfHosts; i++) {
            List<Pe> peList = new ArrayList<>();
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
}
