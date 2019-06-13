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

import java.time.ZonedDateTime;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.trade.OrderStrategy.Makable;
import cointoss.trade.OrderStrategy.Takable;
import cointoss.util.Num;
import kiss.Variable;

/**
 * Declarative entry and exit definition.
 */
public abstract class Entry implements Directional {

    protected Trader trader;

    /** The entry direction. */
    public final Direction direction;

    /** The list entry orders. */
    final List<Order> entries = new ArrayList<>();

    /** The list exit orders. */
    final List<Order> exits = new ArrayList<>();

    protected final Stop stop = new Stop();

    protected final StopLoss stopLoss = new StopLoss();

    protected Num price;

    /** The profit or loss on this {@link Entry}. */
    protected final Variable<Num> profit = Variable.of(Num.ZERO);

    /**
     * @param direction
     */
    protected Entry(Direction direction) {
        this.trader = null;
        this.direction = direction;
    }

    /**
     * Declare entry order.
     */
    protected abstract void order();

    /**
     * Declare exit order.
     */
    protected void fixProfit() {
        fixProfitAtRiskRewardRatio();
    }

    /**
     * 
     * 
     */
    protected final void fixProfitAtRiskRewardRatio() {

    }

    /**
     * Declare exit orders. Loss cutting is the only element in the trade that investors can
     * control.
     */
    protected void stopLoss() {
        stopLossAtAcceptableRisk();
    }

    /**
     * Declare exit order.
     */
    protected final void stopLossAtAcceptableRisk() {
        stop.when(profit.observeNow().take(v -> v.isLessThan(trader.funds.riskAssets().negate()))).how(OrderStrategy.with.take());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Direction direction() {
        return direction;
    }

    protected final <S extends Takable & Makable> S order(long size) {
        return order(Num.of(size));
    }

    protected final <S extends Takable & Makable> S order(double size) {
        return order(Num.of(size));
    }

    protected final <S extends Takable & Makable> S order(Num size) {
        return (S) new OrderStrategy.with.OrderStrategies();
    }

    /**
     * <p>
     * Create rule which the specified condition is fulfilled during the specified duration.
     * </p>
     * 
     * @param time
     * @param unit
     * @param condition
     * @return
     */
    protected final Predicate<Execution> keep(int time, TemporalUnit unit, BooleanSupplier condition) {
        return keep(time, unit, e -> condition.getAsBoolean());
    }

    /**
     * <p>
     * Create rule which the specified condition is fulfilled during the specified duration.
     * </p>
     * 
     * @param time
     * @param unit
     * @param condition
     * @return
     */
    protected final Predicate<Execution> keep(int time, TemporalUnit unit, Predicate<Execution> condition) {
        AtomicBoolean testing = new AtomicBoolean();
        AtomicReference<ZonedDateTime> last = new AtomicReference(ZonedDateTime.now());

        return e -> {
            if (condition.test(e)) {
                if (testing.get()) {
                    if (e.date.isAfter(last.get())) {
                        testing.set(false);
                        return true;
                    }
                } else {
                    testing.set(true);
                    last.set(e.date.plus(time, unit).minusNanos(1));
                }
            } else {
                if (testing.get()) {
                    if (e.date.isAfter(last.get())) {
                        testing.set(false);
                    }
                }
            }
            return false;
        };
    }

    // /**
    // * {@inheritDoc}
    // */
    // @Override
    // public String toString() {
    // return new StringBuilder() //
    // .append("注文 ")
    // .append(holdTime())
    // .append("\t 損益")
    // .append(profit().asJPY(4))
    // .append("\t")
    // .append(exitSize())
    // .append("/")
    // .append(order.executedSize)
    // .append("@")
    // .append(direction().mark())
    // .append(entryPrice().asJPY(1))
    // .append(" → ")
    // .append(exitPrice().asJPY(1))
    // .toString();
    // }

}