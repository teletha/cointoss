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

import org.junit.jupiter.api.BeforeEach;

import cointoss.execution.Execution;
import cointoss.verify.VerifiableMarket;
import kiss.I;
import kiss.Signal;

public abstract class TraderTestSupport extends Trader {

    protected VerifiableMarket market;

    protected ZonedDateTime base;

    /**
     * @param provider
     */
    public TraderTestSupport() {
        super(new VerifiableMarket());

        this.market = (VerifiableMarket) super.market;
    }

    @BeforeEach
    void initialize() {
        market.service.clear();
        scenarios.clear();

        base = market.service.now();
    }

    /**
     * Return the latest completed or canceled entry.
     * 
     * @return
     */
    protected final Scenario latest() {
        return scenarios.getLast();
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
     * Config delay.
     * 
     * @param delay
     * @return
     */
    protected final ZonedDateTime second(long delay) {
        return market.service.now().plusSeconds(delay);
    }

    /**
     * Build date time.
     * 
     * @param time
     * @param unit
     * @return
     */
    protected final ZonedDateTime after(long time, ChronoUnit unit) {
        return market.service.now().plus(time, unit);
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
}
