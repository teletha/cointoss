/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker.data;

import java.time.ZonedDateTime;

public interface TimeseriesData {

    /**
     * The date and time.
     * 
     * @return
     */
    ZonedDateTime date();

    /**
     * The date and time.
     * 
     * @return
     */
    default long epochSeconds() {
        return date().toEpochSecond();
    }
}
