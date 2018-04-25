/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cointoss.util.Num;
import kiss.Signal;
import kiss.Variable;
import viewtify.Switch;

/**
 * @version 2018/04/25 16:59:46
 */
public final class PositionManager {

    /** The actual position manager. */
    private final List<Position> positions = new CopyOnWriteArrayList();

    /** The unmodifiable open positions. */
    public final List<Position> items = Collections.unmodifiableList(positions);

    /** position remove event. */
    private final Switch<Position> remove = new Switch();

    /** position remove event. */
    public final Signal<Position> removed = remove.expose;

    /** position add event. */
    private final Switch<Position> addition = new Switch();

    /** position add event. */
    public final Signal<Position> added = addition.expose;

    /** The total size. */
    public final Variable<Num> size = Variable.of(Num.ZERO);

    /** The average price. */
    public final Variable<Num> price = Variable.of(Num.ZERO);

    /**
     * Check the position state.
     * 
     * @return
     */
    public boolean hasPosition() {
        return positions.isEmpty() == false;
    }

    /**
     * Check the position state.
     * 
     * @return
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
     * Handle the specified execution on the side.
     * 
     * @param side A position side.
     * @param exe A target execution.
     */
    public void add(Position add) {
        if (add != null) {
            for (Position position : positions) {
                if (position.side == add.side) {
                    // check same price position
                    if (position.price.is(add.price)) {
                        position.size.set(add.size.v::plus);
                        recalculate();
                        return;
                    }
                } else {
                    Num remaining = add.size.v.minus(position.size);

                    if (remaining.isPositive()) {
                        add.size.set(remaining);
                        position.size.set(Num.ZERO);

                        positions.remove(position);
                        remove.emit(position);
                    } else if (remaining.isZero()) {
                        add.size.set(remaining);
                        position.size.set(Num.ZERO);

                        positions.remove(position);
                        remove.emit(position);
                        recalculate();
                        return;
                    } else {
                        position.size.set(v -> v.minus(add.size));
                        recalculate();
                        return;
                    }
                }
            }

            if (add.size.v.isPositive()) {
                positions.add(add);
                addition.emit(add);
                recalculate();
            }
        }
    }

    /**
     * Calculate some variables.
     */
    private void recalculate() {
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
