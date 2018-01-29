/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.chart;

import java.time.ZonedDateTime;

/**
 * @version 2018/01/29 10:32:49
 */
public class ETick {

    /** Begin time of the tick */
    public final ZonedDateTime start;

    /** End time of the tick */
    public final ZonedDateTime end;

    /**
     * @param startTime
     * @param endTime
     */
    public ETick(ZonedDateTime startTime, ZonedDateTime endTime) {
        this.start = startTime;
        this.end = endTime;
    }
}
