/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.arithmeric;

import java.math.BigDecimal;

public interface ArithmeticFactory<A extends Arithmetic> {

    /**
     * Build by the specified value.
     * 
     * @param value
     * @return
     */
    A create(int value);

    /**
     * Build by the specified value.
     * 
     * @param value
     * @return
     */
    A create(long value);

    /**
     * Build by the specified value.
     * 
     * @param value
     * @return
     */
    A create(double value);

    /**
     * Build by the specified value.
     * 
     * @param value
     * @return
     */
    A create(String value);

    /**
     * Build by the specified value.
     * 
     * @param value
     * @return
     */
    A create(BigDecimal value);

    /**
     * Build by the specified value.
     * 
     * @param value
     * @return
     */
    A zero();
}
