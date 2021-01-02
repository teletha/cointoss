/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade;

import java.util.function.Function;
import java.util.function.Supplier;

import cointoss.util.arithmetic.Num;
import kiss.Signal;

/**
 * Define Chart Domain Specific Filters.
 */
public interface TradingFilters {

    /**
     * Provides a filter that allows only when the value exceeds the specified value.
     * 
     * @param threshold
     * @return A value transition filter.
     */
    default Function<Signal<Num>, Signal<Num>> breakup(double threshold) {
        return breakup(Num.of(threshold));
    }

    /**
     * Provides a filter that allows only when the value exceeds the specified value.
     * 
     * @param threshold
     * @return A value transition filter.
     */
    default Function<Signal<Num>, Signal<Num>> breakup(Num threshold) {
        return breakup(() -> threshold);
    }

    /**
     * Provides a filter that allows only when the value exceeds the specified value.
     * 
     * @param threshold
     * @return A value transition filter.
     */
    default Function<Signal<Num>, Signal<Num>> breakup(Supplier<Num> threshold) {
        return s -> s.take(Num.ZERO, (previous, current) -> {
            Num num = threshold.get();
            return previous.isLessThan(num) && current.isGreaterThanOrEqual(num);
        });
    }

    /**
     * Provides a filter that allows only when the value falls below the specified value.
     * 
     * @param threshold
     * @return A value transition filter.
     */
    default Function<Signal<Double>, Signal<Double>> breakdownDouble(double threshold) {
        return s -> s.take(0d, (previous, current) -> previous > threshold && current <= threshold);
    }

    /**
     * Provides a filter that allows only when the value exceeds the specified value.
     * 
     * @param threshold
     * @return A value transition filter.
     */
    default Function<Signal<Double>, Signal<Double>> breakupDouble(double threshold) {
        return s -> s.take(0d, (previous, current) -> previous < threshold && current >= threshold);
    }

    /**
     * Provides a filter that allows only when the value falls below the specified value.
     * 
     * @param threshold
     * @return A value transition filter.
     */
    default Function<Signal<Num>, Signal<Num>> breakdown(double threshold) {
        return breakdown(Num.of(threshold));
    }

    /**
     * Provides a filter that allows only when the value falls below the specified value.
     * 
     * @param threshold
     * @return A value transition filter.
     */
    default Function<Signal<Num>, Signal<Num>> breakdown(double threshold, double gap) {
        return breakdown(Num.of(threshold), Num.of(gap));
    }

    /**
     * Provides a filter that allows only when the value falls below the specified value.
     * 
     * @param threshold
     * @return A value transition filter.
     */
    default Function<Signal<Num>, Signal<Num>> breakdown(Num threshold) {
        return breakdown(() -> threshold);
    }

    /**
     * Provides a filter that allows only when the value falls below the specified value.
     * 
     * @param threshold
     * @return A value transition filter.
     */
    default Function<Signal<Num>, Signal<Num>> breakdown(Supplier<Num> threshold) {
        return s -> s.take(Num.ZERO, (previous, current) -> {
            Num num = threshold.get();
            return previous.isGreaterThan(num) && current.isLessThanOrEqual(num);
        });
    }

    /**
     * Provides a filter that allows only when the value falls below the specified value.
     * 
     * @param threshold
     * @return A value transition filter.
     */
    default Function<Signal<Num>, Signal<Num>> breakdown(Num threshold, Num gap) {
        return s -> s.take(Num.ZERO, (previous, current) -> previous.isGreaterThan(threshold) && current.isLessThanOrEqual(threshold));
    }
}