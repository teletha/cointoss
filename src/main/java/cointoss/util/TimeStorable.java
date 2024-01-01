/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import java.time.ZonedDateTime;

public interface TimeStorable {

    /**
     * Snapshot the epoch second.
     * 
     * @return
     */
    long time();

    /**
     * Snapshot the date-time. (UTC)
     * 
     * @return
     */
    default ZonedDateTime datetime() {
        return Chrono.utcBySeconds(time());
    }
}