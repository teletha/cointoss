/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.arithmetic;

import org.decimal4j.immutable.Decimal8f;

import antibug.profiler.Benchmark;

public class BigDecimalBenchmark {

    public static void main(String[] args) {
        Benchmark benchmark = new Benchmark();

        benchmark.measure("Num", () -> {
            return cointoss.util.arithmetic.Num.of(1000)
                    .divide(cointoss.util.arithmetic.Num.of(4))
                    .plus(cointoss.util.arithmetic.Num.of(0.123))
                    .multiply(cointoss.util.arithmetic.Num.of(-24.5))
                    .pow(2);
        });

        benchmark.measure("Num#calculate", () -> {
            return cointoss.util.arithmetic.Num.of(1000).calculate(Num.of(4), Num.of(0.123), Num.of(-24.5), (p0, p1, p2, p3) -> {
                long v = ((p0 / p1) + p2) * p3;
                return v * v;
            });
        });

        benchmark.measure("Decimal4J", () -> {
            return Decimal8f.valueOf(1000)
                    .divide(Decimal8f.valueOf(4))
                    .add(Decimal8f.valueOf(0.123))
                    .multiply(Decimal8f.valueOf(-24.5))
                    .pow(2);
        });

        benchmark.measure("JavaBigDecimal", () -> {
            return java.math.BigDecimal.valueOf(1000)
                    .divide(java.math.BigDecimal.valueOf(4))
                    .add(java.math.BigDecimal.valueOf(0.123))
                    .multiply(java.math.BigDecimal.valueOf(-24.5))
                    .pow(2);
        });

        benchmark.perform();
    }
}
