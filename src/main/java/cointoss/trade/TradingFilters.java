/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade;

import java.time.ZonedDateTime;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import cointoss.ticker.Tick;
import cointoss.util.arithmetic.Num;
import kiss.Signal;

/**
 * Define Chart Domain Specific Filters.
 */
public interface TradingFilters {

    /**
     * Timezone restriction.
     */
    Predicate<Tick> InNewYorkTime = tick -> {
        ZonedDateTime time = tick.date();
        int hour = time.getHour();
        return 12 <= hour && hour < 21;
    };

    /**
     * Timezone restriction.
     */
    Predicate<Tick> InTokyoTime = tick -> {
        ZonedDateTime time = tick.date();
        int hour = time.getHour();
        return 0 <= hour && hour < 8;
    };

    /**
     * Timezone restriction.
     */
    Predicate<Tick> InLondonTime = tick -> {
        ZonedDateTime time = tick.date();
        int hour = time.getHour();
        return 7 <= hour && hour < 17;
    };

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
     * Provides a filter that allows only when the value exceeds the specified value.
     * 
     * @param threshold
     * @return A value transition filter.
     */
    default Function<Signal<Double>, Signal<Double>> breakupDouble(Supplier<Double> threshold) {
        return s -> s.take(0d, (previous, current) -> {
            double num = threshold.get();
            return previous < num && current >= num;
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
    default Function<Signal<Double>, Signal<Double>> breakdownDouble(Supplier<Double> threshold) {
        return s -> s.take(0d, (previous, current) -> {
            double num = threshold.get();
            return previous > num && current <= num;
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