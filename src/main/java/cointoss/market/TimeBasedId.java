/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market;

import java.time.ZonedDateTime;

import cointoss.util.Chrono;

public class TimeBasedId {

    public final long padding;

    /**
     * @param padding
     */
    public TimeBasedId(long padding) {
        this.padding = padding;
    }

    public long mills(long id) {
        return id / padding;
    }

    public long secs(long id) {
        return id / (padding * 1000);
    }

    public ZonedDateTime encode(long id) {
        return Chrono.utcByMills(id / padding);
    }

    public long decode(ZonedDateTime time) {
        return time.toInstant().toEpochMilli() * padding;
    }
}
