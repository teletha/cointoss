/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade;

import static java.time.temporal.ChronoUnit.*;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.Market;
import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.order.OrderStrategy.Orderable;
import cointoss.trade.extension.HoldTimePart;
import cointoss.trade.extension.PricePart;
import cointoss.trade.extension.ScenePart;
import cointoss.trade.extension.SidePart;
import cointoss.trade.extension.SizePart;
import cointoss.trade.extension.StrategyPart;
import cointoss.trade.extension.TradePart;
import cointoss.util.Chrono;
import cointoss.util.Num;
import cointoss.util.TimebaseSupport;
import cointoss.verify.VerifiableMarket;
import kiss.I;
import kiss.Signal;

public abstract class TraderTestSupport extends Trader implements TimebaseSupport {

    protected VerifiableMarket market;

    private ZonedDateTime base;

    /**
     * @param provider
     */
    public TraderTestSupport() {
    }

    @BeforeEach
    void setup() {
        market = new VerifiableMarket();
        market.register(this);

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
        Order o = latest().entries.peekFirst();
        executeEntry(o.size, o.price);
    }

    /**
     * Execute entry order partially.
     */
    protected final void executeEntryHalf() {
        Order o = latest().entries.peekFirst();
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
    }

    /**
     * Execute exit order completelly.
     */
    protected final void executeExitAll() {
        Order o = latest().exits.peekFirst();
        executeExit(o.size, o.price);
    }

    /**
     * Execute exit order partially.
     */
    protected final void executeExitHalf() {
        Order o = latest().exits.peekFirst();
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
    }

    /**
     * Build the specified trade scene.
     * 
     * @param parts The trade scene.
     * @return A new created {@link Scenario}.
     */
    protected final Scenario scenario(TradePart... parts) {
        ScenePart scene = ScenePart.ExitCompletely;
        SidePart side = new SidePart(Direction.BUY);
        SizePart size = new SizePart(2);
        PricePart price = new PricePart(10, 20);
        HoldTimePart hold = new HoldTimePart(0);
        StrategyPart strategy = StrategyPart.Make;

        for (TradePart part : parts) {
            if (part instanceof ScenePart) {
                scene = (ScenePart) part;
            } else if (part instanceof SidePart) {
                side = (SidePart) part;
            } else if (part instanceof SizePart) {
                size = (SizePart) part;
            } else if (part instanceof PricePart) {
                price = (PricePart) part;
            } else if (part instanceof HoldTimePart) {
                hold = (HoldTimePart) part;
            } else if (part instanceof StrategyPart) {
                strategy = (StrategyPart) part;
            }
        }
        return scenario(scene, side, size, price, hold, strategy);
    }

