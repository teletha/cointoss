/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.position;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cointoss.Directional;
import cointoss.Execution;
import cointoss.MarketService;
import cointoss.Direction;
import cointoss.util.Num;
import kiss.Signal;
import kiss.Signaling;
import kiss.Variable;

/**
 * @version 2018/04/26 18:11:34
 */
public final class PositionManager implements Directional {

    /** The actual position manager. */
    private final List<Position> positions = new CopyOnWriteArrayList();

    /** The unmodifiable open positions. */
    public final List<Position> items = Collections.unmodifiableList(positions);

    /** position remove event. */
    private final Signaling<Position> remove = new Signaling();

    /** position remove event. */
    public final Signal<Position> removed = remove.expose;

    /** position add event. */
    private final Signaling<Position> addition = new Signaling();

    /** position add event. */
    public final Signal<Position> added = addition.expose;

    /** The total size. */
    public final Variable<Num> size = Variable.of(Num.ZERO);

    /** The average price. */
    public final Variable<Num> price = Variable.of(Num.ZERO);

    /** The total profit and loss. */
    public final Variable<Num> profit = Variable.of(Num.ZERO);

    /** The latest market price. */
    private final Variable<Execution> latest;

    /**
     * Manage {@link Position}.
     * 
     * @param latest A latest market {@link Execution} holder.
     */
    public PositionManager(MarketService service, Variable<Execution> latest) {
        this.latest = latest == null ? Variable.of(Execution.BASE) : latest;
        this.latest.observe().to(this::calculateProfit);

        service.add(service.executionsRealtimelyForMe().to(this::add));
    }

    /**
     * Check the position state.
     * 
     * @return A result.
     */
    public boolean hasPosition() {
        return positions.isEmpty() == false;
    }

    /**
     * Check the position state.
     * 
     * @return A result.
     */
    public boolean hasNoPosition() {
        return positions.isEmpty() == true;
    }

    /**
     * Cehck the position state.
     * 
     * @return
     */
    public boolean isLong() {
        return hasPosition() && positions.get(0).isBuy();
    }

    /**
     * Cehck the position state.
     * 
     * @return
     */
    public boolean isShort() {
        return hasPosition() && positions.get(0).isSell();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Direction direction() {
        return isLong() ? Direction.BUY : Direction.SELL;
    }

    /**
     * Add {@link Position} manually.
     * 
     * @param position
     */
    public void add(Position position) {
        if (position != null) {
            positions.add(position);
            addition.accept(position);
            calculate();
        }
    }

    /**
     * <p>
     * Update position by the specified my execution.
     * </p>
     * <p>
     * This method is separate for test.
     * </p>
     * 
     * @param exe A my execution.
     */
    void add(Execution e) {
        if (e != null) {
            Num size = e.size;

            for (Position position : positions) {
                if (position.side == e.side) {
                    // check same price position
                    if (position.price.is(e.price)) {
                        position.size.set(size::plus);
                        calculate();
                        return;
                    }
                } else {
                    Num remaining = size.minus(position.size);

                    if (remaining.isPositive()) {
                        size = remaining;
                        position.size.set(Num.ZERO);

                        positions.remove(position);
                        remove.accept(position);
                    } else if (remaining.isZero()) {
                        size = remaining;
                        position.size.set(Num.ZERO);

                        positions.remove(position);
                        remove.accept(position);
                        calculate();
                        return;
                    } else {
                        position.size.set(remaining.negate());
                        calculate();
                        return;
                    }
                }
            }

            if (size.isPositive()) {
                Position position = new Position();
                position.side = e.side;
                position.price = e.price;
                position.size.set(size);
                position.date = e.date;

                positions.add(position);
                addition.accept(position);
                calculate();
            }
        }
    }

    /**
     * Calculate some variables.
     */
    private void calculate() {
        Num size = Num.ZERO;
        Num price = Num.ZERO;

        for (Position position : positions) {
            size = size.plus(position.size);
            price = price.plus(position.price.multiply(position.size));
        }

        this.size.set(size);
        this.price.set(size.isZero() ? Num.ZERO : price.divide(size));
        calculateProfit(latest.v);
    }

    /**
     * Calculate profit variable.
     */
    private void calculateProfit(Execution execution) {
        Num total = Num.ZERO;

        for (Position position : positions) {
            Num profit = position.isBuy() ? execution.price.minus(position.price) : position.price.minus(execution.price);
            profit = profit.multiply(position.size).scale(0);

            position.profit.set(profit);
            total = total.plus(profit);
        }
        this.profit.set(total);
    }
}
