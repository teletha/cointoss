/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util.arithmetic;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import cointoss.util.ring.DoubleRingBuffer;
import kiss.WiseTriFunction;

public class OnlineAlgorithm {

    public static Collector<Double, ?, Double> sumDouble(int size) {
        return calculateDouble(size, (current, prev, now) -> current - prev + now);
    }

    public static <R> Collector<Double, ?, R> calculateDouble(int size, WiseTriFunction<R, Double, Double, R> calculator) {
        return new Collector<Double, DoubleRingBuffer, R>() {

            private R current;

            /**
             * {@inheritDoc}
             */
            @Override
            public Supplier<DoubleRingBuffer> supplier() {
                return () -> new DoubleRingBuffer(size);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public BiConsumer<DoubleRingBuffer, Double> accumulator() {
                return (buffer, v) -> {
                    current = calculator.apply(current, buffer.add(v), v);
                };
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public BinaryOperator<DoubleRingBuffer> combiner() {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Function<DoubleRingBuffer, R> finisher() {
                return v -> current;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Set<Characteristics> characteristics() {
                return Set.of();
            }
        };
    }
}