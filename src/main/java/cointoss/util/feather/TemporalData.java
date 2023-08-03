/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util.feather;

import java.time.ZonedDateTime;

import cointoss.Timelinable;
import cointoss.util.Chrono;

public interface TemporalData extends Timelinable {

    /**
     * The date and time. (UTC)
     * 
     * @return
     */
    default ZonedDateTime date() {
        return Chrono.utcBySeconds(seconds());
    }

    /**
     * The date and time represented by epoch seconds. (UTC)
     * 
     * @return
     */
    default long seconds() {
        return date().toEpochSecond();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default long mills() {
        return date().toInstant().toEpochMilli();
    }
}