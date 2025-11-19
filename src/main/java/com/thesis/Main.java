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
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.vms.Vm;

import java.util.List;

/**
 * Main class to run MM-MARRA thesis simulation and comparison
 * Compares 4 algorithms: MM-MARRA (Proposed), MMRR, MARR, Traditional RR
 *
 * @author: Tadena, Princess Brylle N
 * @version 2.0 - FINAL THESIS VERSION
 */
public class Main {

    // Simulation parameters - THESIS SCALE
    private static final int NUMBER_OF_VMS = 100;        // 10 heterogeneous VMs
    private static final int NUMBER_OF_HOSTS = 100;       // 3 physical hosts
    private static final int NUMBER_OF_CLOUDLETS = 500; // 500 tasks

    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("MM-MARRA THESIS SIMULATION - FINAL VERSION");
        System.out.println("Multi-Level Median Average Round Robin Algorithm for Task Scheduling");
        System.out.println("and Load Balancing in Cloud Data Centers");
        System.out.println("University of the Cordilleras - College of IT and Computer Science");
        System.out.println("=".repeat(100));
        System.out.println("CloudSim Plus Version: " + CloudSimPlus.VERSION);
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Simulation Scale: " + NUMBER_OF_CLOUDLETS + " cloudlets, " +
                NUMBER_OF_VMS + " VMs, " + NUMBER_OF_HOSTS + " hosts");
        System.out.println("=".repeat(100) + "\n");

        MetricsCollector metricsCollector = new MetricsCollector();

        // Run all 4 algorithms
        System.out.println("STARTING COMPARATIVE SIMULATION...\n");

        // 1. Traditional RR (Baseline)
        System.out.println("=".repeat(100));
        System.out.println("[1/4] Running Traditional Round Robin (BASELINE)...");
        System.out.println("=".repeat(100));
        PerformanceMetrics traditionalMetrics = runSimulation("Traditional RR");
        metricsCollector.addMetrics(traditionalMetrics);
        System.out.println("\n");

        // 2. MARR
        System.out.println("=".repeat(100));
        System.out.println("[2/4] Running MARR (Median-Average Round Robin)...");
        System.out.println("=".repeat(100));
        PerformanceMetrics marrMetrics = runSimulation("MARR");
        metricsCollector.addMetrics(marrMetrics);
        System.out.println("\n");

        // 3. MMRR
        System.out.println("=".repeat(100));
        System.out.println("[3/4] Running MMRR (Median Mean Round Robin)...");
        System.out.println("=".repeat(100));
        PerformanceMetrics mmrrMetrics = runSimulation("MMRR");
        metricsCollector.addMetrics(mmrrMetrics);
        System.out.println("\n");

        // 4. MM-MARRA (Proposed)
        System.out.println("=".repeat(100));
        System.out.println("[4/4] Running MM-MARRA (PROPOSED ALGORITHM)...");
        System.out.println("=".repeat(100));
        PerformanceMetrics mmMarraMetrics = runSimulation("MM-MARRA");
        metricsCollector.addMetrics(mmMarraMetrics);
        System.out.println("\n");

        // Print results
        System.out.println("\n" + "=".repeat(100));
        System.out.println("INDIVIDUAL ALGORITHM PERFORMANCE RESULTS");
        System.out.println("=".repeat(100));

        traditionalMetrics.printMetrics();
        marrMetrics.printMetrics();
        mmrrMetrics.printMetrics();
        mmMarraMetrics.printMetrics();

        // Comparison table
        metricsCollector.printComparisonTable();

        // Improvement analysis
        metricsCollector.printImprovementAnalysis("Traditional RR", "MM-MARRA");

        // Export results
        metricsCollector.exportToCSV("results/data/performance_comparison.csv");

        // Generate charts
        ChartGenerator.generateAllCharts(List.of(
                traditionalMetrics,
                marrMetrics,
                mmrrMetrics,
                mmMarraMetrics
        ));

        System.out.println("\n" + "=".repeat(100));
        System.out.println("✓ SIMULATION COMPLETED SUCCESSFULLY");
        System.out.println("✓ Results saved to 'results/' directory");
        System.out.println("✓ CSV data: results/data/performance_comparison.csv");
        System.out.println("✓ Charts: results/graphs/ (7 PNG files)");
        System.out.println("=".repeat(100) + "\n");
    }

    private static PerformanceMetrics runSimulation(String algorithmName) {
        System.out.println("\n--- Initializing " + algorithmName + " Simulation ---");

        SimulationEnvironment environment = new SimulationEnvironment();
        Datacenter datacenter = environment.createDatacenter(NUMBER_OF_HOSTS);
        DatacenterBroker broker = createBroker(environment, algorithmName);

        System.out.println("Broker created: " + broker.getClass().getSimpleName());

        List<Vm> vmList = VmCreator.createHeterogeneousVms(NUMBER_OF_VMS);
        System.out.println("Created " + vmList.size() + " heterogeneous VMs");

        List<Cloudlet> cloudletList = WorkloadGenerator.createDiverseWorkload(NUMBER_OF_CLOUDLETS);
        System.out.println("Generated " + cloudletList.size() + " cloudlets (diverse workload with outliers)");

        if (algorithmName.equals("Traditional RR")) {
            VmCreator.printVmConfiguration(vmList);
            WorkloadGenerator.printWorkloadStatistics(cloudletList);
        }

        broker.submitVmList(vmList);
        broker.submitCloudletList(cloudletList);
        System.out.println("Submitted VMs and Cloudlets to broker");

        System.out.println("\n--- Starting Simulation Execution ---");
        double startTime = System.currentTimeMillis();
        environment.getSimulation().start();
        double endTime = System.currentTimeMillis();
        double executionTime = (endTime - startTime) / 1000.0;
        System.out.println("Simulation completed in " + String.format("%.2f", executionTime) + " seconds");

        List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
        int totalSubmitted = cloudletList.size();
        int finishedCount = finishedCloudlets.size();
        int notFinishedCount = totalSubmitted - finishedCount;

        System.out.println("\n--- Simulation Results Summary ---");
        System.out.println("Total Cloudlets Submitted: " + totalSubmitted);
        System.out.println("Cloudlets Finished Successfully: " + finishedCount);
        System.out.println("Cloudlets Not Finished: " + notFinishedCount);
        System.out.printf("Success Rate: %.2f%%%n", (finishedCount * 100.0) / totalSubmitted);

        if (finishedCloudlets.size() > 0) {
            System.out.println("\n--- Sample Cloudlet Execution Details (First 5) ---");
            System.out.printf("%-10s %-12s %-12s %-12s %-12s%n",
                    "Cloudlet", "VM", "Start Time", "Finish Time", "Exec Time");
            System.out.println("-".repeat(60));

            for (int i = 0; i < Math.min(5, finishedCloudlets.size()); i++) {
                Cloudlet c = finishedCloudlets.get(i);
                double execTime = c.getFinishTime() - c.getExecStartTime();
                System.out.printf("%-10d %-12d %-12.2f %-12.2f %-12.2f%n",
                        c.getId(), c.getVm().getId(), c.getExecStartTime(),
                        c.getFinishTime(), execTime);
            }
        }

        if (broker.getVmCreatedList().size() > 0) {
            System.out.println("\n--- VM Utilization Summary ---");
            System.out.printf("%-8s %-12s %-12s %-12s%n", "VM ID", "CPU Util %", "RAM Util %", "Cloudlets");
            System.out.println("-".repeat(48));
            for (Vm vm : broker.getVmCreatedList()) {
                double cpuUtil = vm.getCpuPercentUtilization() * 100;
                double ramUtil = vm.getRam().getPercentUtilization() * 100;
                long cloudletCount = finishedCloudlets.stream()
                        .filter(c -> c.getVm().getId() == vm.getId())
                        .count();
                System.out.printf("%-8d %-12.2f %-12.2f %-12d%n",
                        vm.getId(), cpuUtil, ramUtil, cloudletCount);
            }
        }

        printAlgorithmStatistics(broker, algorithmName);

        return new PerformanceMetrics(algorithmName, finishedCloudlets, vmList);
    }

    private static DatacenterBroker createBroker(SimulationEnvironment environment,
                                                 String algorithmName) {
        switch (algorithmName) {
            case "MM-MARRA":
                return new MM_MARRA_Broker(environment.getSimulation());
            case "MMRR":
                return new MMRR_Broker(environment.getSimulation());
            case "MARR":
                return new MARR_Broker(environment.getSimulation());
            case "Traditional RR":
                return new TraditionalRRBroker(environment.getSimulation());
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + algorithmName);
        }
    }

    private static void printAlgorithmStatistics(DatacenterBroker broker, String algorithmName) {
        try {
            if (broker instanceof MM_MARRA_Broker) {
                ((MM_MARRA_Broker) broker).printStatistics();
            } else if (broker instanceof MMRR_Broker) {
                ((MMRR_Broker) broker).printStatistics();
            } else if (broker instanceof MARR_Broker) {
                ((MARR_Broker) broker).printStatistics();
            } else if (broker instanceof TraditionalRRBroker) {
                ((TraditionalRRBroker) broker).printStatistics();
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not print algorithm statistics: " + e.getMessage());
        }
    }
}