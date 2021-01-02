/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market;

import org.junit.jupiter.api.Test;

import cointoss.util.Chrono;

class TimestampBasedMarketServiceSupporterTest {

    @Test
    void computeEpochTime() {
        TimestampBasedMarketServiceSupporter numbering = new TimestampBasedMarketServiceSupporter(10, true);

        assert numbering.computeEpochTime(100) == 10;
        assert numbering.computeEpochTime(110) == 11;
    }

    @Test
    void computeEpochTimeSecondBase() {
        TimestampBasedMarketServiceSupporter numbering = new TimestampBasedMarketServiceSupporter(10, false);

        assert numbering.computeEpochTime(100) == 10;
        assert numbering.computeEpochTime(110) == 11;
    }

    @Test
    void computeDateTime() {
        TimestampBasedMarketServiceSupporter numbering = new TimestampBasedMarketServiceSupporter(10, true);

        assert numbering.computeDateTime(100).equals(Chrono.utc(1970, 1, 1, 0, 0, 0, 10));
        assert numbering.computeDateTime(12340).equals(Chrono.utc(1970, 1, 1, 0, 0, 1, 234));
    }

    @Test
    void computeDateTimeSecondBase() {
        TimestampBasedMarketServiceSupporter numbering = new TimestampBasedMarketServiceSupporter(10, false);

        assert numbering.computeDateTime(100).equals(Chrono.utc(1970, 1, 1, 0, 0, 10, 0));
        assert numbering.computeDateTime(12340).equals(Chrono.utc(1970, 1, 1, 0, 20, 34, 0));
    }

    @Test
    void computeID() {
        TimestampBasedMarketServiceSupporter numbering = new TimestampBasedMarketServiceSupporter(10, true);

        assert numbering.computeID(Chrono.utc(1970, 1, 1, 0, 0, 12, 345)) == 123450;
        assert numbering.computeID(Chrono.utc(1970, 1, 1, 0, 1, 23, 456)) == 834560;
    }

    @Test
    void computeIDSecondBase() {
        TimestampBasedMarketServiceSupporter numbering = new TimestampBasedMarketServiceSupporter(10, false);

        assert numbering.computeID(Chrono.utc(1970, 1, 1, 0, 0, 12, 345)) == 120;
        assert numbering.computeID(Chrono.utc(1970, 1, 1, 0, 1, 23, 456)) == 830;
    }
}