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

    public static List<Cloudlet> createDiverseWorkload(int numberOfCloudlets) {
        List<Cloudlet> cloudletList = new ArrayList<>();

        for (int i = 0; i < numberOfCloudlets; i++) {
            long length;

            if (i < numberOfCloudlets * 0.60) {
                length = 500 + random.nextInt(1500);      // 60% Normal
            } else if (i < numberOfCloudlets * 0.80) {
                length = 100 + random.nextInt(300);       // 20% Short
            } else {
                length = 3000 + random.nextInt(3000);     // 20% Long
            }

            Cloudlet cloudlet = new CloudletSimple(i, length, 1);
            cloudlet.setFileSize(300).setOutputSize(300)
                    .setUtilizationModelCpu(new UtilizationModelFull())
                    .setUtilizationModelRam(new UtilizationModelDynamic(0.3))
                    .setUtilizationModelBw(new UtilizationModelDynamic(0.2));
            cloudletList.add(cloudlet);
        }

        return cloudletList;
    }
}