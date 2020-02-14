/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;

import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.order.OrderStrategy.Orderable;
import cointoss.util.Num;
import cointoss.util.TimebaseSupport;
import cointoss.verify.VerifiableMarket;
import kiss.I;
import kiss.Signal;

public abstract class TraderTestSupport extends Trader implements TimebaseSupport {

    protected VerifiableMarket market = new VerifiableMarket();

    private ZonedDateTime base;

    /**
     * @param provider
     */
    public TraderTestSupport() {

        market.register(this);
    }

    @BeforeEach
    void setup() {
        initialize(market);
        market.service.clear();

        base = market.service.now();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZonedDateTime baseTime() {
        return base;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void declare(Market market, FundManager fund) {
    }

    /**
     * Timing function.
     * 
     * @return
     */
    protected final Signal<?> now() {
        return I.signal("ok");
    }

    /**
     * Elapse time for order buffering.
     */
    protected final void awaitOrderBufferingTime() {
        market.elapse(1, ChronoUnit.SECONDS);
    }

    /**
     * Shorthand method to entry.
     * 
     * @param eentry
     * @param exit
     */
    protected final void entry(Execution e) {
        entryPartial(e, e.size.doubleValue());
    }

    /**
     * Shorthand method to entry.
     * 
     * @param eentry
     * @param exit
     */
    protected final void entryPartial(Execution e, double partialEntrySize) {
        when(now(), v -> new Scenario() {

            @Override
            protected void entry() {
                entry(e, e.size, s -> s.make(e.price));
            }

            @Override
            protected void exit() {
            }
        });
        market.perform(Execution.with.direction(e.direction, partialEntrySize).price(e.price.minus(e, 1)).date(e.date));
        awaitOrderBufferingTime();
    }

    /**
     * Shorthand method to entry and exit.
     * 
     * @param eentry
     * @param exit
     */
    protected final void entryAndExit(Execution e, Execution exit) {
        entryPartialAndExit(e, e.size.doubleValue(), exit);
    }

    /**
     * Shorthand method to entry and exit.
     * 
     * @param eentry
     * @param exit
     */
    protected final void entryAndExitPartial(Execution e, Execution exit, double partialExitSize) {
        entryPartialAndExitPartial(e, e.size.doubleValue(), exit, partialExitSize);
    }

    /**
     * Shorthand method to entry and exit.
     * 
     * @param eentry
     * @param exit
     */
    protected final void entryPartialAndExit(Execution e, double partialEntrySize, Execution exit) {
        entryPartialAndExitPartial(e, partialEntrySize, exit, exit.size.doubleValue());
    }

    /**
     * Shorthand method to entry and exit.
     * 
     * @param eentry
     * @param exit
     */
    protected final void entryPartialAndExitPartial(Execution e, double partialEntrySize, Execution exit, double partialExitSize) {
        when(now(), v -> new Scenario() {

            @Override
            protected void entry() {
                entry(e, e.size, s -> s.make(e.price));
            }

            @Override
            protected void exit() {
                exitWhen(now(), s -> s.make(exit.price));
            }
        });

        market.perform(Execution.with.direction(e.direction, partialEntrySize).price(e.price.minus(e, 1)).date(e.date));
        awaitOrderBufferingTime();
        market.perform(Execution.with.direction(e.inverse(), partialExitSize).price(exit.price.minus(e.inverse(), 1)).date(exit.date));
        awaitOrderBufferingTime();
    }

    // ========================================
    // Execution Helper
    // ========================================
    protected final Scenario entry(int size, Consumer<Orderable> strategy) {
        when(now(), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.BUY, size, strategy);
            }

            @Override
            protected void exit() {
            }
        });
        return latest();
    }

    protected final Scenario exit(Consumer<Orderable> strategy) {
        Scenario s = latest();
        s.exitWhen(now(), strategy);
        return latest();
    }

    /**
     * Execution for entry.
     * 
     * @param size
     * @param price
     */
    protected final void executeEntry(int size, int price) {
        Scenario s = latest();
        market.perform(Execution.with.direction(s.direction().inverse(), size)
                .price(Num.of(price).minus(s.direction(), 1))
                .date(market.service.now()));
        awaitOrderBufferingTime();
    }

    /**
     * Execution for exit.
     * 
     * @param size
     * @param price
     */
    protected final void executeExit(int size, int price) {
        Scenario s = latest();
        market.perform(Execution.with.direction(s.direction(), size)
                .price(Num.of(price).minus(s.direction().inverse(), 1))
                .date(market.service.now()));
        awaitOrderBufferingTime();
    }

    /**
     * Cancel entry.
     */
    protected final void cancelEntry() {
        Scenario s = latest();
        for (Order entry : s.entries) {
            market.cancel(entry);
        }
        awaitOrderBufferingTime();
    }
}
