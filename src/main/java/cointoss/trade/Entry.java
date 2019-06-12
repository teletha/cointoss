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

import java.util.ArrayList;
import java.util.List;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.order.Order;
import cointoss.trade.OrderStrategy.Makable;
import cointoss.trade.OrderStrategy.Takable;
import cointoss.util.Num;

/**
 * Declarative entry and exit definition.
 */
public abstract class Entry implements Directional {

    /**
     * 
     */
    private final Trader trader;

    /** The entry direction. */
    public final Direction direction;

    /** The list entry orders. */
    final List<Order> entries = new ArrayList<>();

    /** The list exit orders. */
    final List<Order> exits = new ArrayList<>();

    protected final Stop stop = new Stop();

    protected final StopLoss stopLoss = new StopLoss();

    protected Num price;

    protected Entry(Direction direction) {
        this.trader = null;
        this.direction = direction;
    }

    /**
     * Declare entry orders.
     */
    protected abstract void order();

    /**
     * Declare exit orders.
     */
    protected abstract void stop();

    /**
     * {@inheritDoc}
     */
    @Override
    public Direction direction() {
        return direction;
    }

    protected <S extends Takable & Makable> S order(long size) {
        return order(Num.of(size));
    }

    protected <S extends Takable & Makable> S order(double size) {
        return order(Num.of(size));
    }

    protected <S extends Takable & Makable> S order(Num size) {
        return (S) new OrderStrategy.with.OrderStrategies();
    }

    public void cancel() {

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