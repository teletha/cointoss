/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.google.common.annotations.VisibleForTesting;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.Market;
import cointoss.order.Order;
import cointoss.order.OrderState;
import cointoss.order.OrderStrategy.Makable;
import cointoss.order.OrderStrategy.Orderable;
import cointoss.order.OrderStrategy.Takable;
import cointoss.util.Chrono;
import cointoss.util.LinkedQueue;
import cointoss.util.Num;
import kiss.Disposable;
import kiss.I;
import kiss.Signal;
import kiss.Variable;

/**
 * Declarative entry and exit definition.
 */
public abstract class Scenario extends EntryStatus implements Directional {

    /** The scenario direction. */
    private Directional directional;

    /** The target market. */
    protected Market market;

    /** The fund management for this scenario. */
    protected FundManager funds;

    /** The list entry orders. */
    @VisibleForTesting
    final LinkedQueue<Order> entries = new LinkedQueue<>();

    /** The list exit orders. */
    @VisibleForTesting
    final LinkedQueue<Order> exits = new LinkedQueue<>();

    /** The entry disposer. */
    private final Disposable disposerForEntry = Disposable.empty();

    /** The exit disposer. */
    private final Disposable disposerForExit = Disposable.empty();

    /**
     * 
     */
    public Scenario() {
        disposerForExit.add(observeEntryExecutedSize().first().to(this::exit));

        // calculate profit
        disposerForExit.add(observeExitExecutedSize().effectOnce(this::disposeEntry).to(size -> {
            setRealizedProfit(exitPrice.diff(directional, entryPrice).multiply(size));
        }));

        observeExitExecutedSize().take(size -> size.is(entryExecutedSize)).first().to(() -> {
            disposeExit();
        });
    }

    /**
     * Dispose all entry related resources.
     */
    private void disposeEntry() {
        // cancel all remaining entry orders
        I.signal(entries).take(Order::isNotTerminated).flatMap(market::cancel).to(I.NoOP);
        disposerForEntry.dispose();
    }

    /**
     * Dispose all exit related resources.
     */
    private void disposeExit() {
        // cancel all remaining exit orders
        I.signal(exits).take(Order::isNotTerminated).flatMap(market::cancel).to(I.NoOP);
        disposerForExit.dispose();
    }

    /**
     * Check {@link OrderState} of this entry.
     * 
     * @return A result.
     */
    public final boolean isActive() {
        return isTerminated() == false;
    }

    /**
     * Check {@link OrderState} of this entry.
     * 
     * @return A result.
     */
    public final boolean isTerminated() {
        return isEntryTerminated() && isExitTerminated();
    }

    /**
     * Check {@link OrderState} of this entry.
     * 
     * @return A result.
     */
    public final boolean isEntryTerminated() {
        return entries.stream().allMatch(Order::isTerminated);
    }

    /**
     * Check {@link OrderState} of this entry.
     * 
     * @return A result.
     */
    public final boolean isExitTerminated() {
        return exits.isEmpty() == false && exits.stream().allMatch(Order::isTerminated);
    }

    /**
     * Compute position holding time.
     */
    public final Duration holdTime() {
        ZonedDateTime start = entries.first().map(o -> o.creationTime).or(Chrono.MIN);
        ZonedDateTime end = exits.last().map(o -> o.terminationTime).or(market.service.now());

        return Duration.between(start, end);
    }

    /**
     * Declare entry order.
     */
    protected abstract void entry();

    /**
     * We will order with the specified quantity. Use the return the {@link Takable} &
     * {@link Makable} value to define the details of the ordering method.
     * 
     * @param <S> Ordering interface
     * @param size A entry size.
     * @return A ordering method.
     */
    protected final void entry(Directional directional, long size, Consumer<Orderable> declaration) {
        entry(directional, Num.of(size), declaration);
    }

