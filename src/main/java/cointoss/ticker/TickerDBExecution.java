/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import cointoss.Direction;
import hypatia.Num;
import typewriter.duck.DuckModel;

public class TickerDBExecution extends DuckModel {

    public long id;

    public Direction direction;

    public long time;

    public Num size;

    public Num price;

    public int delay;

    /**
     * Get the direction property of this {@link TickerDBExecution}.
     * 
     * @return The direction property.
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Set the direction property of this {@link TickerDBExecution}.
     * 
     * @param direction The direction value to set.
     */
    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}
