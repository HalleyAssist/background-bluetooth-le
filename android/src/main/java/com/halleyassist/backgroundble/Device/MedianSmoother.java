package com.halleyassist.backgroundble.Device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MedianSmoother {

    private final List<Float> history;
    private final int filterSize;

    public MedianSmoother(int filterSize) {
        this.filterSize = filterSize;
        this.history = new ArrayList<>();
    }

    public float smooth(float newValue) {
        history.add(newValue);

        // Keep history size within limits
        if (history.size() > filterSize) {
            history.remove(0); // Remove the oldest element
        }

        // If there are not enough elements to calculate the median return the last element.
        if (history.size() < filterSize) {
            return history.get(history.size() - 1);
        }

        List<Float> sortedHistory = new ArrayList<>(history);
        Collections.sort(sortedHistory);
        return sortedHistory.get(filterSize / 2); // Get the middle element (median)
    }
}
