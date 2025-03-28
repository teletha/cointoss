/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util.feather;

public final class Option {

    public static final long Latest = Long.MAX_VALUE;

    public static final long Earliest = 0;

    /** The singleton. */
    static final Option Default = new Option();

    /** Option */
    boolean infinite;

    /** Option */
    boolean forward = true;

    /** Option */
    boolean excludeStart;

    /** Option */
    int max = Integer.MAX_VALUE;

    /**
     * Hide constructor.
     */
    Option() {
    }

    /**
     * Internal usage.
     * 
     * @return Chainable option.
     */
    Option infinite() {
        infinite = true;
        return this;
    }

    /**
     * Shorthand method of {@link #reverse()} and {@link #exclude()}.
     * 
     * @return Chainable option.
     */
    public Option before() {
        return reverse().exclude();
    }

    /**
     * The items are acquired in an order that goes backward from the future to the past. f not
     * specified, the items will be retrieved in the order of going from the past to the future.
     * 
     * @return Chainable option.
     */
    public Option reverse() {
        forward = false;
        return this;
    }

    /**
     * Specifies whether to include or exclude the elements with the specified start and end times.
     * The default is to include both elements.
     * 
     * @return Chainable option.
     */
    public Option exclude() {
        excludeStart = true;
        return this;
    }

    /**
     * Specifies the maximum number of elements to retrieve.
     * 
     * @param size
     * @return
     */
    public Option max(int size) {
        if (size < 0) {
            size = -1;
        }
        max = size;
        return this;
    }
}