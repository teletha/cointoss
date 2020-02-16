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

import static java.time.temporal.ChronoUnit.*;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;

import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.order.OrderStrategy.Orderable;
import cointoss.trade.extension.PricePart;
import cointoss.trade.extension.ScenePart;
import cointoss.trade.extension.SidePart;
import cointoss.trade.extension.SizePart;
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

    /**
     * Create entry order.
     * 
     * @param size A order size.
     * @param strategy The order strategy.
     * @return A created {@link Scenario}.
     */
    protected final Scenario entry(int size, Consumer<Orderable> strategy) {
        return entry(Direction.BUY, size, strategy);
    }

    /**
     * Create entry order.
     * 
     * @param side A order side.
     * @param size A order size.
     * @param strategy The order strategy.
     * @return A created {@link Scenario}.
     */
    protected final Scenario entry(Directional side, int size, Consumer<Orderable> strategy) {
        return entry(side, Num.of(size), strategy);
    }

    /**
     * Create entry order.
     * 
     * @param side A order side.
     * @param size A order size.
     * @param strategy The order strategy.
     * @return A created {@link Scenario}.
     */
    protected final Scenario entry(Directional side, Num size, Consumer<Orderable> strategy) {
        when(now(), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(side, size, strategy);
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
     * Execute entry order completelly.
     */
    protected final void executeEntryAll() {
        Order o = latest().entries.get(0);
        executeEntry(o.size, o.price);
    }

    /**
     * Execute entry order partially.
     */
    protected final void executeEntryHalf() {
        Order o = latest().entries.get(0);
        executeEntry(o.size.divide(2), o.price);
    }

    /**
     * Execute entry order partially.
     */
    protected final void executeEntry(int size, int price) {
        executeEntry(Num.of(size), Num.of(price));
    }

    /**
     * Execute entry order partially.
     */
    private void executeEntry(Num size, Num price) {
        Scenario s = latest();

        market.perform(Execution.with.direction(s.inverse(), size).price(price.minus(s, 1)).date(market.service.now()));
        awaitOrderBufferingTime();
    }

    /**
     * Execute exit order completelly.
     */
    protected final void executeExitAll() {
        Order o = latest().exits.get(0);
        executeExit(o.size, o.price);
    }

    /**
     * Execute exit order partially.
     */
    protected final void executeExitHalf() {
        Order o = latest().exits.get(0);
        executeExit(o.size.divide(2), o.price);
    }

    /**
     * Execute exit order partially.
     */
    protected final void executeExit(int size, int price) {
        executeExit(Num.of(size), Num.of(price));
    }

    /**
     * Execute exit order partially.
     */
    private void executeExit(Num size, Num price) {
        Scenario s = latest();

        market.perform(Execution.with.direction(s.direction(), size).price(price.minus(s.inverse(), 1)).date(market.service.now()));
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

    /**
     * Cancel entry.
     */
    protected final void cancelExit() {
        Scenario s = latest();
        for (Order e : s.exits) {
            market.cancel(e);
        }
        awaitOrderBufferingTime();
    }

    protected final Scenario build(ScenePart scene) {
        return build(scene, null, null, null);
    }

    protected final Scenario build(ScenePart scene, SidePart side) {
        return build(scene, side, null, null);
    }

    protected final Scenario build(ScenePart scene, SizePart size) {
        return build(scene, null, size, null);
    }

    protected final Scenario build(ScenePart scene, SidePart side, SizePart size, PricePart price) {
        if (side == null) {
            side = new SidePart(Direction.BUY);
        }

        if (size == null) {
            size = new SizePart(2);
        }

        Scenario s = null;
        PricePart p = price != null ? price : new PricePart(10, 20);

        switch (scene) {
        case Entry:
            s = entry(side, size, o -> o.make(p.entry));
            break;

        case EntryPartially:
            s = entry(side, size, o -> o.make(p.entry));
            executeEntryHalf();
            break;

        case EntryCompletely:
            s = entry(side, size, o -> o.make(p.entry));
            executeEntryAll();
            break;

        case EntryCancelled:
            s = entry(side, size, o -> o.make(p.entry).cancelAfter(1, MINUTES));
            market.elapse(1, MINUTES);
            break;

        case EntryPartiallyCancelled:
            s = entry(side, size, o -> o.make(p.entry));
            executeEntryHalf();
            cancelEntry();
            break;

        case Exit:
            s = entry(side, size, o -> o.make(p.entry));
            executeEntryAll();
            exit(o -> o.make(p.exit));
            break;

        case ExitPartially:
            s = entry(side, size, o -> o.make(p.entry));
            executeEntryAll();
            exit(o -> o.make(p.exit));
            executeExitHalf();
            break;

        case ExitCompletely:
            s = entry(side, size, o -> o.make(p.entry));
            executeEntryAll();
            exit(o -> o.make(p.exit));
            executeExitAll();
            break;

        case ExitCancelled:
            s = entry(side, size, o -> o.make(p.entry));
            executeEntryAll();
            exit(o -> o.make(p.exit));
            cancelExit();
            break;

        case ExitPartiallyCancelled:
            s = entry(side, size, o -> o.make(p.entry));
            executeEntryAll();
            exit(o -> o.make(p.exit));
            executeExitHalf();
            cancelExit();
            break;

        case EntryPartiallyAndExitCompletely:
            s = entry(side, size, o -> o.make(p.entry));
            executeEntryHalf();
            exit(o -> o.make(p.exit));
            executeExitAll();
            break;
        }
        return s;
    }
}
