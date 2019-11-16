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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.annotations.VisibleForTesting;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.Market;
import cointoss.order.Order;
import cointoss.order.OrderStrategy.Makable;
import cointoss.order.OrderStrategy.Orderable;
import cointoss.order.OrderStrategy.Takable;
import cointoss.ticker.Span;
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
public abstract class Scenario extends ScenarioBase implements Directional {

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

    /** The debugging log. */
    private LinkedList<String> logs;

    /**
     * 
     */
    public Scenario() {
        // calculate profit
        disposerForExit.add(observeExitExecutedSize().to(size -> {
            setRealizedProfit(exitPrice.diff(directional, entryPrice).multiply(size));
        }));

        disposerForExit.add(observeEntryExecutedSize().first().to(this::exit));
        disposerForExit.add(observeExitExecutedSize().effectOnce(this::disposeEntry)
                .take(size -> size.is(entryExecutedSize))
                .first()
                .to(this::disposeExit));
    }

    /**
     * Dispose all entry related resources.
     */
    private void disposeEntry() {
        // cancel all remaining entry orders
        I.signal(entries).take(Order::isNotTerminated).flatMap(market::cancel).to(I.NoOP);
        disposerForEntry.dispose();
        log("Dispose entry.");
    }

    /**
     * Dispose all exit related resources.
     */
    private void disposeExit() {
        // cancel all remaining exit orders
        I.signal(exits).take(Order::isNotTerminated).flatMap(market::cancel).to(I.NoOP);
        disposerForExit.dispose();
        log("Dispose exit.");
    }

    /**
     * Check status of this {@link Scenario}.
     * 
     * @return A result.
     */
    public final boolean isActive() {
        return isTerminated() == false;
    }

    /**
     * Check status of this {@link Scenario}.
     * 
     * @return A result.
     */
    public final boolean isTerminated() {
        return isEntryTerminated() && isExitTerminated();
    }

    /**
     * Check status of this {@link Scenario}.
     * 
     * @return A result.
     */
    public final boolean isCanceled() {
        return isTerminated() && entryExecutedSize.isZero();
    }

    /**
     * Check status of this {@link Scenario}.
     * 
     * @return A result.
     */
    public final boolean isEntryTerminated() {
        return entries.stream().allMatch(Order::isTerminated);
    }

    /**
     * Check status of this {@link Scenario}.
     * 
     * @return A result.
     */
    public final boolean isExitTerminated() {
        if (entryExecutedSize.isZero() && isEntryTerminated()) {
            return exits.isEmpty() || exits.stream().allMatch(Order::isTerminated);
        } else {
            return exits.isEmpty() == false && exits.stream().allMatch(Order::isTerminated);
        }
    }

    /**
     * Compute position holding time.
     */
    public final Duration holdTime() {
        if (entryExecutedSize.isZero() && isEntryTerminated()) {
            return Duration.ZERO;
        }

        ZonedDateTime start = entries.first().map(o -> o.creationTime).or(Chrono.MIN);
        ZonedDateTime end = exits.isEmpty() ? market.service.now() : exits.last().map(o -> o.terminationTime).or(market.service.now());

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
        market.request(this.directional = directional, size, declaration).to(this::processEntryOrder);
    }

    /**
     * Process for additional entry order.
     * 
     * @param order
     */
    private void processEntryOrder(Order order) {
        entries.add(order);
        setEntrySize(entrySize.plus(order.size));
        logEntry("Process entry order");

        order.observeExecutedSize().to(v -> {
            updateOrderRelatedStatus(entries, this::setEntryPrice, this::setEntryExecutedSize);
            logEntry("Update entry order");
        });
    }

    /**
     * Calculate average price and total executed size.
     * 
     * @param orders
     * @param priceSetter
     * @param executedSizeSetter
     */
    private void updateOrderRelatedStatus(List<Order> orders, Consumer<Num> priceSetter, Consumer<Num> executedSizeSetter) {
        Num totalSize = Num.ZERO;
        Num totalPrice = Num.ZERO;

        for (Order order : orders) {
            totalSize = totalSize.plus(order.executedSize);
            totalPrice = totalPrice.plus(order.executedSize.multiply(order.price));
        }

        priceSetter.accept(totalPrice.divide(totalSize));
        executedSizeSetter.accept(totalSize);
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
        exitAt(price, entryPrice.isLessThan(directional, price) ? s -> s.make(price) : s -> s.take());
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
        exitAt(Variable.of(price), strategy);
    }

