/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.analyze.live;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.locks.StampedLock;

public final class Quantile implements Serializable {
    private static final int N_MARKERS = 5; // positionDeltas and idealPositions must be updated if
                                            // this is changed

    private static final double[] POSITION_DELTA_CONSTANT_PART = {0, 0, .5, 1};

    private static final double[] POSITION_DELTA_MULTIPLIER = {.5, 1, .5, 0};

    private transient final StampedLock lock;

    private transient final StampedLock initLock;

    // length of idealPositions is N_MARKERS-1 because the lowest idealPosition is always 1
    private final double[] idealPositions;

    private final double[] positions;

    private final double[] heights;

    private int initializedMarkers = 0;

    public final double percentile;

    /**
     * Constructs a single quantile object
     */
    public Quantile(double percentile) {
        this.lock = new StampedLock();
        this.initLock = new StampedLock();
        this.percentile = percentile;
        this.idealPositions = new double[] {1 + 2 * percentile, 1 + 4 * percentile, 3 + 2 * percentile, 5};
        this.positions = new double[] {1, 2, 3, 4, 5};
        this.heights = new double[N_MARKERS];
    }

    /**
     * This constructor is for Gson. It initializes the locks. The other values will be overridden
     * by Gson.
     */
    private Quantile() {
        this.lock = new StampedLock();
        this.initLock = new StampedLock();
        this.percentile = Double.NaN;
        this.idealPositions = null;
        this.positions = null;
        this.heights = null;
    }

    /**
     * Used during deserialization to produce a Quantile with locks initialized
     * 
     * @param quantile
     */
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    private Quantile(final Quantile quantile) {
        this.lock = new StampedLock();
        this.initLock = new StampedLock();
        this.idealPositions = quantile.idealPositions;
        this.positions = quantile.positions;
        this.heights = quantile.heights;
        this.initializedMarkers = quantile.initializedMarkers;
        this.percentile = quantile.percentile;
    }

    private Object readResolve() throws ObjectStreamException {
        if (lock != null) {
            throw new IllegalStateException("Impossible: Transient field already set");
        }
        return new Quantile(this);
    }

    /**
     * @return The current approximation of the configured quantile.
     */
    public double quantile() {
        final long optimisticStamp = lock.tryOptimisticRead();
        double quantile = heights[initializedMarkers / 2]; // Let's just accept that this is not
                                                           // accurate pre init
        if (!lock.validate(optimisticStamp)) {
            final long readStamp = lock.readLock();
            quantile = heights[initializedMarkers / 2];
            lock.unlock(readStamp);
        }
        return quantile;
    }

    /**
     * Decays the currently recorded and ideal positions by decayMultiplier
     */
    public void decay(final double decayMultiplier) {
        if (decayMultiplier == 1) {
            return;
        }
        final long writeStamp = lock.writeLock();
        if (initializedMarkers == N_MARKERS) {
            for (int i = 0; i < idealPositions.length; i++) {
                idealPositions[i] *= decayMultiplier;
                positions[i + 1] *= decayMultiplier;
            }
        }
        lock.unlock(writeStamp);
    }

    /**
     * Adds another datum
     */
    public void add(final double item, final double targetMin, final double targetMax) {
        final long writeStamp = lock.writeLock();
        try {
            if (initializedMarkers < N_MARKERS) { // As noted, either lock gives visibility, both
                                                  // are taken for write
                heights[initializedMarkers] = item;
                final long initWriteStamp = initLock.writeLock();
                initializedMarkers++;
                initLock.unlock(initWriteStamp);
                Arrays.sort(heights, 0, initializedMarkers); // Always sort, simplifies quantile()
                                                             // initially
                return;
            }

            if (targetMax > heights[N_MARKERS - 2]) {
                heights[N_MARKERS - 1] = targetMax;
            } else {
                heights[N_MARKERS - 1] = heights[N_MARKERS - 2] + Math.ulp(heights[N_MARKERS - 2]);
            }
            if (targetMin < heights[1]) {
                heights[0] = targetMin;
            } else {
                heights[0] = heights[1] - Math.ulp(heights[1]);
            }
            positions[N_MARKERS - 1]++; // Because marker N_MARKERS-1 is max, it always gets
                                        // incremented
            for (int i = N_MARKERS - 2; heights[i] > item; i--) { // Increment all other markers >
                                                                  // item
                positions[i]++;
            }

            for (int i = 0; i < idealPositions.length; i++) {
                idealPositions[i] += getPositionDelta(percentile, i); // updated desired positions
            }

            adjust();
        } finally {
            lock.unlock(writeStamp);
        }
    }

    private void adjust() {
        for (int i = 1; i < N_MARKERS - 1; i++) {
            final double position = positions[i]; // n
            final double positionDelta = idealPositions[i - 1] - position;

            if ((positionDelta >= 1 && positions[i + 1] > position + 1) || (positionDelta <= -1 && positions[i - 1] < position - 1)) {
                final int direction = positionDelta > 0 ? 1 : -1; // d
                final double heightBelow = heights[i - 1]; // q(i-1)
                final double height = heights[i]; // q
                final double heightAbove = heights[i + 1]; // q(i+1)
                final double positionBelow = positions[i - 1]; // n(i-1)
                final double positionAbove = positions[i + 1]; // n(i+1)

                // q + d / (n(i+1) - n(i-1)) *
                // ((n - n(i-1) + d) * (q(i+1) - q) / (n(i+1) - n) + (n(i+1) - n - d) * (q - q(i-1))
                // / (n - n(i-1)))
                final double signedPositionRange = direction / (positionAbove - positionBelow);
                final double xBelow = position - positionBelow;
                final double xAbove = positionAbove - position;
                final double upperHalf = (xBelow + direction) * (heightAbove - height) / xAbove;
                final double lowerHalf = (xAbove - direction) * (height - heightBelow) / xBelow;
                final double newHeight = height + signedPositionRange * (upperHalf + lowerHalf);

                if (heightBelow < newHeight && newHeight < heightAbove) {
                    heights[i] = newHeight;
                } else {
                    // use linear form
                    final double rise = heights[i + direction] - height;
                    final double run = positions[i + direction] - position;
                    heights[i] = height + Math.copySign(rise / run, direction);
                }

                positions[i] = position + direction;
            }
        }
    }

    public static double getPositionDelta(final double percentile, final int pos) {
        // how far the ideal positions move for each item
        return POSITION_DELTA_CONSTANT_PART[pos] + POSITION_DELTA_MULTIPLIER[pos] * percentile;
    }
}
