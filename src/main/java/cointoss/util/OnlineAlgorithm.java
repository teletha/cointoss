/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import cointoss.util.ring.RingBuffer;
import kiss.WiseTriFunction;

public class OnlineAlgorithm {

    public static Collector<Double, ?, Double> sumDouble(int size) {
        return calculate(size, (current, prev, now) -> current - prev + now);
    }

    public static <T, R> Collector<T, ?, R> calculate(int size, WiseTriFunction<R, T, T, R> calculator) {
        return new Collector<T, RingBuffer<T>, R>() {

            private R current;

            /**
             * {@inheritDoc}
             */
            @Override
            public Supplier<RingBuffer<T>> supplier() {
                return () -> new RingBuffer<>(size);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public BiConsumer<RingBuffer<T>, T> accumulator() {
                return (buffer, v) -> {
                    current = calculator.apply(current, buffer.add(v), v);
                };
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public BinaryOperator<RingBuffer<T>> combiner() {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Function<RingBuffer<T>, R> finisher() {
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
