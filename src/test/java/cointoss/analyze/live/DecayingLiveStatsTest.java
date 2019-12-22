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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

public class DecayingLiveStatsTest extends LiveStatsTestBase {

    private static final int SAMPLE_COUNT = 1000000; // Lots of thresholds need tuning if this is
                                                     // changed

    private static final DecayConfig decayConfig = new DecayConfig(.95);

    private static final Stats expovarMaxPes = new Stats("", 0, 300, 50, .2, 5, .02, 100, quantileMaxPes(.2, .1, .1, .05, .02, .02, .05));

    private static final Stats oneMaxPes = new Stats("", 0, 0, 0, 0, 0, 0, 0, quantileMaxPes(0, 0, 0, 0, 0, 0, 0));

    private static final Stats knownMaxPes = new Stats("", 0, 0, 0, .0000001, 30, 5, 300, quantileMaxPes(5, 20, 50, 50, 100, 100, 100));

    private static final Stats gaussianMaxPes = new Stats("", 0, 100, 100, .1, 1, 2, 20000, quantileMaxPes(.2, .2, .2, .2, 5, 5, 20));

    private static final Stats uniformMaxPes = new Stats("", 0, 5, 5, .2, .5, .000002, 1, quantileMaxPes(.5, .5, .5, .5, .1, .1, .02));

    private static final Stats bimodalMaxPes = new Stats("", 0, 50, 50, .2, 1, .01, 1, quantileMaxPes(.5, .5, .5, .2, .2, .5, 1));

    private static final Stats triangularMaxPes = new Stats("", 0, 20, 20, .2, 1, .00001, 2, quantileMaxPes(.5, .5, .5, .5, .5, 1, 2));

    private static final Stats bimodalThenTriangularMaxPes = new Stats("", 0, 50, 50, .2, 5, .05, 20, quantileMaxPes(.2, .2, .5, .5, .5, 1, 3));

    @Test
    public void testOnePoint() { // Doesn't use SAMPLE_COUNT
        test("One", DoubleStream.of(.02), oneMaxPes, Optional.empty());
    }

    @Test
    public void testKnown() {
        final double[] test = {0.02, 0.15, 0.74, 3.39, 0.83, 22.37, 10.15, 15.43, 38.62, 15.92, 34.60, 10.28, 1.47, 0.40, 0.05, 11.39, 0.27,
                0.42, 0.09, 11.37};
        test("Known", Arrays.stream(test), knownMaxPes, Optional.empty());
    }

    @Test
    public void testUniform() {
        final List<Double> ux = IntStream.range(0, SAMPLE_COUNT).asDoubleStream().collect(ArrayList::new, List::add, List::addAll);
        Collections.shuffle(ux, ThreadLocalRandom.current());
        test("Uniform", ux.stream().mapToDouble(x -> x), uniformMaxPes, Optional.empty());
    }

    @Test
    public void testGaussian() {
        test("Gaussian", DoubleStream.generate(ThreadLocalRandom.current()::nextGaussian), gaussianMaxPes, Optional.empty());
    }

    @Test
    public void testBimodalThenTriangular() {
        final Random r = ThreadLocalRandom.current();
        final DoubleStream bx = r.doubles()
                .map(bimodal(r::nextBoolean, triangular(0, 1000, 500), triangular(500, 1500, 1400)))
                .limit(SAMPLE_COUNT / 5);
        final double[] tx = r.doubles().map(triangular(500, 1000, 100)).limit(SAMPLE_COUNT * 4 / 5).toArray();
        test("BthenT", DoubleStream.concat(bx, Arrays.stream(tx)), bimodalThenTriangularMaxPes, Optional.of(Arrays.stream(tx)));
    }

    @Test
    public void testExpovar() {
        final double lambda = 1.0 / 435;
        test("Expovar", ThreadLocalRandom.current().doubles().map(d -> Math.log(1. - d) / lambda), expovarMaxPes, Optional.empty());
    }

    @Test
    public void testTriangular() {
        test("Triangular", ThreadLocalRandom.current()
                .doubles()
                .map(triangular(-100 * SAMPLE_COUNT, 100 * SAMPLE_COUNT, 100)), triangularMaxPes, Optional.empty());
    }

    @Test
    public void testBimodal() {
        final Random r = ThreadLocalRandom.current();
        test("Bimodal", r.doubles()
                .map(bimodal(r::nextBoolean, triangular(0, 1000, 500), triangular(500, 1500, 1400))), bimodalMaxPes, Optional.empty());
    }

    private Stats test(final String name, final DoubleStream dataStream, final Stats maxPes, final Optional<DoubleStream> expectedStream) {
        final double[] data = dataStream.limit(SAMPLE_COUNT).toArray();
        final LiveStats live = new LiveStats(decayConfig, TEST_TILES);

        for (int i = 0; i < data.length; i++) {
            if (i % (SAMPLE_COUNT / 200) == 0) {
                live.decay();
            }
            live.accept(data[i]);
        }
        final Stats captured = new Stats(name, live);
        final Stats real = calculateReal(name, expectedStream.map(s -> s.limit(SAMPLE_COUNT)).orElse(Arrays.stream(data)));
        assertEquals("name", real.name, captured.name);
        final double dataRange = live.decayedMaximum() - live.decayedMinimum();
        for (double tile : TEST_TILES) {
            assertEquals("p" + tile + "%e", 0., calculateError(captured.quantiles.get(tile), real.quantiles
                    .get(tile), dataRange), maxPes.quantiles.get(tile));
        }
        assertEquals("min", 0, calculateError(live.decayedMinimum(), real.min, dataRange), maxPes.min);
        assertEquals("max", 0, calculateError(live.decayedMaximum(), real.max, dataRange), maxPes.max);
        assertEquals("mean%e", 0., calculateError(captured.mean, real.mean, dataRange), maxPes.mean);
        assertEquals("variance%e", 0., calculateError(captured.variance, real.variance, real.variance), maxPes.variance);
        assertEquals("skewness%e", 0., calculateError(captured.skewness, real.skewness, dataRange), maxPes.skewness);
        assertEquals("kurtosis%e", 0., calculateError(captured.kurtosis, real.kurtosis, real.kurtosis), maxPes.kurtosis);
        return real;
    }
}