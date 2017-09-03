/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market;

import java.time.LocalDate;

/**
 * @version 2017/09/03 22:04:28
 */
public class Span {

    /** The start date. */
    public final LocalDate start;

    /** The end date. */
    public final LocalDate end;

    /**
     * @param start
     * @param end
     */
    public Span(int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay) {
        this.start = LocalDate.of(startYear, startMonth, startDay);
        this.end = LocalDate.of(endYear, endMonth, endDay);
    }
}
