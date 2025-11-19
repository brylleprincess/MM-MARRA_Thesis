package com.thesis.simulation;

import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.utilizationmodels.UtilizationModelDynamic;
import org.cloudsimplus.utilizationmodels.UtilizationModelFull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorkloadGenerator {

    private static final Random random = new Random(42);

    /**
     * CRITICAL: Cloudlet lengths MUST be small (100-5000 MI)
     */
    public static List<Cloudlet> createDiverseWorkload(int numberOfCloudlets) {
        List<Cloudlet> cloudletList = new ArrayList<>();

        for (int i = 0; i < numberOfCloudlets; i++) {
            long length;

            // 80% Normal tasks: 400-2000 MI
            if (i < numberOfCloudlets * 0.8) {
                length = 400 + random.nextInt(1600);  // 400 to 2000 MI
            }
            // 10% Short outliers: 100-300 MI
            else if (i < numberOfCloudlets * 0.9) {
                length = 100 + random.nextInt(200);   // 100 to 300 MI
            }
            // 10% Long outliers: 3000-5000 MI
            else {
                length = 3000 + random.nextInt(2000); // 3000 to 5000 MI
            }

            // CRITICAL: Create cloudlet with SMALL length
            Cloudlet cloudlet = new CloudletSimple(i, length, 1);
            cloudlet.setFileSize(300)
                    .setOutputSize(300)
                    .setUtilizationModelCpu(new UtilizationModelFull())
                    .setUtilizationModelRam(new UtilizationModelDynamic(0.3))
                    .setUtilizationModelBw(new UtilizationModelDynamic(0.2));

            cloudletList.add(cloudlet);
        }

        System.out.println("Generated " + numberOfCloudlets + " cloudlets with lengths 100-5000 MI");
        return cloudletList;
    }

    public static void printWorkloadStatistics(List<Cloudlet> cloudletList) {
        System.out.println("\n========================================");
        System.out.println("WORKLOAD STATISTICS");
        System.out.println("========================================");

        long totalLength = 0;
        long minLength = Long.MAX_VALUE;
        long maxLength = Long.MIN_VALUE;

        for (Cloudlet c : cloudletList) {
            long len = c.getLength();
            totalLength += len;
            minLength = Math.min(minLength, len);
            maxLength = Math.max(maxLength, len);
        }

        double avgLength = (double) totalLength / cloudletList.size();

        System.out.printf("Total Cloudlets: %d%n", cloudletList.size());
        System.out.printf("Average Length: %.2f MI%n", avgLength);
        System.out.printf("Min Length: %d MI%n", minLength);
        System.out.printf("Max Length: %d MI%n", maxLength);
        System.out.println("========================================\n");
    }
}