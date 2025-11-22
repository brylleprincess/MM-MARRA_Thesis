package com.thesis.simulation;

import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.utilizationmodels.UtilizationModelDynamic;
import org.cloudsimplus.utilizationmodels.UtilizationModelFull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates cloudlets (tasks) with realistic workload distribution
 * Includes normal, short (outliers), and long (outliers) tasks
 * to test algorithm robustness against varied workloads
 */
public class WorkloadGenerator {

    // Fixed seed ensures reproducible workload for fair algorithm comparison
    private static final Random random = new Random(42);

    /**
     * Creates diverse workload with three task categories:
     * - 60% Normal tasks (500-2000 MI): Typical workload
     * - 20% Short tasks (100-400 MI): Quick operations, tests outlier handling
     * - 20% Long tasks (3000-6000 MI): Heavy computations, tests load balancing
     *
     * MI = Million Instructions (task length/complexity)
     *
     * @param numberOfCloudlets Total tasks to generate
     * @return List of cloudlets with varied lengths
     */
    public static List<Cloudlet> createDiverseWorkload(int numberOfCloudlets) {
        List<Cloudlet> cloudletList = new ArrayList<>();

        for (int i = 0; i < numberOfCloudlets; i++) {
            long length;

            // Assign task length based on distribution percentages
            if (i < numberOfCloudlets * 0.60) {
                length = 500 + random.nextInt(1500);      // 60% Normal (500-2000 MI)
            } else if (i < numberOfCloudlets * 0.80) {
                length = 100 + random.nextInt(300);       // 20% Short (100-400 MI)
            } else {
                length = 3000 + random.nextInt(3000);     // 20% Long (3000-6000 MI)
            }

            // Create cloudlet with 1 PE requirement
            Cloudlet cloudlet = new CloudletSimple(i, length, 1);
            cloudlet.setFileSize(300)       // Input data size (MB)
                    .setOutputSize(300)     // Output data size (MB)
                    .setUtilizationModelCpu(new UtilizationModelFull())      // Uses 100% allocated CPU
                    .setUtilizationModelRam(new UtilizationModelDynamic(0.3)) // Uses 30% allocated RAM
                    .setUtilizationModelBw(new UtilizationModelDynamic(0.2)); // Uses 20% allocated BW
            cloudletList.add(cloudlet);
        }

        return cloudletList;
    }
}