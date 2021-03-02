/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.feather;

public final class Option {

    public static final long Latest = Long.MAX_VALUE;

    public static final long Earliest = 0;

    /** The singleton. */
    static final Option Default = new Option();

    /** Option */
    boolean includeStart = true;

    /** Option */
    boolean includeEnd = true;

    /** Option */
    int max = -1;

    /**
     * Hide constructor.
     */
    Option() {
    }

    public Option from(long start) {
        return this;
    }

    public Option fromLatest() {
        return this;
    }

    /**
     * Specifies whether to include or exclude the elements with the specified start and end times.
     * The default is to include both elements.
     * 
     * @return Chainable option.
     */
    public Option exclude() {
        return include(false, false);
    }

    /**
     * Specifies whether to include or exclude the elements with the specified start and end times.
     * The default is to include both elements.
     * 
     * @param start
     * @param end
     * @return Chainable option.
     */
    public Option include(boolean start, boolean end) {
        includeStart = start;
        includeEnd = end;
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
