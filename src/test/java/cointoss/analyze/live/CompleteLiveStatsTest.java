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

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

public class CompleteLiveStatsTest extends LiveStatsTestBase {

    private static final int SAMPLE_COUNT = 10000; // Lots of thresholds need tuning if this is
                                                   // changed

    private static final Stats expovarMaxPes = new Stats("", 0, 0, 0, .0000001, 5, .02, 100, quantileMaxPes(.2, .1, .05, .02, .05, .01, .01));

    private static final Stats oneMaxPes = new Stats("", 0, 0, 0, 0, 0, 0, 0, quantileMaxPes(0, 0, 0, 0, 0, 0, 0));

    private static final Stats knownMaxPes = new Stats("", 0, 0, 0, .0000001, 30, 5, 300, quantileMaxPes(5, 20, 50, 50, 100, 100, 100));

    private static final Stats gaussianMaxPes = new Stats("", 0, 0, 0, .0000001, .2, 2, 500, quantileMaxPes(.2, .1, .2, .5, 1, 5, 20));

    private static final Stats uniformMaxPes = new Stats("", 0, 0, 0, .0000001, .2, .05, 200, quantileMaxPes(10, 20, 20, 10, .5, .02, .05));

    private static final Stats triangularMaxPes = new Stats("", 0, 0, 0, .0000001, .2, .00001, 2, quantileMaxPes(.2, .5, .2, .5, .5, 1, 2));

    private static final Stats bimodalMaxPes = new Stats("", 0, 0, 0, .0000001, 1, .01, 1, quantileMaxPes(.5, .2, .5, .2, .5, .5, 1));

    @Test
    public void testOnePoint() { // Doesn't use SAMPLE_COUNT
        test("One", DoubleStream.of(.02), oneMaxPes);
    }

    @Test
    public void testKnown() { // Doesn't use SAMPLE_COUNT
        final double[] test = {0.02, 0.15, 0.74, 3.39, 0.83, 22.37, 10.15, 15.43, 38.62, 15.92, 34.60, 10.28, 1.47, 0.40, 0.05, 11.39, 0.27,
                0.42, 0.09, 11.37};
        test("Known", Arrays.stream(test), knownMaxPes);
    }

    @Test
    public void testUniform() {
        final double[] ux = IntStream.range(0, SAMPLE_COUNT).asDoubleStream().toArray();
        Collections.shuffle(Arrays.asList(ux), ThreadLocalRandom.current()); // Shuffles the
                                                                             // underlying array
        test("Uniform", Arrays.stream(ux), uniformMaxPes);
    }

    @Test
    public void testGaussian() {
        test("Gaussian", DoubleStream.generate(ThreadLocalRandom.current()::nextGaussian), gaussianMaxPes);
    }

    @Test
    public void testExpovar() {
        final double lambda = 1.0 / 435;
        test("Expovar", ThreadLocalRandom.current().doubles().map(d -> Math.log(1. - d) / lambda), expovarMaxPes);
    }

    @Test
    public void testTriangular() {
        final DoubleStream tx = ThreadLocalRandom.current().doubles().map(triangular(-100 * SAMPLE_COUNT, 100 * SAMPLE_COUNT, 100));
        test("Triangular", tx, triangularMaxPes);
    }

    @Test
    public void testBimodal() {
        final Random r = ThreadLocalRandom.current();
        final DoubleStream bx = r.doubles().map(bimodal(r::nextBoolean, triangular(0, 1000, 500), triangular(500, 1500, 1400)));
        test("Bimodal", bx, bimodalMaxPes);
    }

    private LiveStats test(final String name, final DoubleStream dataStream, final Stats maxPes) {
        final double[] data = dataStream.limit(SAMPLE_COUNT).toArray();
        final LiveStats stats = new LiveStats(TEST_TILES);

        Arrays.stream(data).parallel().forEach(stats);
        final Stats live = new Stats(name, stats);

        final Stats real = calculateReal(name, Arrays.stream(data));

        assertEquals("name", real.name, live.name);
        assertEquals("count", real.n, live.n);
        for (double tile : TEST_TILES) {
            assertEquals("p" + tile + "%e", 0., calculateError(live.quantiles.get(tile), real.quantiles
                    .get(tile), real.max - real.min), maxPes.quantiles.get(tile));
        }
        assertEquals("min", real.min, live.min, maxPes.min);
        assertEquals("max", real.max, live.max, maxPes.max);
        assertEquals("mean%e", 0., calculateError(live.mean, real.mean, real.mean), maxPes.mean);
        assertEquals("variance%e", 0., calculateError(live.variance, real.variance, real.variance), maxPes.variance);
        assertEquals("skewness%e", 0., calculateError(live.skewness, real.skewness, real.max - real.min), maxPes.skewness);
        assertEquals("kurtosis%e", 0., calculateError(live.kurtosis, real.kurtosis, real.kurtosis), maxPes.kurtosis);

        return stats;
    }

}