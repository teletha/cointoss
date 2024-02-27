/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker.data;

import java.nio.ByteBuffer;
import java.time.ZonedDateTime;

import cointoss.util.Chrono;
import cointoss.util.feather.DataCodec;
import cointoss.util.feather.Timelinable;
import icy.manipulator.Icy;

@Icy
abstract class OpenInterestModel implements Timelinable {

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

    /**
     * Codec for {@link OpenInterest}.
     */
    @SuppressWarnings("unused")
    private static class Codec extends DataCodec<OpenInterest> {

        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            return 4;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public OpenInterest read(long time, ByteBuffer reader) {
            return OpenInterest.with.date(time).size(reader.getFloat());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(OpenInterest item, ByteBuffer writer) {
            writer.putFloat(item.size);
        }
    }
}