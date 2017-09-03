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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * @version 2017/07/21 23:04:19
 */
public class Price {

    /** The base context. */
    private static final RoundingMode mode = RoundingMode.HALF_UP;

    /** The price position. */
    public final Side position;

    /** The actual value. */
    public final BigDecimal value;

    /**
     * @param price
     */
    private Price(float price, Side position) {
        this.value = new BigDecimal(price);
        this.position = Objects.requireNonNull(position);
    }

    /**
     * @param price
     */
    private Price(BigDecimal price, Side position) {
        this.value = price;
        this.position = position;
    }

    /**
     * @param ratio
     * @return
     */
    public Price increaseBy(String ratio) {
        if (position == Side.BUY) {
            return new Price(value.multiply(BigDecimal.ONE.add(new BigDecimal(ratio))).setScale(0, mode), position);
        } else {
            return new Price(value.multiply(BigDecimal.ONE.subtract(new BigDecimal(ratio))).setScale(0, mode), position);
        }
    }

    /**
     * @param ratio
     * @return
     */
    public Price decreaseBy(String ratio) {
        if (position == Side.BUY) {
            return new Price(value.multiply(BigDecimal.ONE.subtract(new BigDecimal(ratio))).setScale(0, mode), position);
        } else {
            return new Price(value.multiply(BigDecimal.ONE.add(new BigDecimal(ratio))).setScale(0, mode), position);
        }
    }

    /**
     * @param value
     * @return
     */
    public Price decrease(int value) {
        if (position == Side.BUY) {
            return new Price(this.value.subtract(new BigDecimal(value)).setScale(0, mode), position);
        } else {
            return new Price(this.value.add(new BigDecimal(value)).setScale(0, mode), position);
        }
    }

    /**
     * @param base
     * @return
     */
    public Price subtract(Price price) {
        return new Price(value.subtract(price.value).setScale(0, mode), position);
    }

    /**
     * @param size
     * @return
     */
    public Price multiply(float size) {
        return new Price(value.multiply(BigDecimal.valueOf(size)).setScale(2, mode), position);
    }

    /**
     * @param price
     * @return
     */
    public boolean isLargerThan(Price price) {
        if (position == Side.BUY) {
            return value.compareTo(price.value) > 0;
        } else {
            return value.compareTo(price.value) < 0;
        }
    }

    /**
     * @param price
     * @return
     */
    public boolean isEqualOrLargerThan(Price price) {
        if (position == Side.BUY) {
            return value.compareTo(price.value) >= 0;
        } else {
            return value.compareTo(price.value) <= 0;
        }
    }

    /**
     * @param price
     * @return
     */
    public boolean isLessThan(Price price) {
        if (position == Side.BUY) {
            return value.compareTo(price.value) < 0;
        } else {
            return value.compareTo(price.value) > 0;
        }
    }

    /**
     * @param price
     * @return
     */
    public boolean isEqualOrLessThan(Price price) {
        if (position == Side.BUY) {
            return value.compareTo(price.value) <= 0;
        } else {
            return value.compareTo(price.value) >= 0;
        }
    }

    /**
     * @return
     */
    public Price inverse() {
        return new Price(value, position.inverse());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return value + "(" + position + ")";
    }

    /**
     * @param price
     * @param position
     * @return
     */
    public static Price of(float price, Side position) {
        return new Price(price, position);
    }
}