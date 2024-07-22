/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.trade;

import static java.util.concurrent.TimeUnit.*;

import java.math.RoundingMode;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;

import cointoss.Direction;
import cointoss.Market;
import cointoss.order.Makable;
import cointoss.order.Order;
import cointoss.order.OrderState;
import cointoss.order.Orderable;
import cointoss.order.Takable;
import cointoss.ticker.Span;
import cointoss.util.Chrono;
import hypatia.Num;
import hypatia.Orientational;
import kiss.Disposable;
import kiss.I;
import kiss.Signal;
import kiss.Variable;

/**
 * Declarative entry and exit definition.
 */
public abstract class Scenario extends ScenarioBase implements Orientational<Direction>, Disposable, TradingEntry, TradingExit {

    /** The scenario direction. */
    private Orientational<Direction> directional;

    /** The target market. */
    protected Market market;

    /** The fund management for this scenario. */
    protected Funds funds;

    /** The parent {@link Trader}. */
    Trader trader;

    /** The logger name. */
    String logger;

    /** The entry disposer. */
    private final Disposable disposerForEntry = Disposable.empty();

    /** The exit disposer. */
    private final Disposable disposerForExit = Disposable.empty();

    /** The debugging log. */
    private LinkedList<String> logs;

    /** The scenario's state. */
    public final Variable<OrderState> state = Variable.of(OrderState.INIT);

    /**
     * 
     */
    public Scenario() {
        enableLog();

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
        I.signal(entries).take(Order::isNotTerminated).flatMap(market.orders::cancel).to(I.NoOP);
        disposerForEntry.dispose();
        log("Dispose entry.");
    }

