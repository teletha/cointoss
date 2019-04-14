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

import java.time.ZonedDateTime;

import cointoss.Directional;
import cointoss.Direction;
import cointoss.util.Num;
import kiss.Variable;

/**
 * @version 2018/04/29 13:37:42
 */
public class Position implements Directional {

    /** The position side. */
    public Direction side;

    /** The position price. */
    public Num price;

    /** The position size. */
    public Variable<Num> size = Variable.of(Num.ZERO);

    /** The opened date. */
    public ZonedDateTime date;

    /** The current profit and loss. */
    public Variable<Num> profit = Variable.of(Num.ZERO);

    /** Optional : The associated execution id. */
    public String id;

    /**
     * {@inheritDoc}
     */
    @Override
    public Direction side() {
        return side;
    }
}