    /**
     * We will order with the specified quantity. Use the return the {@link Takable} &
     * {@link Makable} value to define the details of the ordering method.
     * 
     * @param <S> Ordering interface
     * @param size A entry size.
     * @return A ordering method.
     */
    protected final void entry(Directional directional, double size, Consumer<Orderable> declaration) {
        entry(directional, Num.of(size), declaration);
    }

    /**
     * We will order with the specified quantity. Use the return the {@link Takable} &
     * {@link Makable} value to define the details of the ordering method.
     * 
     * @param <S> Ordering interface
     * @param size A entry size.
     * @return A ordering method.
     */
    protected final void entry(Directional directional, Num size, Consumer<Orderable> declaration) {
        if (size == null || size.isLessThan(market.service.setting.targetCurrencyMinimumBidSize)) {
            throw new Error("Entry size is less than minimum bid size.");
        }
        market.request(this.directional = directional, size, declaration).to(this::processAddEntryOrder);
    }

    /**
     * Process for additional entry order.
     * 
     * @param order
     */
    private void processAddEntryOrder(Order order) {
        entries.add(order);
        setEntrySize(entrySize.plus(order.size));

        order.observeExecutedSize().to(v -> {
            updateOrderRelatedStatus(entries, this::setEntryPrice, this::setEntryExecutedSize);
        });
    }

    /**
     * Calculate average price and total executed size.
     * 
     * @param orders
     */
    private void updateOrderRelatedStatus(List<Order> orders, Consumer<Num> priceSetter, Consumer<Num> sizeSetter) {
        Num totalSize = Num.ZERO;
        Num totalPrice = Num.ZERO;

        for (Order order : orders) {
            totalSize = totalSize.plus(order.executedSize);
            totalPrice = totalPrice.plus(order.executedSize.multiply(order.price));
        }

        priceSetter.accept(totalPrice.divide(totalSize));
        sizeSetter.accept(totalSize);

    }

    /**
     * Declare exit order. Loss cutting is the only element in the trade that investors can control.
     */
    protected abstract void exit();

    /**
     * Declare exit order by price. Loss cutting is the only element in the trade that investors can
     * control.
     * 
     * @param time
     * @param unit
     */
    protected final void exitAfter(long time, TimeUnit unit) {
        exitWhen(I.signal(time, 0, unit, market.service.scheduler()).first(), Orderable::take);
    }

    /**
     * Declare exit order by price. Loss cutting is the only element in the trade that investors can
     * control.
     * 
     * @param price An exit price.
     */
    protected final void exitAt(long price) {
        exitAt(Num.of(price));
    }

    /**
     * Declare exit order by price. Loss cutting is the only element in the trade that investors can
     * control.
     * 
     * @param price An exit price.
     */
    protected final void exitAt(double price) {
        exitAt(Num.of(price));
    }

    /**
     * Declare exit order by price. Loss cutting is the only element in the trade that investors can
     * control.
     * 
     * @param price An exit price.
     */
    protected final void exitAt(Num price) {
        exitAt(Variable.of(price));
    }

    /**
     * Declare exit order by price. Loss cutting is the only element in the trade that investors can
     * control.
     * 
     * @param price An exit price.
     */
    protected final void exitAt(Variable<Num> price) {
        if (entryPrice.isLessThan(directional, price)) {
            disposerForExit.add(observeEntryExecutedSizeDiff().debounce(1, SECONDS, market.service.scheduler()).to(size -> {
                market.request(directional.inverse(), entryExecutedSize.minus(exitSize), s -> s.make(price)).to(this::processAddExitOrder);
            }));
        } else {
            disposerForExit.add(market.tickers.latest.observe().take(e -> e.price.isLessThanOrEqual(directional, price)).first().to(e -> {
                Num v = entryExecutedSize.minus(exitExecutedSize);

                if (v.isNegativeOrZero()) {
                    System.out.println("The exit executed size exceeds the entr executed size.\r\n" + toString());
                }

                market.request(directional.inverse(), entryExecutedSize.minus(exitExecutedSize), Orderable::take)
                        .to(this::processAddExitOrder);
            }));
        }
    }

