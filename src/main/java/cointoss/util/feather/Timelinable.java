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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import typewriter.api.Identifiable;

public interface Timelinable extends Identifiable {

    /**
     * The date and time. (UTC)
     * 
     * @return
     */
    default ZonedDateTime date() {
        return Instant.ofEpochMilli(mills()).atZone(ZoneId.of("Z"));
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
     * Occuered date-time. (Epoch mills)
     * 
     * @return
     */
    default long mills() {
        return date().toInstant().toEpochMilli();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default long getId() {
        return seconds();
    }
}