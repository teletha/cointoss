/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade;

import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import cointoss.Market;
import kiss.Disposable;
import kiss.Signal;
import kiss.Signaling;

public abstract class Trader {

    /** The market. */
    protected final Market market;

    /** The fund management. */
    protected final FundManager funds;

    /** The signal observers. */
    final Signaling<Boolean> completeEntries = new Signaling();

    /** The trade related signal. */
    protected final Signal<Boolean> completingEntry = completeEntries.expose;

    /** The signal observers. */
    final Signaling<Boolean> completeExits = new Signaling();

    /** The trade related signal. */
    protected final Signal<Boolean> completingExit = completeExits.expose;

    /** All managed entries. */
    private final LinkedList<Entry> entries = new LinkedList<>();

    /** The alive state. */
    private final AtomicBoolean enable = new AtomicBoolean(true);

    /** The disposer manager. */
    private final Disposable disposer = Disposable.empty();

    /**
     * Declare your strategy.
     * 
     * @param market A target market to deal.
     */
    protected Trader(Market market) {
        this.market = Objects.requireNonNull(market);
        this.funds = FundManager.with.totalAssets(market.service.baseCurrency().first().to().v);
    }

    /**
     * Add {@link Entry}.
     * 
     * @param entry An entry to add.
     */
    protected final void add(Entry entry) {
        if (entry != null) {
            entries.add(entry);
        }
    }

    /**
     * Return the latest completed or canceled entry.
     * 
     * @return
     */
    protected final Entry latest() {
        return entries.peekLast();
    }

    /**
     * Set up entry at your timing.
     * 
     * @param <T>
     * @param timing
     * @param builder
     */
    protected final <T> void entryWhen(Signal<T> timing, Function<T, Entry> builder) {
        if (timing == null || builder == null) {
            return;
        }

        disposer.add(timing.takeWhile(v -> enable.get()).to(value -> {
            Entry entry = builder.apply(value);

            if (entry != null) {
                entry.trader = this;
                entries.add(entry);
            }
        }));
    }
}
