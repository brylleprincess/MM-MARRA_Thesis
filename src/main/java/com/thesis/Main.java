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
 * MM-MARRA Thesis Simulation - Main Entry Point
 * Orchestrates comparative analysis of four scheduling algorithms:
 * Traditional RR, MARR, MMRR, and the proposed MM-MARRA
 *
 * @author: Tadena, Princess Brylle N
 */
public class Main {

    // Simulation configuration constants defining the scale of the experiment
    private static final int NUMBER_OF_VMS = 10;        // Total virtual machines in the cloud
    private static final int NUMBER_OF_HOSTS = 3;       // Physical servers hosting the VMs
    private static final int NUMBER_OF_CLOUDLETS = 150; // Tasks to be scheduled and executed

    /**
     * Main method - executes all algorithm simulations and generates comparison reports
     */
    public static void main(String[] args) {
        // Display simulation header with configuration details
        System.out.println("\n" + "=".repeat(120));
        System.out.println("MM-MARRA THESIS SIMULATION - FINAL WORKING VERSION");
        System.out.println("Multi-Level Median Average Round Robin Algorithm for Cloud Load Balancing");
        System.out.println("Author: Tadena, Princess Brylle N");
        System.out.println("=".repeat(120));
        System.out.printf("Simulation Scale: %d cloudlets | %d VMs | %d hosts%n",
                NUMBER_OF_CLOUDLETS, NUMBER_OF_VMS, NUMBER_OF_HOSTS);
        System.out.println("=".repeat(120) + "\n");

        // Collector to aggregate and compare metrics from all algorithm runs
        MetricsCollector metricsCollector = new MetricsCollector();

        // Execute each scheduling algorithm and collect performance metrics
        System.out.println("STARTING COMPARATIVE SIMULATION...\n");

        metricsCollector.addMetrics(runSimulation("Traditional RR")); // Baseline algorithm
        metricsCollector.addMetrics(runSimulation("MARR"));           // Median Average Round Robin
        metricsCollector.addMetrics(runSimulation("MMRR"));           // Median Mean Round Robin
        metricsCollector.addMetrics(runSimulation("MM-MARRA"));       // Proposed algorithm

        // Generate output: comparison tables, improvement analysis, and CSV export
        metricsCollector.printComparisonTable();
        metricsCollector.printImprovementAnalysis("Traditional RR", "MM-MARRA");
        metricsCollector.exportToCSV("results/data/performance_comparison.csv");

        // Generate visualization data files for charts
        ChartGenerator.generateAllCharts(metricsCollector.getMetricsList());

        System.out.println("\n" + "=".repeat(120));
        System.out.println("✓ SIMULATION COMPLETED SUCCESSFULLY");
        System.out.println("=".repeat(120) + "\n");
    }

    /**
     * Executes a single simulation run for the specified algorithm
     * Creates fresh environment, datacenter, VMs, and cloudlets for each run
     *
     * @param algorithmName Name of the scheduling algorithm to test
     * @return PerformanceMetrics containing all measured results
     */
    private static PerformanceMetrics runSimulation(String algorithmName) {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("Running: " + algorithmName);
        System.out.println("-".repeat(80));

        // Initialize fresh simulation environment for unbiased comparison
        SimulationEnvironment env = new SimulationEnvironment();
        Datacenter dc = env.createDatacenter(NUMBER_OF_HOSTS);
        DatacenterBroker broker = createBroker(env, algorithmName);

        // Create heterogeneous VMs and diverse workload for realistic testing
        List<Vm> vmList = VmCreator.createHeterogeneousVms(NUMBER_OF_VMS);
        List<Cloudlet> cloudletList = WorkloadGenerator.createDiverseWorkload(NUMBER_OF_CLOUDLETS);

        // Submit resources to broker and start simulation
        broker.submitVmList(vmList);
        broker.submitCloudletList(cloudletList);
        env.getSimulation().start();

        // Retrieve completed tasks and report completion rate
        List<Cloudlet> finished = broker.getCloudletFinishedList();
        System.out.printf("Completed: %d/%d cloudlets%n", finished.size(), cloudletList.size());

        // Print MM-MARRA specific load distribution statistics if applicable
        if (broker instanceof MM_MARRA_Broker) {
            ((MM_MARRA_Broker) broker).printStatistics();
        }

        return new PerformanceMetrics(algorithmName, finished, vmList);
    }

    /**
     * Factory method to create the appropriate broker based on algorithm name
     * Each broker implements a different VM selection strategy
     *
     * @param env  Simulation environment containing CloudSimPlus instance
     * @param name Algorithm identifier string
     * @return Configured DatacenterBroker for the specified algorithm
     */
    private static DatacenterBroker createBroker(SimulationEnvironment env, String name) {
        switch (name) {
            case "MM-MARRA": return new MM_MARRA_Broker(env.getSimulation());
            case "MMRR": return new MMRR_Broker(env.getSimulation());
            case "MARR": return new MARR_Broker(env.getSimulation());
            default: return new TraditionalRRBroker(env.getSimulation());
        }
    }
}