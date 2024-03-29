/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import java.util.Objects;

import org.apache.commons.lang3.RandomUtils;

import kiss.Decoder;
import kiss.Encoder;
import kiss.Managed;
import kiss.Singleton;

/**
 * @version 2017/07/21 19:47:35
 */
public enum Direction implements Directional {
    BUY, SELL;

    /**
     * Return ID.
     * 
     * @return
     */
    public String mark() {
        return this == BUY ? "B" : "S";
    }

    /**
     * Utility to inverse {@link Direction}.
     * 
     * @return
     */
    public Direction inverse() {
        return this == Direction.BUY ? Direction.SELL : Direction.BUY;
    }

    /**
     * @param position
     * @return
     */
    public boolean isPair(Direction position) {
        return this != Objects.requireNonNull(position);
    }

    /**
     * @param position
     * @return
     */
    public boolean isPair(String position) {
        return isPair(parse(position));
    }

    /**
     * @param position
     * @return
     */
    public boolean isSame(Direction position) {
        return this == Objects.requireNonNull(position);
    }

    /**
     * @param position
     * @return
     */
    public boolean isSame(String position) {
        return isSame(parse(position));
    }

    /**
     * Generate random {@link Direction}.
     * 
     * @return A random value.
     */
    public static Direction random() {
        return RandomUtils.nextBoolean() ? BUY : SELL;
    }

    /**
     * <p>
     * Parse by value.
     * </p>
     * 
     * @param value
     * @return
     */
    public static Direction parse(String value) {
        if (value == null || value.length() == 0) {
            return SELL;
        }
        char c = value.charAt(0);
        return c == 'S' || c == 's' ? SELL : BUY;
    }

    /**
     * Parse by value.
     * 
     * @param c A directional text.
     * @return
     */
    public static Direction parse(char c) {
        return c == 'S' ? SELL : BUY;
    }

    /**
     * 
     */
    @Managed(value = Singleton.class)
    private static class Codec implements Encoder<Direction>, Decoder<Direction> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Direction decode(String value) {
            return parse(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String encode(Direction value) {
            return value.mark();
        }
    }
}