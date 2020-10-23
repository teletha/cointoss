/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.analyze;

/**
 * Utility to incrementally calculate mean, variance and standard deviation of a sample. Sample
 * points can be {@link #add(double) added}, {@link #remove(double) removed} or
 * {@link #replace(double, double) replaced}.
 * <p>
 * The implementation is based on Welford’s Algorithm given in Knuth Vol 2, p 232.
 * <p>
 * This class is <i>NOT</i> thread safe.
 */
public class OnlineStats {

    private long count;

    private double mean, s;

    /**
     * Adds the value {@code x} to the sample. The sample count is incremented by one by this
     * operation,
     * 
     * @param x the value to add
     */
    public void add(double x) {
        count++;
        final double delta = x - mean;
        mean = mean + delta / count;
        s += delta * (x - mean);
    }

    /**
     * Removes the value {@code x} currently present in this sample. The sample count is decremented
     * by one by this operation.
     * 
     * @param x the value to remove
     */
    public void remove(double x) {
        if (count == 0) {
            throw new IllegalStateException("sample is empty");
        }
        final double deltaOld = x - mean;
        final double countMinus1 = count - 1;
        mean = count / countMinus1 * mean - x / countMinus1;
        final double deltaNew = x - mean;
        s -= deltaOld * deltaNew;
        count--;
    }

    /**
     * Replaces the value {@code x} currently present in this sample with the new value {@code y}.
     * In a sliding window, {@code x} is the value that drops out and {@code y} is the new value
     * entering the window. The sample count remains constant with this operation.
     * 
     * @param x the value to remove
     * @param y the value to add
     */
    public void replace(double x, double y) {
        if (count == 0) {
            throw new IllegalStateException("sample is empty");
        }
        final double deltaYX = y - x;
        final double deltaX = x - mean;
        final double deltaY = y - mean;
        mean = mean + deltaYX / count;
        final double deltaYp = y - mean;
        final long countMinus1 = count - 1;
        s = s - count * (deltaX * deltaX - deltaY * deltaYp) / countMinus1 - (deltaYX * deltaYp) / countMinus1;
    }

    /**
     * Returns the number of values in the sample.
     * 
     * @return the number of values in the sample
     */
    public long getCount() {
        return count;
    }

    /**
     * Returns the sum value of the sample. Returns 0 if the sample count is zero.
     * 
     * @return
     */
    public double getSum() {
        return mean * count;
    }

    /**
     * Returns the mean value of the sample. Returns 0 if the sample count is zero.
     * <p>
     * The method returns the calculated value and returns immediately.
     * 
     * @return the mean value of the sample
     */
    public double getMean() {
        return mean;
    }

    /**
     * Returns the variance of the sample using the {@code (n)} method. Returns NaN if count is 0.
     * <p>
     * The method is based on calculated values and returns almost immediately (involves a simple
     * division).
     * 
     * @return the biased variance of the sample
     */
    public double getVariance() {
        return s / count;// yes, this returns NaN if count==0
    }

    /**
     * Returns the variance of the sample using the {@code (n-1)} method. Returns 0 if the sample
     * count is zero, and Inf or NaN if count is 1.
     * <p>
     * The method is based on calculated values and returns almost immediately (involves a simple
     * division).
     * 
     * @return the variance of the sample (bias corrected)
     */
    public double getVarianceUnbiased() {
        return count > 0 ? s / (count - 1) : 0;// yes, this returns Inf if count==1
    }

    /**
     * Returns the standard deviation of the sample using the {@code (n)} method. Returns NaN if
     * count is 0.
     * <p>
     * The method is based on calculated values and returns almost immediately (involves a square
     * root and division operation).
     * 
     * @return the biased standard deviation of the sample
     */
    public double getStdDev() {
        return Math.sqrt(getVariance());
    }

    /**
     * Returns the standard deviation of the sample using the {@code (n-1)} method. Returns 0 if the
     * sample count is zero, and Inf or NaN if count is 1.
     * <p>
     * The method is based on calculated values and returns almost immediately (involves a square
     * root and division operation).
     * 
     * @return the standard deviation of the sample (bias corrected)
     */
    public double getStdDevUnbiased() {
        return Math.sqrt(getVarianceUnbiased());
    }

    /**
     * Resets this sampler to its initial state. The sample count is 0 after this operation.
     */
    public void reset() {
        count = 0;
        mean = 0;
        s = 0;
    }

    /**
     * Combines this sampler with the specified other sampler. After the operation, this sampler
     * reflects the combined mean, variance and standard deviation.
     * <p>
     * Combining samplers is sometimes useful e.g. if separate parts of a statistic are collected on
     * separate threads. The combine operation (maybe after calling clone) can be used to calculate
     * the combined statistics with a lower frequency (e.g. every 1000 data points).
     *
     * @param with the sampler with which this sampler is combined
     */
    public void combine(final OnlineStats with) {
        // e.g. see https://en.wikipedia.org/wiki/Standard_deviation#Combining_standard_deviations
        final long n1 = this.count;
        final long n2 = with.count;
        final double m1 = this.mean;
        final double m2 = with.mean;
        final double s1 = this.s;
        final double s2 = with.s;
        final long n = n1 + n2;
        final double m = (n1 * m1 + n2 * m2) / n;
        final double s = s1 + s2 + n1 * m1 * m1 + n2 * m2 * m2 - n * m * m;
        this.count = n;
        this.mean = m;
        this.s = s;
    }

    /**
     * Compute sigma of the specified value.
     * 
     * @param value
     * @return
     */
    public int calculateSigma(double value) {
        return (int) Math.floor(Math.abs(value - mean) / getStdDev());
    }

    /**
     * Compute sigma of the specified value.
     * 
     * @param value
     * @return
     */
    public double sigma(int value) {
        return mean + value * getStdDev();
    }
}