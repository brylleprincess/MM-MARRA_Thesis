package com.thesis.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Statistical utility class for median-average calculations
 * Key component of MM-MARRA's outlier resistance
 */
public class StatisticsHelper {

    /**
     * Calculate median of a list of values
     * Median is resistant to outliers
     */
    public static double calculateMedian(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }

        List<Double> sortedValues = new ArrayList<>(values);
        Collections.sort(sortedValues);

        int size = sortedValues.size();

        if (size % 2 == 0) {
            // Even number of elements: average of two middle values
            return (sortedValues.get(size / 2 - 1) + sortedValues.get(size / 2)) / 2.0;
        } else {
            // Odd number of elements: middle value
            return sortedValues.get(size / 2);
        }
    }

    /**
     * Calculate arithmetic mean of a list of values
     */
    public static double calculateMean(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        for (Double value : values) {
            sum += value;
        }

        return sum / values.size();
    }

    /**
     * Calculate median-average: combination of median and mean
     * This provides statistical robustness while maintaining responsiveness
     * Core innovation of MM-MARRA
     */
    public static double calculateMedianAverage(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }

        double median = calculateMedian(values);
        double mean = calculateMean(values);

        // Equal weighting of median and mean
        return (median + mean) / 2.0;
    }

    /**
     * Calculate standard deviation
     * Useful for measuring workload variability
     */
    public static double calculateStandardDeviation(List<Double> values) {
        if (values == null || values.size() < 2) {
            return 0.0;
        }

        double mean = calculateMean(values);
        double sumSquaredDiff = 0.0;

        for (Double value : values) {
            double diff = value - mean;
            sumSquaredDiff += diff * diff;
        }

        return Math.sqrt(sumSquaredDiff / (values.size() - 1));
    }

    /**
     * Calculate quartiles (Q1, Q2/Median, Q3)
     * Useful for outlier detection
     */
    public static double[] calculateQuartiles(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return new double[]{0.0, 0.0, 0.0};
        }

        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);

        int size = sorted.size();
        double q1 = sorted.get(size / 4);
        double q2 = calculateMedian(sorted);
        double q3 = sorted.get((3 * size) / 4);

        return new double[]{q1, q2, q3};
    }

    /**
     * Detect if a value is an outlier using IQR method
     * IQR = Interquartile Range
     */
    public static boolean isOutlier(double value, List<Double> values) {
        double[] quartiles = calculateQuartiles(values);
        double q1 = quartiles[0];
        double q3 = quartiles[2];
        double iqr = q3 - q1;

        double lowerBound = q1 - 1.5 * iqr;
        double upperBound = q3 + 1.5 * iqr;

        return value < lowerBound || value > upperBound;
    }
}