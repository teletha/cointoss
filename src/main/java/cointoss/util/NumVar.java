/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import kiss.Variable;

public class NumVar extends Variable<Num> {

    /**
     * @param value
     */
    private NumVar(Num value) {
        super(value);
    }

    /**
     * Test equality.
     * 
     * @param value
     * @return
     */
    public boolean is(long value) {
        return v.is(value);
    }

    /**
     * Test equality.
     * 
     * @param value
     * @return
     */
    public boolean is(double value) {
        return v.is(value);
    }

    /**
     * Create new number variable.
     * 
     * @param value A value.
     * @return A created variable.
     */
    public static NumVar of(long value) {
        return of(Num.of(value));
    }

    /**
     * Create new number variable.
     * 
     * @param value A value.
     * @return A created variable.
     */
    public static NumVar of(double value) {
        return of(Num.of(value));
    }

    /**
     * Create new number variable.
     * 
     * @param value A value.
     * @return A created variable.
     */
    public static NumVar of(Num value) {
        return new NumVar(value);
    }

    public static NumVar zero() {
        return of(Num.ZERO);
    }
}
