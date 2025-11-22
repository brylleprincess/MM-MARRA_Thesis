package com.thesis.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Statistical utility class for MM-MARRA algorithm
 * Provides median, mean, and median-average calculations
 * Key for outlier-resistant task classification
 */
public class StatisticsHelper {

    /**
     * Calculates the median (middle value) of a list
     * Median is robust to outliers unlike mean
     * For even-sized lists, returns average of two middle values
     *
     * @param values List of numeric values
     * @return Median value, or 0.0 if empty
     */
    public static double calculateMedian(List<Double> values) {
        if (values == null || values.isEmpty()) return 0.0;

        // Sort a copy to find middle value(s)
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int size = sorted.size();

        // Even count: average of two middle values; Odd: exact middle
        return (size % 2 == 0)
                ? (sorted.get(size/2 - 1) + sorted.get(size/2)) / 2.0
                : sorted.get(size/2);
    }

    /**
     * Calculates arithmetic mean (average) of a list
     *
     * @param values List of numeric values
     * @return Mean value, or 0.0 if empty
     */
    public static double calculateMean(List<Double> values) {
        if (values == null || values.isEmpty()) return 0.0;
        return values.stream().mapToDouble(d -> d).average().orElse(0.0);
    }

    /**
     * Calculates median-average: (median + mean) / 2
     * Combines outlier resistance of median with sensitivity of mean
     * Core statistical method for MM-MARRA task classification
     *
     * @param values List of numeric values
     * @return Median-average value
     */
    public static double calculateMedianAverage(List<Double> values) {
        return (calculateMedian(values) + calculateMean(values)) / 2.0;
    }
}