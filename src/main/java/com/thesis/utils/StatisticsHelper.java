package com.thesis.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author: Princess Brylle N. Tadena
 */

public class StatisticsHelper {

    public static double calculateMedian(List<Double> values) {
        if (values == null || values.isEmpty()) return 0.0;

        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int size = sorted.size();

        return (size % 2 == 0)
                ? (sorted.get(size/2 - 1) + sorted.get(size/2)) / 2.0
                : sorted.get(size/2);
    }

    public static double calculateMean(List<Double> values) {
        if (values == null || values.isEmpty()) return 0.0;
        return values.stream().mapToDouble(d -> d).average().orElse(0.0);
    }

    public static double calculateMedianAverage(List<Double> values) {
        return (calculateMedian(values) + calculateMean(values)) / 2.0;
    }
}
