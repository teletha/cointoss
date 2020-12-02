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

class TimestampIDTest {

    @Test
    void decode() {
        TimestampID numbering = new TimestampID(true, 10);

        assert numbering.decode(100) == 10;
        assert numbering.decode(110) == 11;
    }

    @Test
    void decodeSecondBase() {
        TimestampID numbering = new TimestampID(false, 10);

        assert numbering.decode(100) == 10;
        assert numbering.decode(110) == 11;
    }

    @Test
    void decodeAsDate() {
        TimestampID numbering = new TimestampID(true, 10);
    
        assert numbering.decodeAsDate(100).equals(Chrono.utc(1970, 1, 1, 0, 0, 0, 10));
        assert numbering.decodeAsDate(12340).equals(Chrono.utc(1970, 1, 1, 0, 0, 1, 234));
    }

    @Test
    void decodeAsDateSecondBase() {
        TimestampID numbering = new TimestampID(false, 10);
    
        assert numbering.decodeAsDate(100).equals(Chrono.utc(1970, 1, 1, 0, 0, 10, 0));
        assert numbering.decodeAsDate(12340).equals(Chrono.utc(1970, 1, 1, 0, 20, 34, 0));
    }

    @Test
    void encode() {
        TimestampID numbering = new TimestampID(true, 10);

        assert numbering.encode(Chrono.utc(1970, 1, 1, 0, 0, 12, 345)) == 123450;
        assert numbering.encode(Chrono.utc(1970, 1, 1, 0, 1, 23, 456)) == 834560;
    }

    @Test
    void encodeSecondBase() {
        TimestampID numbering = new TimestampID(false, 10);

        assert numbering.encode(Chrono.utc(1970, 1, 1, 0, 0, 12, 345)) == 120;
        assert numbering.encode(Chrono.utc(1970, 1, 1, 0, 1, 23, 456)) == 830;
    }
}
