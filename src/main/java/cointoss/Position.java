/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.time.ZonedDateTime;

import cointoss.util.Num;
import kiss.Variable;

/**
 * @version 2018/02/06 22:37:41
 */
public class Position implements Directional {

    /** The position side. */
    public Side side;

    /** The position price. */
    public Num price;

    /** The position size. */
    public Variable<Num> size = Variable.of(Num.ZERO);

    /** The opened date. */
    public ZonedDateTime date;

    /** The current profit and loss. */
    public Variable<Num> profit = Variable.of(Num.ZERO);

    /**
     * {@inheritDoc}
     */
    @Override
    public Side side() {
        return side;
    }
}