    /**
     * Declare exit order by price. Loss cutting is the only element in the trade that investors can
     * control.
     * 
     * @param price An exit price.
     */
    protected final void exitAt(Variable<Num> price, Consumer<Orderable> strategy) {
        if (entryPrice.isLessThan(directional, price)) {
            disposerForExit.add(observeEntryExecutedSizeDiff().debounce(1, SECONDS, market.service.scheduler()).to(size -> {
                market.request(directional.inverse(), entryExecutedSize.minus(exitSize), strategy).to(this::processExitOrder);
            }));
        } else {
            disposerForExit
                    .add(market.tickers.of(Span.Second5).add.take(e -> e.openPrice.isLessThanOrEqual(directional, price)).first().to(e -> {
                        disposeEntry();

                        market.request(directional.inverse(), entryExecutedSize.minus(exitExecutedSize), strategy)
                                .to(this::processExitOrder);
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
            market.request(directional.inverse(), entryExecutedSize.minus(exitExecutedSize), strategy).to(this::processExitOrder);
        }));
    }

    protected final Variable<Num> trailing(Function<Num, Num> trailer) {
        Variable<Num> trailedPrice = Variable.of(trailer.apply(entryPrice));
        disposerForExit.add(market.tickers.of(Span.Second5).add.map(tick -> Num.max(this, trailer.apply(tick.openPrice), trailedPrice.v))
                .to(trailedPrice));
        return trailedPrice;
    }

    protected final Variable<Num> trailing2(Function<Num, Num> trailer) {
        Variable variable = Variable.of(trailer.apply(entryPrice.minus(this, entryPrice)));
        disposerForExit.add(market.tickers.of(Span.Second5).add.map(v -> trailer.apply(entryPrice.minus(this, v.openPrice))).to(variable));
        return variable;
    }

    protected final Variable<Num> trailing3(Function<Num, Num> trailer) {
        Variable variable = Variable.of(trailer.apply(market.tickers.latestPrice.v));
        disposerForExit.add(market.tickers.of(Span.Second5).add.map(tick -> trailer.apply(tick.openPrice)).to(variable));
        return variable;
    }

    /**
     * Process for additional exit order.
     * 
     * @param order
     */
    private void processExitOrder(Order order) {
        exits.add(order);
        setExitSize(exitSize.plus(order.size));
        logExit("Process exit order");

        order.observeExecutedSize().to(v -> {
            updateOrderRelatedStatus(exits, this::setExitPrice, this::setExitExecutedSize);
            logExit("Update exit order");
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
        StringBuilder builder = new StringBuilder("Scenario ").append(directional)
                .append(" un/realized")
                .append(unrealizedProfit(market.tickers.latestPrice.v))
                .append("/")
                .append(realizedProfit)
                .append("\r\n");
        format(builder, "IN", entries, entryPrice, entrySize, entryExecutedSize, calculateCanceledSize(entries));
        format(builder, "OUT", exits, exitPrice, exitSize, exitExecutedSize, calculateCanceledSize(exits));
        if (logs != null) logs.forEach(log -> builder.append("\t").append(log).append("\r\n"));
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

    /**
     * Enable log for this {@link Scenario}.
     */
    protected final void enableLog() {
        if (logs == null) {
            logs = new LinkedList();
        }
    }

    /**
     * Check log availability.
     * 
     * @return
     */
    protected final boolean isEnableLog() {
        return logs != null;
    }

    /**
     * Write log message.
     * 
     * @param message
     */
    protected final void log(String message) {
        if (logs != null) {
            logs.add(message);
        }
    }

    /**
     * Write entry info as log message.
     * 
     * @param message
     */
    protected final void logEntry(String message) {
        log(message + " " + entryExecutedSize + "/" + entrySize + "@" + entryPrice);
    }

    /**
     * Write exit info as log message.
     * 
     * @param message
     */
    protected final void logExit(String message) {
        log(message + " " + exitExecutedSize + "/" + exitSize + "@" + exitPrice);
    }
}