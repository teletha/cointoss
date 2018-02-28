/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import org.apache.commons.lang3.RandomUtils;

/**
 * @version 2017/05/03 22:41:58
 */
public class Generator {
    /**
     * <p>
     * Generate random int value.
     * </p>
     * 
     * @param startInclusive A start value.
     * @param endInclusive A end value.
     * @return
     */
    public static <E extends Enum> E random(Class<E> type) {
        return type.getEnumConstants()[randomInt(0, type.getEnumConstants().length - 1)];
    }

    /**
     * <p>
     * Generate random boolean value.
     * </p>
     * 
     * @return
     */
    public static boolean randomBoolean() {
        return RandomUtils.nextInt(0, 1) == 0;
    }

    /**
     * <p>
     * Generate random int value.
     * </p>
     * 
     * @param startInclusive A start value.
     * @param endInclusive A end value.
     * @return
     */
    public static int randomInt(int startInclusive, int endInclusive) {
        if (endInclusive != Integer.MAX_VALUE) {
            endInclusive++;
        }
        return RandomUtils.nextInt(startInclusive, endInclusive);
    }

    /**
     * <p>
     * Generate random int value.
     * </p>
     * 
     * @param startInclusive A start value.
     * @param endInclusive A end value.
     * @return
     */
    public static long randomLong(long startInclusive, long endInclusive) {
        if (endInclusive != Long.MAX_VALUE) {
            endInclusive++;
        }
        return RandomUtils.nextLong(startInclusive, endInclusive);
    }
}
