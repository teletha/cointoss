/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.RandomStringUtils;

import cointoss.util.Num;

/**
 * @version 2018/04/23 23:34:34
 */
public class MarketTestSupport {

    /** The execution id manager. */
    private static final AtomicLong executionId = new AtomicLong();

    /**
     * Create {@link Execution}.
     * 
     * @param price
     * @param size
     * @return
     */
    public static Execution buy(double price, double size) {
        return buy(Num.of(price), Num.of(size));
    }

    /**
     * Create {@link Execution}.
     * 
     * @param price
     * @param size
     * @return
     */
    public static Execution buy(Num price, Num size) {
        return execution(Side.BUY, price, size);
    }

    /**
     * Create {@link Execution}.
     * 
     * @param price
     * @param size
     * @return
     */
    public static Execution sell(double price, double size) {
        return sell(Num.of(price), Num.of(size));
    }

    /**
     * Create {@link Execution}.
     * 
     * @param price
     * @param size
     * @return
     */
    public static Execution sell(Num price, Num size) {
        return execution(Side.SELL, price, size);
    }

    /**
     * Create {@link Execution}.
     * 
     * @param side
     * @param price
     * @param size
     * @return
     */
    public static Execution execution(Side side, Num price, Num size) {
        Execution exe = new Execution();
        exe.id = executionId.getAndIncrement();
        exe.side = Objects.requireNonNull(side);
        exe.price = Objects.requireNonNull(price);
        exe.size = Objects.requireNonNull(size);
        exe.exec_date = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        exe.buy_child_order_acceptance_id = RandomStringUtils.randomAlphabetic(10);
        exe.sell_child_order_acceptance_id = RandomStringUtils.randomAlphabetic(10);

        return exe;
    }
}
