/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util.feather;

import java.time.ZonedDateTime;

public interface TemporalData {

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