    /**
     * Dispose all exit related resources.
     */
    private void disposeExit() {
        // cancel all remaining exit orders
        I.signal(exits).take(Order::isNotTerminated).flatMap(market.orders::cancel).to(I.NoOP);
        disposerForExit.dispose();
        log("Dispose exit.");
        state.set(OrderState.COMPLETED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Market market() {
        return market;
    }

    /**
     * Check status of this {@link Scenario}.
     * 
     * @return A result.
     */
    public final boolean isActive() {
        return state.is(OrderState.ACTIVE);
    }

    /**
     * Check status of this {@link Scenario}.
     * 
     * @return A result.
     */
    public final boolean isTerminated() {
        return state.is(OrderState.COMPLETED) || state.is(OrderState.CANCELED);
    }

    /**
     * Check status of this {@link Scenario}.
     * 
     * @return A result.
     */
    public final boolean isNotCancelled() {
        return state.isNot(OrderState.CANCELED);
    }

    /**
     * Check status of this {@link Scenario}.
     * 
     * @return A result.
     */
    public final boolean isEntryTerminated() {
        return !entries.isEmpty() && entries.stream().allMatch(Order::isTerminated);
    }

    /**
     * Check status of this {@link Scenario}.
     * 
     * @return A result.
     */
    public final boolean isExitTerminated() {
        if (!isEntryTerminated()) {
            return false;
        }

        if (entryExecutedSize.isZero()) {
            return true;
        }

        if (exitExecutedSize.isLessThan(entryExecutedSize)) {
            return false;
        }

        return !exits.isEmpty() && exits.stream().allMatch(Order::isTerminated);
    }

    /**
     * Observe entry state.
     * 
     * @return
     */
    public final Signal<Boolean> observeEntryTerminated() {
        return I.signal(entries).flatMap(Order::observeStateNow).map(x -> entries.stream().allMatch(Order::isTerminated));
    }

    /**
     * Observe exit state.
     * 
     * @return
     */
    public final Signal<Boolean> observeExitTerminated() {
        return I.signal(exits).flatMap(Order::observeStateNow).map(x -> exits.stream().allMatch(Order::isTerminated));
    }

    /**
     * Compute position holding time.
     */
    public final Duration holdTime() {
        if (entryExecutedSize.isZero() && isEntryTerminated()) {
            return Duration.ZERO;
        }
        return Duration.between(holdStartTime(), holdEndTime());
    }

    /**
     * Compute the time when this {@link Scenario} has first position.
     * 
     * @return
     */
    public final ZonedDateTime holdStartTime() {
        return Variable.of(entries.peekFirst()).map(o -> o.creationTime).or(() -> Chrono.MIN);
    }

    /**
     * Compute the time when this {@link Scenario} ends last position.
     * 
     * @return
     */
    public final ZonedDateTime holdEndTime() {
        return Variable.of(exits.peekLast()).map(o -> o.terminationTime).or(market.service::now);
    }

    /**
     * Declare entry order.
     */
    protected abstract void entry();

    /**
     * We will order with the specified quantity. Use the return the {@link Takable} &
     * {@link Makable} value to define the details of the ordering method.
     * 
     * @param size A entry size.
     * @return A ordering method.
     */
    @Override
    public final Scenario entry(Orientational directional, Num size, Consumer<Orderable> declaration) {
        if (size == null || size.isLessThan(market.service.setting.target.minimumSize)) {
            throw new Error("Entry size is less than minimum bid size.");
        }
        market.request(this.directional = directional, size, declaration).to(this::processEntryOrder);

        return this;
    }

    /**
     * Process for additional entry order.
     * 
     * @param order
     */
    private void processEntryOrder(Order order) {
        entries.add(order);
        updateOrderRelatedStatus(entries, this::setEntryPrice, this::setEntrySize, Order::size, this::setEntryCommission);
        logEntry("Launch entry");
        if (state.is(OrderState.INIT)) state.set(OrderState.ACTIVE);
        disposerForEntry.add(order.observeState().take(OrderState.CANCELED).to(() -> {
            // If all entries have been cancelled without being executed, then this scenario
            // is considered cancelled.
            if (entryExecutedSize.isZero() && entries.stream().allMatch(Order::isCanceled)) {
                state.set(OrderState.CANCELED);
            }
        }));

        order.observeExecutedSize().to(v -> {
            Num deltaSize = v.minus(entryExecutedSize);

            updateOrderRelatedStatus(entries, this::setEntryPrice, this::setEntryExecutedSize, Order::executedSize, this::setEntryCommission);
            trader.updateSnapshot(orientation(), Num.ZERO, deltaSize, order.price);

            logEntry("Update entry ");
        });
    }

    /**
     * Calculate average price, total executed size and total commisision.
     * 
     * @param orders
     * @param priceSetter
     * @param sizeSetter
     */
    private void updateOrderRelatedStatus(Deque<Order> orders, Consumer<Num> priceSetter, Consumer<Num> sizeSetter, Function<Order, Num> sizeExtractor, Consumer<Num> commissionSetter) {
        Num totalSize = Num.ZERO;
        Num totalPrice = Num.ZERO;
        Num totalCommision = Num.ZERO;

        for (Order order : orders) {
            Num size = sizeExtractor.apply(order);
            totalSize = totalSize.plus(size);
            totalPrice = totalPrice.plus(size.multiply(order.price));
            totalCommision = totalCommision.plus(order.commission);
        }

        priceSetter.accept(totalPrice.divide(totalSize).scale(this, market.service.setting.base.scale));
        sizeSetter.accept(totalSize);
        commissionSetter.accept(totalCommision);
    }

    /**
     * Declare exit order. Loss cutting is the only element in the trade that investors can control.
     */
    protected abstract void exit();

    /**
     * {@inheritDoc}
     */
    @Override
    public final void exitAt(Variable<Num> price, Consumer<Orderable> strategy) {
        Disposable disposer = Disposable.empty();
        if (entryPrice.isLessThan(directional, price)) {
            // profit
            disposer = observeEntryExecutedSizeDiff().debounceAll(1, SECONDS, market.service.scheduler()).map(Num::sum).to(size -> {
                market.request(directional.orientation().inverse(), size, strategy).to(o -> {
                    processExitOrder(o, "exitAt");
                });
            });
        } else {
            // losscut
            market.priceMatcher.register(price, directional, () -> {
                disposeEntry();

                market.request(orientation().inverse(), entryExecutedSize.minus(exitExecutedSize), strategy).to(o -> {
                    processExitOrder(o, "exitAtStopLoss");
                });
            });
        }
        disposerForExit.add(disposer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void exitWhen(Signal<?> timing, double ratio, Consumer<Orderable> strategy) {
        disposerForExit.add(timing.first().to(() -> {
            if (!isExitTerminated()) {
                Num exitRatio = Num.between(Num.ZERO, Num.of(ratio), Num.ONE);
                Num actualRequestSize = Num.min(remainingSize(), entryExecutedSize.multiply(exitRatio));
                market.request(directional.orientation().inverse(), actualRequestSize, strategy).to(e -> {
                    processExitOrder(e, "exitWhen");
                });
            }
        }));
    }

    /**
     * Process for additional exit order.
     * 
     * @param order
     */
    private void processExitOrder(Order order, String type) {
        exits.add(order);
        setExitSize(exitSize.plus(order.size));
        logExit("Launch " + type);

        order.observeExecutedSize().to(v -> {
            Num previous = realizedProfit;
            Num deltaSize = v.minus(exitExecutedSize);

            updateOrderRelatedStatus(exits, this::setExitPrice, this::setExitExecutedSize, Order::executedSize, this::setExitCommission);
            trader.updateSnapshot(orientation(), realizedProfit.minus(previous), deltaSize.negate(), null);

            logExit("Update " + type);
        });
    }

    /**
     * Declare exit order
     */
    public final void exitAtRiskRewardRatio(double ratio, Span span) {
        Num range = Num.of(market.tickers.on(span).spreadStats.getMean()).scale(market.service.setting.target.scale);
        exitAt(entryPrice.plus(directional, range.multiply(ratio)));
        exitAt(entryPrice.minus(directional, range));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Direction orientation() {
        return directional.orientation();
    }

    /**
     * Calculate the expected profit and loss considering the current state of the order book.
     * 
     * @return The expected profit and loss.
     */
    public final Num predictProfit() {
        Num profit = profit(market.orderBook.by(this).predictTakingPrice(remainingSize()));
        return profit.scale(market.service.setting.base.scale, profit.isPositiveOrZero() ? RoundingMode.FLOOR : RoundingMode.CEILING);
    }

    /**
     * Observe and calculate the expected profit and loss considering the current state of the order
     * book.
     * 
     * @return An event stream of the expected profit and loss.
     */
    public final Signal<Num> observePredictableProfit() {
        return market.orderBook.by(this).best.observe().map(page -> predictProfit());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void vandalize() {
        directional = null;
        market = null;
        funds = null;
        trader = null;
        entries.clear();
        exits.clear();
        disposerForEntry.dispose();
        disposerForExit.dispose();
        if (logs != null) {
            logs.clear();
            logs = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Scenario ").append(directional)
                .append(" un/realized")
                .append(unrealizedProfit(market.tickers.latest.v.price))
                .append("/")
                .append(realizedProfit)
                .append("\r\n");
        format(builder, "IN", entries, entryPrice, entrySize, entryExecutedSize, calculateCanceledSize(entries));
        format(builder, "OUT", exits, exitPrice, exitSize, exitExecutedSize, calculateCanceledSize(exits));
        if (logs != null) logs.forEach(log -> builder.append("\t").append(log).append("\r\n"));
        return builder.toString();
    }

    private Num calculateCanceledSize(Deque<Order> orders) {
        return I.signal(orders).take(Order::isCanceled).scan(() -> Num.ZERO, (v, o) -> v.plus(o.remainingSize())).to().or(Num.ZERO);
    }

    private void format(StringBuilder builder, String type, Deque<Order> orders, Num price, Num size, Num executedSize, Num canceledSize) {
        builder.append("\t")
                .append(type)
                .append("\t order ")
                .append(orders.size())
                .append("\tprice ")
                .append(price.scale(market.service.setting.base.scale))
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
        I.debug(trader.name(), message);

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
        String date = Chrono.format(Chrono.system(market.service.now()));
        log(date + "\t" + message + "\t" + entryExecutedSize + "/" + entrySize + "@" + entryPrice);
    }

    /**
     * Write exit info as log message.
     * 
     * @param message
     */
    protected final void logExit(String message) {
        String date = Chrono.format(Chrono.system(market.service.now()));
        log(date + "\t" + message + "\t" + exitExecutedSize + "/" + exitSize + "@" + exitPrice);
    }

    /**
     * Declare timing.
     * 
     * @return
     */
    protected final Signal<?> now() {
        return I.signal("");
    }

    /**
     * Cancel all entry orders.
     */
    public final void cancelEntries() {
        entries.forEach(e -> market().orders.cancelNow(e));
    }

    /**
     * Operate to stop this {@link Scenario} right now.
     */
    public final void stop() {
        stop(1);
    }

    /**
     * Operate to stop this {@link Scenario} right now.
     */
    public final void stop(double ratio) {
        cancelEntries();
        exitWhen(now(), ratio);
    }

    /**
     * Operate to stop this {@link Scenario} right now.
     */
    public final void retreat() {
        retreat(Num.ZERO);
    }

    /**
     * Operate to stop this {@link Scenario} right now.
     */
    public final void retreat(long threshold) {
        retreat(Num.of(threshold));
    }

    /**
     * Operate to stop this {@link Scenario} right now.
     */
    public final void retreat(double threshold) {
        retreat(Num.of(threshold));
    }

    /**
     * Operate to stop this {@link Scenario} right now.
     */
    public final void retreat(Num threshold) {
        exitWhen(observePredictableProfit().take(profit -> profit.isLessThanOrEqual(threshold)));
    }

    /**
     * Operate to stop this {@link Scenario} right now.
     */
    public final void retreat(Num acceptableProfit, Num acceptableLoss) {
        exitWhen(observePredictableProfit()
                .take(pnl -> pnl.isLessThanOrEqual(acceptableProfit) && pnl.isGreaterThanOrEqual(acceptableLoss)));
    }
}