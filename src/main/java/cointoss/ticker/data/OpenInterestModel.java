/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker.data;

import java.time.ZonedDateTime;

import cointoss.util.Chrono;
import cointoss.util.feather.Timelinable;
import icy.manipulator.Icy;
import typewriter.api.model.IdentifiableModel;

@Icy
abstract class OpenInterestModel extends IdentifiableModel implements Timelinable {

    /**
     * {@inheritDoc}
     */
    @Override
    public long getId() {
        return seconds();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setId(long id) {
        date(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Icy.Property
    public abstract ZonedDateTime date();

    @Icy.Overload("date")
    private ZonedDateTime date(long time) {
        return Chrono.utcBySeconds(time);
    }

    @Icy.Property
    public abstract float size();
}