package com.thesis;

import com.thesis.algorithms.*;
import com.thesis.metrics.MetricsCollector;
import com.thesis.metrics.PerformanceMetrics;
import com.thesis.simulation.SimulationEnvironment;
import com.thesis.simulation.VmCreator;
import com.thesis.simulation.WorkloadGenerator;
import com.thesis.utils.ChartGenerator;
import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.vms.Vm;

import java.util.List;

/**
 * MMARRA Thesis Simulation - FINAL WORKING VERSION
 */
public class Main {

    private static final int NUMBER_OF_VMS = 15;
    private static final int NUMBER_OF_HOSTS = 20;
    private static final int NUMBER_OF_CLOUDLETS = 500;

    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(120));
        System.out.println("MMARRA THESIS SIMULATION - FINAL WORKING VERSION");
        System.out.println("Multi-Level Median Average Round Robin Algorithm for Cloud Load Balancing");
        System.out.println("=".repeat(120));
        System.out.printf("Simulation Scale: %d cloudlets | %d VMs | %d hosts%n",
                NUMBER_OF_CLOUDLETS, NUMBER_OF_VMS, NUMBER_OF_HOSTS);
        System.out.println("=".repeat(120) + "\n");

        MetricsCollector metricsCollector = new MetricsCollector();

        // Run all algorithms
        System.out.println("STARTING COMPARATIVE SIMULATION...\n");

        metricsCollector.addMetrics(runSimulation("Traditional RR"));
        metricsCollector.addMetrics(runSimulation("MARR"));
        metricsCollector.addMetrics(runSimulation("MMRR"));
        metricsCollector.addMetrics(runSimulation("MMARRA"));

        // Print results
        metricsCollector.printComparisonTable();
        metricsCollector.printImprovementAnalysis("Traditional RR", "MMARRA");
        metricsCollector.exportToCSV("results/data/performance_comparison.csv");

        ChartGenerator.generateAllCharts(metricsCollector.getMetricsList());

        System.out.println("\n" + "=".repeat(120));
        System.out.println("✓ SIMULATION COMPLETED SUCCESSFULLY");
        System.out.println("=".repeat(120) + "\n");
    }

    private static PerformanceMetrics runSimulation(String algorithmName) {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("Running: " + algorithmName);
        System.out.println("-".repeat(80));

        SimulationEnvironment env = new SimulationEnvironment();
        Datacenter dc = env.createDatacenter(NUMBER_OF_HOSTS);
        DatacenterBroker broker = createBroker(env, algorithmName);

        List<Vm> vmList = VmCreator.createHeterogeneousVms(NUMBER_OF_VMS);
        List<Cloudlet> cloudletList = WorkloadGenerator.createDiverseWorkload(NUMBER_OF_CLOUDLETS);

        broker.submitVmList(vmList);
        broker.submitCloudletList(cloudletList);

        env.getSimulation().start();

        List<Cloudlet> finished = broker.getCloudletFinishedList();
        System.out.printf("Completed: %d/%d cloudlets%n", finished.size(), cloudletList.size());

        // Print algorithm-specific stats
        if (broker instanceof MMARRA_Broker) {
            ((MMARRA_Broker) broker).printStatistics();
        } else if (broker instanceof MMRR_Broker) {
            ((MMRR_Broker) broker).printStatistics();
        } else if (broker instanceof MARR_Broker) {
            ((MARR_Broker) broker).printStatistics();
        }

        return new PerformanceMetrics(algorithmName, finished, vmList);
    }

    private static DatacenterBroker createBroker(SimulationEnvironment env, String name) {
        switch (name) {
            case "MMARRA": return new MMARRA_Broker(env.getSimulation());
            case "MMRR": return new MMRR_Broker(env.getSimulation());
            case "MARR": return new MARR_Broker(env.getSimulation());
            default: return new TraditionalRRBroker(env.getSimulation());
        }
    }
}