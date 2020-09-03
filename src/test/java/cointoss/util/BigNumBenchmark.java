/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import org.decimal4j.immutable.Decimal8f;

import antibug.profiler.Benchmark;
import cointoss.util.decimal.Decimal;

public class BigNumBenchmark {

    public static void main(String[] args) {
        Benchmark benchmark = new Benchmark();

        benchmark.measure("JavaBigDecimal", () -> {
            return java.math.BigDecimal.valueOf(1000)
                    .divide(java.math.BigDecimal.valueOf(4))
                    .add(java.math.BigDecimal.valueOf(0.123))
                    .multiply(java.math.BigDecimal.valueOf(-24.5))
                    .pow(2);
        });

        // benchmark.measure("GWTBigDecimal", () -> {
        // return cointoss.util.math.BigDecimal.valueOf(1000)
        // .divide(cointoss.util.math.BigDecimal.valueOf(4))
        // .add(cointoss.util.math.BigDecimal.valueOf(0.123))
        // .multiply(cointoss.util.math.BigDecimal.valueOf(-24.5))
        // .pow(2);
        // });
        //
        // benchmark.measure("ICU4JBigDecimal", () -> {
        // return com.ibm.icu.math.BigDecimal.valueOf(1000)
        // .divide(com.ibm.icu.math.BigDecimal.valueOf(4))
        // .add(com.ibm.icu.math.BigDecimal.valueOf(0.123))
        // .multiply(com.ibm.icu.math.BigDecimal.valueOf(-24.5))
        // .pow(BigDecimal.valueOf(2));
        // });

        benchmark.measure("Decimal4J", () -> {
            return Decimal8f.valueOf(1000)
                    .divide(Decimal8f.valueOf(4))
                    .add(Decimal8f.valueOf(0.123))
                    .multiply(Decimal8f.valueOf(-24.5))
                    .pow(2);
        });

        benchmark.measure("Decimal", () -> {
            return Decimal.of(1000).divide(Decimal.of(4)).plus(Decimal.of(0.123)).multiply(Decimal.of(-24.5)).pow(2);
        });

        benchmark.perform();
    }
}
