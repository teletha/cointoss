/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package hypatia;

import org.decimal4j.immutable.Decimal8f;

import antibug.profiler.Benchmark;
import hypatia.Num;

public class BigDecimalBenchmark {

    public static void main(String[] args) {
        Benchmark benchmark = new Benchmark();

        benchmark.measure("Num", () -> {
            return hypatia.Num.of(1000)
                    .divide(hypatia.Num.of(4))
                    .plus(hypatia.Num.of(0.123))
                    .multiply(hypatia.Num.of(-24.5))
                    .pow(2);
        });

        benchmark.measure("Num#calculate", () -> {
            return hypatia.Num.of(1000).calculate(Num.of(4), Num.of(0.123), Num.of(-24.5), (p0, p1, p2, p3) -> {
                double v = ((p0 / p1) + p2) * p3;
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

        benchmark.measure("Java Primitive", () -> {
            double v = (1000 / 4 + 0.123) * -24.5;
            return v * v;
        });

        benchmark.measure("Java BigDecimal", () -> {
            return java.math.BigDecimal.valueOf(1000)
                    .divide(java.math.BigDecimal.valueOf(4))
                    .add(java.math.BigDecimal.valueOf(0.123))
                    .multiply(java.math.BigDecimal.valueOf(-24.5))
                    .pow(2);
        });

        benchmark.perform();
    }
}