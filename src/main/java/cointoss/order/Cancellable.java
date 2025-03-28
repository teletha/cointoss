/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import cointoss.ticker.Span;
import kiss.I;
import kiss.Signal;

/**
 * Cancelling order strategy.
 */
public interface Cancellable {

    /**
     * Cancel the order if it remains after the specified time has passed.
     * 
     * @param time A time value.
     * @param unit A time unit.
     * @return
     */
    default Orderable cancelAfter(long time, ChronoUnit unit) {
        return cancelAfter(time, TimeUnit.of(unit));
    }

    /**
     * Cancel the order if it remains after the specified time has passed.
     * 
     * @param time A time value.
     * @param unit A time unit.
     * @return
     */
    default Orderable cancelAfter(long time, TimeUnit unit) {
        return cancelWhen(scheduler -> I.schedule(time, unit, scheduler), "Cancel order after " + time + " " + unit + ".");
    }

    /**
     * Cancel the order if it remains after the specified time has passed.
     * 
     * @param duration A time until canceling.
     * @return
     */
    default Orderable cancelAfter(Duration duration) {
        return cancelAfter(duration.toSeconds(), ChronoUnit.SECONDS);
    }

    /**
     * Cancel the order if it remains after the specified time has passed.
     * 
     * @param span A time until canceling.
     * @return
     */
    default Orderable cancelAfter(Span span) {
        return cancelAfter(span.duration);
    }

    /**
     * Cancel the order if it remains after the specified time has passed.
     * 
     * @param timing A timing to cancel order.
     * @return
     */
    default Orderable cancelWhen(Signal<?> timing) {
        return cancelWhen(timing, "Cancel order when the specifid timing.");
    }

    /**
     * Cancel the order if it remains after the specified time has passed.
     * 
     * @param timing A timing to cancel order.
     * @return
     */
    default Orderable cancelWhen(Signal<?> timing, String description) {
        return cancelWhen(scheduler -> timing, description);
    }

    /**
     * Cancel the order if it remains after the specified time has passed.
     * 
     * @param timing A timing to cancel order.
     * @return
     */
    default Orderable cancelWhen(Function<ScheduledExecutorService, Signal<?>> timing) {
        return cancelWhen(timing, "Cancel order when the specified timing.");
    }

    /**
     * Cancel the order if it remains after the specified time has passed.
     * 
     * @param timing A timing to cancel order.
     * @return
     */
    Orderable cancelWhen(Function<ScheduledExecutorService, Signal<?>> timing, String description);
}