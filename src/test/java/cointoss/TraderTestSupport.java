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

    protected final void entry(Execution e) {
        when(now(), v -> new Scenario() {

            @Override
            protected void entry() {
                entry(e, e.size, s -> s.make(e.price));
            }

            @Override
            protected void exit() {
            }
        });
        market.perform(Execution.with.direction(e.direction, e.size).price(e.price.minus(e, 1)).date(e.date));
        awaitOrderBufferingTime();
    }

    /**
     * Shorthand method to entry and exit.
     * 
     * @param eentry
     * @param exit
     */
    protected final void entryAndExit(Execution e, Execution exit) {
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

        market.perform(Execution.with.direction(e.direction, e.size).price(e.price.minus(e, 1)).date(e.date));
        awaitOrderBufferingTime();
        market.perform(Execution.with.direction(e.inverse(), exit.size).price(exit.price.minus(e.inverse(), 1)).date(exit.date));
        awaitOrderBufferingTime();
    }
}
