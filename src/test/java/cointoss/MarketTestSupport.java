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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import cointoss.util.Chrono;
import cointoss.util.Generator;
import cointoss.util.Num;
import kiss.I;
import kiss.Signal;

/**
 * @version 2018/05/23 17:41:24
 */
public class MarketTestSupport {

    /** The base time. */
    public static final ZonedDateTime BaseDate = Chrono.utcNow().truncatedTo(ChronoUnit.DAYS);

    /** The execution id manager. */
    private static final AtomicLong executionId = new AtomicLong(1); // don't use 0

    /**
     * Create {@link Execution}.
     * 
     * @param price
     * @param size
     * @return
     */
    public static ChainableExecution buy(double price, double size) {
        return buy(Num.of(price), Num.of(size));
    }

    /**
     * Create {@link Execution}.
     * 
     * @param price
     * @param size
     * @return
     */
    public static ChainableExecution buy(Num price, Num size) {
        return execution(Side.BUY, price, size);
    }

    /**
     * Create {@link Execution}.
     * 
     * @param price
     * @param size
     * @return
     */
    public static ChainableExecution sell(double price, double size) {
        return sell(Num.of(price), Num.of(size));
    }

    /**
     * Create {@link Execution}.
     * 
     * @param price
     * @param size
     * @return
     */
    public static ChainableExecution sell(Num price, Num size) {
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
    public static ChainableExecution execution(Side side, double price, double size) {
        return execution(side, Num.of(price), Num.of(size));
    }

    /**
     * Create {@link Execution}.
     * 
     * @param side
     * @param price
     * @param size
     * @return
     */
    public static ChainableExecution execution(Side side, Num price, Num size) {
        ChainableExecution exe = new ChainableExecution();
        exe.id = executionId.getAndIncrement();
        exe.side = Objects.requireNonNull(side);
        exe.price = Objects.requireNonNull(price);
        exe.size = exe.cumulativeSize = Objects.requireNonNull(size);
        exe.date = BaseDate;

        return exe;
    }

    /**
     * Create {@link Execution}.
     * 
     * @param price
     * @param size
     * @return
     */
    public static List<Execution> executionSerially(int count, Side side, double price, double size) {
        List<Execution> list = new ArrayList();

        for (int i = 0; i < count; i++) {
            Execution e = execution(side, price, size);
            if (i != 0) e.consecutive = side.isBuy() ? Execution.ConsecutiveSameBuyer : Execution.ConsecutiveSameSeller;
            list.add(e);
        }
        return list;
    }

    /**
     * Create {@link Execution}.
     * 
     * @param count A number of executions.
     * @return
     */
    public static Signal<Execution> executionRandomly(int count) {
        List<Execution> list = new ArrayList();

        for (int i = 0; i < count; i++) {
            list.add(execution(Side.random(), Generator.randomInt(1, 10), Generator.randomInt(1, 10)));
        }
        return I.signal(list);
    }

    /**
     * Create {@link Position}.
     * 
     * @param side
     * @param price
     * @param size
     * @return
     */
    public static Position position(Side side, double price, double size) {
        return position(side, Num.of(price), Num.of(size));
    }

    /**
     * Create {@link Position}.
     * 
     * @param side
     * @param price
     * @param size
     * @return
     */
    public static Position position(Side side, Num price, Num size) {
        Position position = new Position();
        position.date = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        position.side = Objects.requireNonNull(side);
        position.price = Objects.requireNonNull(price);
        position.size.set(Objects.requireNonNull(size));

        return position;
    }

    /**
     * @version 2018/05/23 17:38:03
     */
    public static class ChainableExecution extends Execution {

        /**
         * Assign id.
         * 
         * @param id The id to assign.
         * @return Chainable API.
         */
        public ChainableExecution id(long id) {
            this.id = id;

            return this;
        }

        /**
         * Assign the consecutive type.
         * 
         * @param A consective type to assign.
         * @return Chainable API.
         */
        public ChainableExecution consecutive(int type) {
            this.consecutive = type;

            return this;
        }

        /**
         * Assign the delay time.
         * 
         * @param A delay time to assign.
         * @return Chainable API.
         */
        public ChainableExecution delay(int delay) {
            this.delay = delay;

            return this;
        }

        /**
         * Assign date.
         * 
         * @param year A year value.
         * @param month A month value.
         * @param day A day value.
         * @param hour A hour value.
         * @param minute A minute value.
         * @param second A second value.
         * @param ms A millsecond value.
         * @return Chainable API.
         */
        public ChainableExecution date(int year, int month, int day, int hour, int minute, int second, int ms) {
            this.date = ZonedDateTime.of(year, month, day, hour, minute, second, ms * 1000000, Chrono.UTC);

            return this;
        }

        /**
         * Assign date.
         * 
         * @param time
         * @return
         */
        public ChainableExecution date(ZonedDateTime time) {
            this.date = Objects.requireNonNull(time);

            return this;
        }
    }
}
