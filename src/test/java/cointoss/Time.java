/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.time.LocalDateTime;

import cointoss.Generator;

/**
 * @version 2017/08/16 22:07:12
 */
public class Time {

    /** The base time */
    static final LocalDateTime BASE = LocalDateTime.of(2017, 1, 1, 0, 0, 0);

    /**
     * @version 2017/08/16 22:36:24
     */
    private static class TimeBase {

        private final int time;

        /**
         * @param time
         */
        protected TimeBase(int time) {
            this.time = time;
        }

        /**
         * @return
         */
        public LocalDateTime to() {
            return BASE.plusSeconds(time);
        }
    }

    /**
     * Specify time
     * 
     * @param seconds
     * @return
     */
    public static Rule after(int seconds) {
        return Rule.when(at(seconds).to());
    }

    /**
     * Specify time.
     * 
     * @param seconds
     * @return
     */
    public static At at(int seconds) {
        return new At(seconds);
    }

    /**
     * @version 2017/08/16 22:32:11
     */
    public static class At extends TimeBase {

        /**
         * @param time
         */
        private At(int time) {
            super(time);
        }
    }

    /**
     * Specify lag time.
     * 
     * @param seconds
     * @return
     */
    public static Lag lag(int seconds) {
        return new Lag(seconds, seconds);
    }

    /**
     * Specify lag time.
     * 
     * @param start
     * @param end
     * @return
     */
    public static Lag lag(int start, int end) {
        return new Lag(start, end);
    }

    /**
     * @version 2017/08/16 22:32:11
     */
    public static class Lag extends TimeBase {

        /** The start time */
        private final int start;

        /** The end time */
        private final int end;

        /**
         * @param start
         * @param end
         */
        private Lag(int start, int end) {
            super(start);

            this.start = start;
            this.end = end;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public LocalDateTime to() {
            return BASE.plusSeconds(Generator.randomInt(start, end));
        }

        public long generate() {
            return Generator.randomLong(start * 1000000000L, end * 1000000000L);
        }
    }
}
