/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.execution;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import cointoss.Direction;
import cointoss.util.Chrono;
import cointoss.util.Num;
import kiss.Decoder;
import kiss.Encoder;
import kiss.Manageable;
import kiss.Singleton;

public class Executions {

    /** The empty object. */
    public static final Execution BASE = Execution.with().date(Chrono.utc(2000, 1, 1)).ice();

    /**
     * 
     */
    @Manageable(lifestyle = Singleton.class)
    private static class Codec implements Decoder<ZonedDateTime>, Encoder<ZonedDateTime> {

        /**
         * {@inheritDoc}
         */
        @Override
        public String encode(ZonedDateTime value) {
            return value.toLocalDate().toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ZonedDateTime decode(String value) {
            return LocalDateTime.parse(value).atZone(Chrono.UTC);
        }
    }

    static Execution of(String[] values) {
        Num size = Num.of(values[4]);
    
        return Execution.with()
                .id(Long.parseLong(values[0]))
                .date(LocalDateTime.parse(values[1]).atZone(Chrono.UTC))
                .side(Direction.parse(values[2]))
                .price(Num.of(values[3]))
                .size(size)
                .cumulativeSize(size)
                .consecutive(Integer.parseInt(values[5]))
                .delay(Integer.parseInt(values[6]));
    }
}
