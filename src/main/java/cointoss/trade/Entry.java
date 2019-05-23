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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.execution.Execution;
import cointoss.order.Order;
import cointoss.util.Num;
import cointoss.util.Span;
import kiss.Variable;
import kiss.Ⅱ;

/**
 * @version 2017/09/17 19:59:43
 */
public class Entry implements Directional {

    /**
     * 
     */
    private final Trader trader;

    /** The entry order. */
    public final Order order;

    /** The list exit orders. */
    final List<Order> exit = new ArrayList<>();

    /** The current position size. */
    private Num positionSize = Num.ZERO;

    /** The total cost of entry order. */
    private Num entryCost = Num.ZERO;

    /** The remaining size of entry order. */
    private Num exitRemaining = Num.ZERO;

    /** The total size of exit order. */
    private Num exitTotalSize = Num.ZERO;

    /** The total cost of exit order. */
    private Num exitCost = Num.ZERO;

    /**
     * Create {@link Entry} with {@link Order}.
     * 
     * @param entry A entry order.
     * @param trader TODO
     */
    Entry(Trader trader, Order entry, Consumer<Entry> initializer) {
        this.trader = trader;
        this.order = entry;

        // create new entry
        this.trader.entries.add(this);
        this.trader.actives.add(this);

        // request order
        this.trader.market.request(order).to(o -> {
            this.trader.market.orders.updated.take(u -> u.ⅰ == o).map(Ⅱ::ⅱ).to(exe -> {
                positionSize = positionSize.plus(exe.size);
                entryCost = entryCost.plus(exe.price.multiply(exe.size));

                if (o.isCompleted()) {
                    this.trader.completeEntries.accept(true);
                }
            });
            if (initializer != null) initializer.accept(this);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Direction direction() {
        return order.direction();
    }

    /**
     * Calculate remaining size of position.
     * 
     * @return
     */
    public final Num remaining() {
        return positionSize;
    }

    /**
     * Calculate profit or loss.
     * 
     * @return
     */
    public final Num profit() {
        Num up, down;

        if (direction().isBuy()) {
            up = exitCost.plus(positionSize.multiply(this.trader.market.tickers.latest.v.price));
            down = entryCost;
        } else {
            up = entryCost;
            down = exitCost.plus(positionSize.multiply(this.trader.market.tickers.latest.v.price));
        }
        return up.minus(down);
    }

    /**
     * Calculate average of entry price.
     * 
     * @return
     */
    public final Num entryPrice() {
        return order.executedSize.isZero() ? Num.ZERO : entryCost.divide(order.executedSize);
    }

    /**
     * Calculate total executed exit size.
     * 
     * @return
     */
    public final Num exitSize() {
        return exitTotalSize;
    }

    /**
     * Calculate average of exit price.
     * 
     * @return
     */
    public final Num exitPrice() {
        return exitTotalSize.isZero() ? Num.ZERO : exitCost.divide(exitTotalSize);
    }

    /**
     * Calculate ordering time.
     * 
     * @return
     */
    public final Span orderTime() {
        Variable<Execution> last = order.last();
        ZonedDateTime start = order.creationTime;
        ZonedDateTime finish = last.map(v -> v.date).or(this.trader.market.tickers.latest.v.date);

        if (start.isBefore(finish)) {
            finish = this.trader.market.tickers.latest.v.date;
        }
        return new Span(start, finish);
    }

    /**
     * Calculate holding time.
     * 
     * @return
     */
    public final Span holdTime() {
        Variable<Execution> first = order.first();

        if (first.isAbsent()) {
            return Span.ZERO;
        }

        ZonedDateTime start = first.v.date;
        ZonedDateTime finish = start;

        if (isActive()) {
            finish = this.trader.market.tickers.latest.v.date;
        } else {
            for (Order order : exit) {
                Variable<Execution> last = order.last();

                if (last.isPresent()) {
                    finish = last.v.date;
                }
            }
        }

        // if (start.isBefore(finish)) {
        // finish = market.getExecutionLatest().exec_date;
        // }

        if (finish.isBefore(start)) {
            // If this exception will be thrown, it is bug of this program. So we must rethrow
            // the wrapped error in here.
            order.all().to(e -> {
                System.out.println("Start Exe " + e);
            });

            for (Order o : exit) {
                o.all().to(e -> {
                    System.out.println("Exit Exe " + e);
                });
            }

            throw new Error(finish + "   " + start);
        }

        return new Span(start, finish);
    }

    /**
     * Cehck whether this position has profit
     */
    public final boolean isWin() {
        return profit().isPositive();
    }

    /**
     * Cehck whether this position has loss
     */
    public final boolean isLose() {
        return profit().isNegative();
    }

    /**
     * Cehck whether this position is not activated.
     */
    public final boolean isInitial() {
        return order.size.is(order.remainingSize);
    }

    /**
     * Cehck whether this position was activated but not completed.
     */
    public final boolean isActive() {
        return positionSize.isZero() == false;
    }

    /**
     * Cehck whether this position was completed.
     */
    public final boolean isCompleted() {
        return positionSize.isZero() && order.remainingSize.isZero();
    }

    /**
     * Cehck whether this position was not activated, then it was canceled.
     */
    public final boolean isCanceled() {
        return isInitial() && order.isCanceled();
    }

    /**
     * Request exit order.
     * 
     * @param size A exit size.
     * @param price A exit price.
     */
    public final void exitLimit(Num size, Num price, Consumer<Order> process) {
        // check size
        if (size == null || size.isLessThanOrEqual(Num.ZERO)) {
            return;
        }

        // check price
        if (price == null || price.isLessThanOrEqual(Num.ZERO)) {
            return;
        }
        exit(Order.with.direction(order.inverse(), size).price(price), process);
    }

    /**
     * Request exit order.
     * 
     * @param size A exit size.
     */
    public final void exitMarket() {
        exitMarket((Consumer<Order>) null);
    }

    /**
     * Request exit order.
     * 
     * @param size A exit size.
     */
    public final void exitMarket(Consumer<Order> process) {
        // check size
        exitMarket(remaining(), process);

        if (!remaining().isZero()) {
            this.trader.market.cancel(order).to();
        }
    }

    /**
     * Request exit order.
     * 
     * @param size A exit size.
     */
    public final void exitMarket(Num size) {
        exitMarket(size, null);
    }

    /**
     * Request exit order.
     * 
     * @param size A exit size.
     */
    public final void exitMarket(Num size, Consumer<Order> process) {
        // check size
        if (size == null || size.isLessThanOrEqual(Num.ZERO)) {
            return;
        }
        exit(Order.with.direction(order.inverse(), size), process);
    }

    /**
     * Request exit order.
     * 
     * @param order A exit order.
     */
    private void exit(Order order, Consumer<Order> initializer) {
        exitRemaining = exitRemaining.plus(order.size);

        this.trader.market.request(order).to(o -> {
            exit.add(o);

            this.trader.market.orders.updated.take(u -> u.ⅰ == o).map(Ⅱ::ⅱ).to(exe -> {
                positionSize = positionSize.minus(exe.size);
                exitTotalSize = exitTotalSize.plus(exe.size);
                exitRemaining = exitRemaining.minus(exe.size);
                exitCost = exitCost.plus(exe.price.multiply(exe.size));

                if (o.isCompleted()) {
                    this.trader.completeExits.accept(true);

                    if (positionSize.isZero()) {
                        this.trader.actives.remove(this);
                        this.trader.closePositions.accept(true);
                    }
                }
            });
            if (initializer != null) initializer.accept(o);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new StringBuilder() //
                .append("注文 ")
                .append(holdTime())
                .append("\t 損益")
                .append(profit().asJPY(4))
                .append("\t")
                .append(exitSize())
                .append("/")
                .append(order.executedSize)
                .append("@")
                .append(direction().mark())
                .append(entryPrice().asJPY(1))
                .append(" → ")
                .append(exitPrice().asJPY(1))
                .toString();
    }
}