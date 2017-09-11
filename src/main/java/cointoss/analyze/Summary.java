/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.analyze;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * @version 2017/09/11 19:04:05
 */
public class Summary {

    /** The entry start time. */
    public ZonedDateTime orderStart;

    /** The entry end time. */
    public ZonedDateTime orderFinish;

    /** The hold start time. */
    public ZonedDateTime holdStart;

    /** The hold end time. */
    public ZonedDateTime holdEnd;

    /**
     * <p>
     * Calculate entry time.
     * </p>
     * 
     * @return
     */
    public long getEntryOrderTime() {
        return Duration.between(orderStart, orderFinish).getSeconds();
    }
}
