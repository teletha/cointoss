/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.time;

import java.time.ZonedDateTime;

import cointoss.Market;

/**
 * Virtual clock to retrieve the current date and time of the associated {@link Market}.
 */
public interface Clock {

    /**
     * Get the current date and time.
     * 
     * @return
     */
    ZonedDateTime now();
}
