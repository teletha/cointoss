/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.util.PerformanceSensitive;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.MarketService;
import cointoss.execution.Execution;
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

    /**
     * Manage {@link Position}.
     */
    public PositionManager(MarketService service) {
        service.add(service.executionsRealtimelyForMe().to(e -> add(e.ⅰ, e.ⅲ)));
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
     * Calculate total profit or loss on the current price.
     * 
     * @param currentPrice A current price.
     * @return A total profit or loss of this entry.
     */
    @PerformanceSensitive
    public final Num profit(Num currentPrice) {
        Num total = Num.ZERO;
        for (Position position : positions) {
            total = total.plus(position.profit(currentPrice));
        }
        return total;
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
     * For test.
     * 
     * @param e
     */
    void add(Execution e) {
        add(e.direction, e);
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
    public void add(Direction direction, Execution e) {
        if (e != null) {
            Num size = e.size;

            for (Position position : positions) {
                if (position.direction == direction) {
                    // check same price position
                    if (position.price.is(e.price)) {
                        position.assignSize(position.size.plus(size));
                        calculate();
                        return;
                    }
                } else {
                    Num remaining = size.minus(position.size);

                    if (remaining.isPositive()) {
                        size = remaining;
                        position.assignSize(Num.ZERO);

                        positions.remove(position);
                        remove.accept(position);
                    } else if (remaining.isZero()) {
                        size = remaining;
                        position.assignSize(Num.ZERO);

                        positions.remove(position);
                        remove.accept(position);
                        calculate();
                        return;
                    } else {
                        position.assignSize(remaining.negate());
                        calculate();
                        return;
                    }
                }
            }

            if (size.isPositive()) {
                Position position = Position.with.direction(direction).price(e.price).size(size).date(e.date);

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
    }
}
