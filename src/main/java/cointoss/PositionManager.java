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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cointoss.util.Num;
import kiss.Signal;
import viewtify.Switch;

/**
 * @version 2018/04/25 16:59:46
 */
public class PositionManager {

    /** The actual position manager. */
    private final List<Position> positions = new CopyOnWriteArrayList();

    /** position remove event. */
    private final Switch<Position> remove = new Switch();

    /** position remove event. */
    public final Signal<Position> removed = remove.expose;

    /** position add event. */
    private final Switch<Position> addition = new Switch();

    /** position add event. */
    public final Signal<Position> added = addition.expose;

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
    void add(Side side, Execution exe) {
        if (side != null && exe != null) {
            Num remaining = exe.size;

            if (positions.isEmpty()) {
                // no position
            } else {
                // some positions
                for (Position position : positions) {
                    if (position.side == side) {
                        // check same price position
                        if (position.price.is(exe.price)) {
                            position.size.set(exe.size::plus);
                            return;
                        }
                    } else {
                        remaining = remaining.minus(position.size);

                        if (remaining.isPositive()) {
                            position.size.set(Num.ZERO);
                            positions.remove(position);

                            // remove event
                            remove.emit(position);
                        } else if (remaining.isZero()) {
                            position.size.set(Num.ZERO);
                            positions.remove(position);

                            // remove event
                            remove.emit(position);
                            return;
                        } else {
                            position.size.set(position.size.v.minus(remaining));
                            return;
                        }
                    }
                }
            }

            if (remaining.isPositive()) {
                Position add = new Position();
                add.side = side;
                add.date = exe.exec_date;
                add.price = exe.price;
                add.size.set(remaining);

                positions.add(add);
                addition.emit(add);
            }
        }
    }
}
