/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.execution;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import cointoss.Direction;
import cointoss.Market;
import cointoss.util.Chrono;
import cointoss.util.Num;
import cointoss.verify.TimeLag;
import cointoss.verify.VerifiableMarket;

/**
 * Chainable API for {@link Execution}.
 */
public class Executing extends Execution {

    /** The execution id manager. */
    private static final AtomicLong executionId = new AtomicLong(1); // don't use 0

    /**
     * Assign executed price.
     * 
     * @param price An executed price.
     * @return Chainable API.
     */
    public final Executing price(long price) {
        return price(Num.of(price));
    }

    /**
     * Assign executed price.
     * 
     * @param price An executed price.
     * @return Chainable API.
     */
    public final Executing price(double price) {
        return price(Num.of(price));
    }

    /**
     * Assign executed price.
     * 
     * @param price An executed price.
     * @return Chainable API.
     */
    public final Executing price(Num price) {
        this.price = price;

        return this;
    }

    /**
     * Assign execution id.
     * 
     * @param id An executed id.
     * @return Chainable API.
     */
    public final Executing id(long id) {
        this.id = id;

        return this;
    }

    /**
     * Set executed date.
     * 
     * @param date An executed date.
     * @return Chainable API.
     */
    public final Executing date(int year, int month, int day, int hour, int minute, int second, int ms) {
        return date(ZonedDateTime.of(year, month, day, hour, minute, second, ms * 1000000, Chrono.UTC));
    }

    /**
     * Assign the delay time.
     * 
     * @param A delay time to assign.
     * @return Chainable API.
     */
    public final Executing delay(int delay) {
        this.delay = delay;

        return this;
    }

    /**
     * Assign the consecutive type.
     * 
     * @param A consective type to assign.
     * @return Chainable API.
     */
    public final Executing consecutive(int type) {
        this.consecutive = type;

        return this;
    }

    /**
     * Set executed date.
     * 
     * @param date An executed date.
     * @return Chainable API.
     */
    public final Executing date(ZonedDateTime date) {
        this.date = date;

        return this;
    }

    /**
     * Execute on the specified market.
     * 
     * @param market A target {@link Market}.
     */
    public final void on(VerifiableMarket market) {
        market.perform(this);
    }

    /**
     * Create new buying {@link Execution}.
     * 
     * @param size A execution size.
     * @return Chainable API.
     */
    public static Executing buy(long size) {
        return buy(Num.of(size));
    }

    /**
     * Create new buying {@link Execution}.
     * 
     * @param size A execution size.
     * @return Chainable API.
     */
    public static Executing buy(double size) {
        return buy(Num.of(size));
    }

    /**
     * Create new buying {@link Execution}.
     * 
     * @param size A execution size.
     * @return Chainable API.
     */
    public static Executing buy(Num size) {
        return of(Direction.BUY, size);
    }

    /**
     * Create new selling {@link Execution}.
     * 
     * @param size A execution size.
     * @return Chainable API.
     */
    public static Executing sell(long size) {
        return sell(Num.of(size));
    }

    /**
     * Create new selling {@link Execution}.
     * 
     * @param size A execution size.
     * @return Chainable API.
     */
    public static Executing sell(double size) {
        return sell(Num.of(size));
    }

    /**
     * Create new selling {@link Execution}.
     * 
     * @param size A execution size.
     * @return Chainable API.
     */
    public static Executing sell(Num size) {
        return of(Direction.SELL, size);
    }

    /**
     * Create new {@link Execution}.
     * 
     * @param direction An execution direction.
     * @param size An execution size.
     * @return Chainable API.
     */
    public static Executing of(Direction direction, Num size) {
        Executing e = new Executing();
        e.id = executionId.getAndIncrement();
        e.side = direction;
        e.size = e.cumulativeSize = size;
        e.date = TimeLag.Base;

        return e;
    }

    /**
     * Create the specified numbers of {@link Execution}.
     * 
     * @param numbers
     * @return
     */
    public static List<Execution> random(int numbers) {
        List<Execution> list = new ArrayList();

        for (int i = 0; i < numbers; i++) {
            Executing e = Executing.of(Direction.random(), Num.random(1, 10)).price(Num.random(1, 10));
            list.add(e);
        }

        return list;
    }

    /**
     * Create the sequence of {@link Execution}s.
     * 
     * @param size
     * @param price
     * @return
     */
    public static List<Execution> sequence(int count, Direction side, double size, double price) {
        List<Execution> list = new ArrayList();

        for (int i = 0; i < count; i++) {
            Executing e = Executing.of(side, Num.of(size)).price(price);
            if (i != 0) e.consecutive = side.isBuy() ? ConsecutiveSameBuyer : ConsecutiveSameSeller;
            list.add(e);
        }
        return list;
    }
}
