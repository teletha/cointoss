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

import org.junit.jupiter.api.Test;

import cointoss.util.Chrono;

class TimestampBasedMarketServiceSupporterTest {

    @Test
    void computeEpochTime() {
        TimestampBasedMarketServiceSupporter numbering = new TimestampBasedMarketServiceSupporter(true, 10);

        assert numbering.computeEpochTime(100) == 10;
        assert numbering.computeEpochTime(110) == 11;
    }

    @Test
    void computeEpochTimeSecondBase() {
        TimestampBasedMarketServiceSupporter numbering = new TimestampBasedMarketServiceSupporter(false, 10);

        assert numbering.computeEpochTime(100) == 10;
        assert numbering.computeEpochTime(110) == 11;
    }

    @Test
    void computeDateTime() {
        TimestampBasedMarketServiceSupporter numbering = new TimestampBasedMarketServiceSupporter(true, 10);

        assert numbering.computeDateTime(100).equals(Chrono.utc(1970, 1, 1, 0, 0, 0, 10));
        assert numbering.computeDateTime(12340).equals(Chrono.utc(1970, 1, 1, 0, 0, 1, 234));
    }

    @Test
    void computeDateTimeSecondBase() {
        TimestampBasedMarketServiceSupporter numbering = new TimestampBasedMarketServiceSupporter(false, 10);

        assert numbering.computeDateTime(100).equals(Chrono.utc(1970, 1, 1, 0, 0, 10, 0));
        assert numbering.computeDateTime(12340).equals(Chrono.utc(1970, 1, 1, 0, 20, 34, 0));
    }

    @Test
    void computeID() {
        TimestampBasedMarketServiceSupporter numbering = new TimestampBasedMarketServiceSupporter(true, 10);

        assert numbering.computeID(Chrono.utc(1970, 1, 1, 0, 0, 12, 345)) == 123450;
        assert numbering.computeID(Chrono.utc(1970, 1, 1, 0, 1, 23, 456)) == 834560;
    }

    @Test
    void computeIDSecondBase() {
        TimestampBasedMarketServiceSupporter numbering = new TimestampBasedMarketServiceSupporter(false, 10);

        assert numbering.computeID(Chrono.utc(1970, 1, 1, 0, 0, 12, 345)) == 120;
        assert numbering.computeID(Chrono.utc(1970, 1, 1, 0, 1, 23, 456)) == 830;
    }
}