    /**
     * Build the specified trade scene.
     * 
     * @param scene A target trade scene.
     * @param side An entry side.
     * @param size An entry size.
     * @param price An entry and exit price
     * @return A new created {@link Scenario}.
     */
    private final Scenario scenario(ScenePart scene, SidePart side, SizePart size, PricePart price, HoldTimePart hold, StrategyPart strategy) {
        Scenario s = null;

        switch (scene) {
        case Entry:
            s = entry(side, size, o -> o.make(price.entry));
            break;

        case EntryPartially:
            s = entry(side, size, o -> o.make(price.entry));
            execute(true, side, size.halfN, price.entryN, 0);
            break;

        case EntryCompletely:
            s = entry(side, size, o -> o.make(price.entry));
            execute(true, side, size, price.entryN, 0);
            break;

        case EntryMultiple:
            s = entry(side, size, o -> o.make(price.entry));
            execute(true, side, size.halfN, price.entryN, 0);
            execute(true, side, size.halfN, price.entryN, 0);
            break;

        case EntrySeparately:
            s = entry(side, size, o -> o.make(price.entry));
            execute(true, side, size.halfN, price.entryN, 0);
            market.elapse(30, SECONDS);
            execute(true, side, size.halfN, price.entryN, 0);
            break;

        case EntryCanceled:
            s = entry(side, size, o -> o.make(price.entry));
            cancelEntry();
            break;

        case EntryPartiallyCanceled:
            s = entry(side, size, o -> o.make(price.entry));
            execute(true, side, size.halfN, price.entryN, 0);
            cancelEntry();
            break;

        case Exit:
            s = entry(side, size, o -> o.make(price.entry));
            execute(true, side, size, price.entryN, 0);
            market.elapse(hold.sec);
            exit(o -> o.make(price.exit));
            break;

        case ExitPartially:
            s = entry(side, size, o -> o.make(price.entry));
            execute(true, side, size, price.entryN, 0);
            market.elapse(hold.sec);
            exit(o -> o.make(price.exit));
            execute(true, side.inverse(), size.halfN, price.exitN, 0);
            break;

        case ExitCompletely:
            s = entry(side, size, o -> o.make(price.entry));
            execute(true, side, size, price.entryN, 0);
            market.elapse(hold.sec);
            exit(o -> o.make(price.exit));
            execute(true, side.inverse(), size, price.exitN, 0);
            break;

        case ExitMultiple:
            s = entry(side, size, o -> o.make(price.entry));
            execute(true, side, size, price.entryN, 0);
            market.elapse(hold.sec);
            exit(o -> o.make(price.exit));
            execute(true, side.inverse(), size.halfN, price.exitN, 0);
            execute(true, side.inverse(), size.halfN, price.exitN, 0);
            break;

        case ExitSeparately:
            s = entry(side, size, o -> o.make(price.entry));
            execute(true, side, size, price.entryN, 0);
            market.elapse(hold.sec);
            exit(o -> o.make(price.exit));
            execute(true, side.inverse(), size.halfN, price.exitN, 0);
            market.elapse(30, SECONDS);
            execute(true, side.inverse(), size.halfN, price.exitN, 0);
            break;

        case ExitCanceled:
            s = entry(side, size, o -> o.make(price.entry));
            execute(true, side, size, price.entryN, 0);
            market.elapse(hold.sec);
            exit(o -> o.make(price.exit));
            cancelExit();
            break;

        case ExitCanceledThenOtherExit:
            s = entry(side, size, o -> o.make(price.entry));
            execute(true, side, size, price.entryN, 0);
            market.elapse(hold.sec);
            exit(o -> o.make(price.exit).cancelAfter(0, MINUTES).make(price.exit));
            break;

        case ExitCanceledThenOtherExitCompletely:
            s = entry(side, size, o -> o.make(price.entry));
            execute(true, side, size, price.entryN, 0);
            market.elapse(hold.sec);
            exit(o -> o.make(price.exit).cancelAfter(0, MINUTES).make(price.exit));
            execute(true, side.inverse(), size, price.exitN, 0);
            break;

        case ExitPartiallyCancelled:
            s = entry(side, size, o -> o.make(price.entry));
            execute(true, side, size, price.entryN, 0);
            market.elapse(hold.sec);
            exit(o -> o.make(price.exit));
            execute(true, side.inverse(), size.halfN, price.exitN, 0);
            break;

        case EntryPartiallyCanceledAndExitCompletely:
            s = entry(side, size, o -> o.make(price.entry));
            execute(true, side, size.halfN, price.entryN, 0);
            cancelEntry();
            market.elapse(hold.sec);
            exit(o -> o.make(price.exit));
            execute(true, side.inverse(), size, price.exitN, 0);
            break;
        }
        return s;
    }

    /**
     * Perform exection.
     * 
     * @param make
     * @param side
     * @param size
     * @param price
     * @param sec
     */
    private void execute(boolean make, Directional side, Num size, Num price, long sec) {
        ZonedDateTime time = Chrono.MIN.plusSeconds(sec);

        if (make) {
            Num p = price.divide(2);
            if (side.isBuy()) {
                p = price.minus(p);
            } else {
                p = price.plus(p);
            }
            market.perform(Execution.with.direction(side, size).price(p).date(time));
        } else {
            market.perform(Execution.with.direction(side, size).price(price).date(time));
        }
    }

    /**
     * Cancel all entry orders.
     */
    private void cancelEntry() {
        for (Order order : latest().entries) {
            market.cancel(order).to(I.NoOP);
        }
    }

    /**
     * Cancel all entry orders.
     */
    private void cancelExit() {
        for (Order order : latest().exits) {
            market.cancel(order).to(I.NoOP);
        }
    }
}