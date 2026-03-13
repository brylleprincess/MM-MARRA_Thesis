package com.thesis.ui;

import com.thesis.algorithms.MARR_Broker;
import com.thesis.algorithms.MMARRA_Broker;
import com.thesis.algorithms.MMRR_Broker;
import com.thesis.algorithms.TraditionalRRBroker;
import com.thesis.metrics.MetricsCollector;
import com.thesis.metrics.PerformanceMetrics;
import com.thesis.simulation.SimulationEnvironment;
import com.thesis.simulation.VmCreator;
import com.thesis.simulation.WorkloadGenerator;
import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.vms.Vm;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Princess Brylle N. Tadena
 */

public class SimulationRunner {

    public static final String TRADITIONAL_RR = "Traditional RR";
    public static final String MARR = "MARR";
    public static final String MMRR = "MMRR";
    public static final String MMARRA = "MMARRA";
    public static final String RUN_ALL = "Run All Algorithms";

    private SimulationRunner() {
    }

    public static MetricsCollector runSelection(int hosts, int vms, int cloudlets, String selection) {
        MetricsCollector collector = new MetricsCollector();

        List<String> algorithms = new ArrayList<>();
        if (RUN_ALL.equals(selection)) {
            algorithms.add(TRADITIONAL_RR);
            algorithms.add(MARR);
            algorithms.add(MMRR);
            algorithms.add(MMARRA);
        } else {
            algorithms.add(selection);
        }

        for (String algorithm : algorithms) {
            PerformanceMetrics metrics = runSingleSimulation(hosts, vms, cloudlets, algorithm);
            collector.addMetrics(metrics);
        }

        return collector;
    }

    public static PerformanceMetrics runSingleSimulation(int numberOfHosts, int numberOfVms, int numberOfCloudlets, String algorithmName) {
        SimulationEnvironment env = new SimulationEnvironment();
        Datacenter datacenter = env.createDatacenter(numberOfHosts);

        DatacenterBroker broker = createBroker(env, algorithmName);

        List<Vm> vmList = VmCreator.createHeterogeneousVms(numberOfVms);
        List<Cloudlet> cloudletList = WorkloadGenerator.createDiverseWorkload(numberOfCloudlets);

        broker.submitVmList(vmList);
        broker.submitCloudletList(cloudletList);

        env.getSimulation().start();

        List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();

        return new PerformanceMetrics(algorithmName, finishedCloudlets, vmList);
    }

    private static DatacenterBroker createBroker(SimulationEnvironment env, String algorithmName) {
        switch (algorithmName) {
            case MMARRA:
                return new MMARRA_Broker(env.getSimulation());
            case MMRR:
                return new MMRR_Broker(env.getSimulation());
            case MARR:
                return new MARR_Broker(env.getSimulation());
            default:
                return new TraditionalRRBroker(env.getSimulation());
        }
    }
}