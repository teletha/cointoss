/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade;

import java.util.function.Function;

import cointoss.util.Num;
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
        return s -> s.take(Num.ZERO, (previous, current) -> previous.isLessThan(threshold) && current.isGreaterThanOrEqual(threshold));
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
    default Function<Signal<Num>, Signal<Num>> breakdown(Num threshold) {
        return s -> s.take(Num.ZERO, (previous, current) -> previous.isGreaterThan(threshold) && current.isLessThanOrEqual(threshold));
    }
}