    /**
     * Declare exit order by price. Loss cutting is the only element in the trade that investors can
     * control.
     * 
     * @param price An exit price.
     */
    protected final void exitAt(long price, Consumer<Orderable> strategy) {
        exitAt(Num.of(price), strategy);
    }

    /**
     * Declare exit order by price. Loss cutting is the only element in the trade that investors can
     * control.
     * 
     * @param price An exit price.
     */
    protected final void exitAt(double price, Consumer<Orderable> strategy) {
        exitAt(Num.of(price), strategy);
    }

    /**
     * Declare exit order by price. Loss cutting is the only element in the trade that investors can
     * control.
     * 
     * @param price An exit price.
     */
    protected final void exitAt(Num price, Consumer<Orderable> strategy) {
        if (price.isGreaterThan(directional, entryPrice)) {
            disposerForExit
                    .add(market.tickers.latest.observe().take(e -> e.price.isGreaterThanOrEqual(directional, price)).first().to(e -> {
                        market.request(directional.inverse(), entryExecutedSize.minus(exitExecutedSize), strategy)
                                .to(this::processAddExitOrder);
                    }));
        } else {
            disposerForExit.add(market.tickers.latest.observe().take(e -> e.price.isLessThanOrEqual(directional, price)).first().to(e -> {
                market.request(directional.inverse(), entryExecutedSize.minus(exitExecutedSize), strategy).to(this::processAddExitOrder);
            }));
        }
    }

    /**
     * Declare exit order
     * 
     * @param <S>
     * @param timing
     * @param strategy
     */
    protected final void exitWhen(Signal<?> timing, Consumer<Orderable> strategy) {
        disposerForExit.add(timing.first().to(() -> {
            market.request(directional.inverse(), entryExecutedSize.minus(exitExecutedSize), strategy).to(this::processAddExitOrder);
        }));
    }

    /**
     * Process for additional exit order.
     * 
     * @param order
     */
    private void processAddExitOrder(Order order) {
        exits.add(order);
        setExitSize(exitSize.plus(order.size));

        order.observeExecutedSize().to(v -> {
            updateOrderRelatedStatus(exits, this::setExitPrice, this::setExitExecutedSize);
        });

        order.observeTerminating().to(() -> {
            Num remains = entryExecutedSize.minus(exitExecutedSize);

            if (remains.isPositive()) {
                market.request(directional.inverse(), remains, s -> s.take()).to(this::processAddExitOrder);
            }
        });
    }

    /**
     * 
     * 
     */
    protected final void exitAtRiskRewardRatio() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Direction direction() {
        return directional.direction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Scenario ").append(directional).append("\r\n");
        format(builder, "IN", entries, entryPrice, entrySize, entryExecutedSize, calculateCanceledSize(entries));
        format(builder, "OUT", exits, exitPrice, exitSize, exitExecutedSize, calculateCanceledSize(exits));
        return builder.toString();
    }

    private Num calculateCanceledSize(List<Order> orders) {
        return I.signal(orders).scanWith(Num.ZERO, (v, o) -> v.plus(o.canceledSize())).to().or(Num.ZERO);
    }

    private void format(StringBuilder builder, String type, List<Order> orders, Num price, Num size, Num executedSize, Num canceledSize) {
        builder.append("\t")
                .append(type)
                .append("\t order ")
                .append(orders.size())
                .append("\tprice ")
                .append(price.scale(market.service.setting.baseCurrencyScaleSize))
                .append("\t size ")
                .append(executedSize + "/" + size.minus(canceledSize) + "(" + canceledSize + ")\r\n");

        for (Order order : orders) {
            builder.append("\t\t ").append(order).append("\r\n");
        }
    }
}