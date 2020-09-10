/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.arithmetic;

import java.math.BigDecimal;

import com.google.common.math.DoubleMath;

import kiss.Variable;

public class ArithmeticTestSupport {

    /**
     * Convert to {@link BigDecimal}.
     * 
     * @param value A target value to convert.
     * @return A converted value.
     */
    protected static BigDecimal big(int value) {
        return new BigDecimal(value, Num.CONTEXT);
    }

    /**
     * Convert to {@link BigDecimal}.
     * 
     * @param value A target value to convert.
     * @return A converted value.
     */
    protected static BigDecimal big(long value) {
        return new BigDecimal(value, Num.CONTEXT);
    }

    /**
     * Convert to {@link BigDecimal}.
     * 
     * @param value A target value to convert.
     * @return A converted value.
     */
    protected static BigDecimal big(double value) {
        return BigDecimal.valueOf(value);
    }

    /**
     * Convert to {@link BigDecimal}.
     * 
     * @param value A target value to convert.
     * @return A converted value.
     */
    protected static BigDecimal big(String value) {
        return new BigDecimal(value, Num.CONTEXT);
    }

    /**
     * Convert to {@link BigDecimal}.
     * 
     * @param value A target value to convert.
     * @return A converted value.
     */
    protected static BigDecimal big(Num value) {
        return big(value.toString());
    }

    /**
     * Convert to {@link BigDecimal}.
     * 
     * @param value A target value to convert.
     * @return A converted value.
     */
    protected static BigDecimal big(Variable<Num> value) {
        return big(value.v);
    }

    /**
     * Check equality of the specified values.
     * 
     * @param one A targe value.
     * @param other A target value.
     * @return A result.
     */
    protected static boolean equality(Num one, BigDecimal other) {
        return one.toString().equals(other.toPlainString());
    }

    /**
     * Check equality of the specified values vaguely.
     * 
     * @param one A targe value.
     * @param other A target value.
     * @return A result.
     */
    protected static boolean equalityVaguely(Num one, BigDecimal other) {
        return DoubleMath.fuzzyEquals(one.doubleValue(), other.doubleValue(), Num.Fuzzy);
    }

    /**
     * Check equality between the specified value and zero.
     * 
     * @param value Target value.
     * @return A result.
     */
    protected static boolean zeroIsEqualTo(int value) {
        return value == 0;
    }

    /**
     * Check equality between the specified value and zero.
     * 
     * @param value Target value.
     * @return A result.
     */
    protected static boolean zeroIsEqualTo(long value) {
        return value == 0;
    }

    /**
     * Check equality between the specified value and zero.
     * 
     * @param value Target value.
     * @return A result.
     */
    protected static boolean zeroIsEqualTo(double value) {
        return DoubleMath.fuzzyEquals(value, 0, Num.Fuzzy);
    }

    /**
     * Check equality between the specified value and zero.
     * 
     * @param value Target value.
     * @return A result.
     */
    protected static boolean zeroIsEqualTo(String value) {
        return value.equals("0");
    }

    /**
     * Check equality between the specified value and zero.
     * 
     * @param value Target value.
     * @return A result.
     */
    protected static boolean zeroIsEqualTo(Num value) {
        return value.is(0);
    }

    /**
     * Check equality between the specified value and zero.
     * 
     * @param value Target value.
     * @return A result.
     */
    protected static boolean zeroIsEqualTo(Variable<Num> value) {
        return zeroIsEqualTo(value.v);
    }
}
