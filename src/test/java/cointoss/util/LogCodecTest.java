/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

/**
 * @version 2018/05/02 14:36:50
 */
class LogCodecTest {

    @Test
    void deltaIntIncrease() {
        String encoded = LogCodec.encodeDelta(10, 9, 0);
        long decoded = LogCodec.decodeDelta(encoded, 9, 0);
        assert decoded == 10;
    }

    @Test
    void deltaIntDecrease() {
        String encoded = LogCodec.encodeDelta(8, 9, 0);
        long decoded = LogCodec.decodeDelta(encoded, 9, 0);
        assert decoded == 8;
    }

    @Test
    void deltaIntSame() {
        String encoded = LogCodec.encodeDelta(5, 5, 1);
        long decoded = LogCodec.decodeDelta(encoded, 5, 1);
        assert encoded.isEmpty() == false;
        assert decoded == 5;
    }

    @Test
    void deltaIntDefaultValue() {
        String encoded = LogCodec.encodeDelta(5, 5, 0);
        long decoded = LogCodec.decodeDelta(encoded, 5, 0);
        assert encoded.equals("");
        assert decoded == 5;
    }

    @Test
    void deltaLongIncrease() {
        String encoded = LogCodec.encodeDelta(10L, 9L, 0);
        long decoded = LogCodec.decodeDelta(encoded, 9L, 0);
        assert decoded == 10L;
    }

    @Test
    void deltaLongDecrease() {
        String encoded = LogCodec.encodeDelta(8L, 9L, 0);
        long decoded = LogCodec.decodeDelta(encoded, 9L, 0);
        assert decoded == 8L;
    }

    @Test
    void deltaLongSame() {
        String encoded = LogCodec.encodeDelta(5L, 5L, 1);
        long decoded = LogCodec.decodeDelta(encoded, 5L, 1);
        assert encoded.isEmpty() == false;
        assert decoded == 5L;
    }

    @Test
    void deltaLongDefaultValue() {
        String encoded = LogCodec.encodeDelta(5L, 5L, 0);
        long decoded = LogCodec.decodeDelta(encoded, 5L, 0);
        assert encoded.equals("");
        assert decoded == 5L;
    }

    @Test
    void deltaTimeIncrease() {
        ZonedDateTime current = ZonedDateTime.now();
        ZonedDateTime previous = current.minus(300, ChronoUnit.MILLIS);

        String encoded = LogCodec.encodeDelta(current, previous, 0);
        ZonedDateTime decoded = LogCodec.decodeDelta(encoded, previous, 0);
        assert decoded.equals(current);
    }

    @Test
    void deltaTimeDecrease() {
        ZonedDateTime current = ZonedDateTime.now();
        ZonedDateTime previous = current.plus(300, ChronoUnit.MILLIS);

        String encoded = LogCodec.encodeDelta(current, previous, 0);
        ZonedDateTime decoded = LogCodec.decodeDelta(encoded, previous, 0);
        assert decoded.equals(current);
    }

    @Test
    void deltaTimeSame() {
        ZonedDateTime current = ZonedDateTime.now();
        ZonedDateTime previous = current;

        String encoded = LogCodec.encodeDelta(current, previous, 100);
        ZonedDateTime decoded = LogCodec.decodeDelta(encoded, previous, 100);
        assert encoded.isEmpty() == false;
        assert decoded.equals(current);
    }

    @Test
    void deltaTimeDefaultValue() {
        ZonedDateTime current = ZonedDateTime.now();
        ZonedDateTime previous = current;

        String encoded = LogCodec.encodeDelta(current, previous, 0);
        ZonedDateTime decoded = LogCodec.decodeDelta(encoded, previous, 0);
        assert encoded.equals("");
        assert decoded.equals(current);
    }

    @Test
    void deltaIntegralNumIncrease() {
        String encoded = LogCodec.encodeIntegralDelta(Num.ONE, Num.ZERO, 0);
        Num decoded = LogCodec.decodeIntegralDelta(encoded, Num.ZERO, 0);
        assert decoded.is(1);
    }

    @Test
    void deltaIntegralNumDecrease() {
        String encoded = LogCodec.encodeIntegralDelta(Num.ONE, Num.TWO, 0);
        Num decoded = LogCodec.decodeIntegralDelta(encoded, Num.TWO, 0);
        assert decoded.is(1);
    }

    @Test
    void deltaIntegralNumSame() {
        String encoded = LogCodec.encodeIntegralDelta(Num.ONE, Num.ONE, 1);
        Num decoded = LogCodec.decodeIntegralDelta(encoded, Num.ONE, 1);
        assert encoded.isEmpty() == false;
        assert decoded.is(1);
    }

    @Test
    void deltaIntegralNumDefaultValue() {
        String encoded = LogCodec.encodeIntegralDelta(Num.ONE, Num.ONE, 0);
        Num decoded = LogCodec.decodeIntegralDelta(encoded, Num.ONE, 0);
        assert encoded.equals("");
        assert decoded.is(1);
    }

    @Test
    void diffNumIncrease() {
        String encoded = LogCodec.encodeDiff(Num.ONE, Num.ZERO, Num.HUNDRED);
        Num decoded = LogCodec.decodeDiff(encoded, Num.ZERO, Num.HUNDRED);
        assert decoded.is(Num.ONE);
    }

    @Test
    void diffNumDecrease() {
        String encoded = LogCodec.encodeDiff(Num.ONE, Num.TWO, Num.HUNDRED);
        Num decoded = LogCodec.decodeDiff(encoded, Num.TWO, Num.HUNDRED);
        assert decoded.is(Num.ONE);
    }

    @Test
    void diffNumSame() {
        String encoded = LogCodec.encodeDiff(Num.ONE, Num.ONE, Num.HUNDRED);
        Num decoded = LogCodec.decodeDiff(encoded, Num.ONE, Num.HUNDRED);
        assert encoded.equals("");
        assert decoded.is(Num.ONE);
    }

    @Test
    void diffNum() {
        Num current = Num.of(0.12345678);

        String encoded = LogCodec.encodeDiff(current, Num.ZERO);
        Num decoded = LogCodec.decodeDiff(encoded, Num.ZERO);
        assert decoded.is(current);
    }

    @Test
    void diffString() {
        String encoded = LogCodec.encodeDiff("current", "previous");
        String decoded = LogCodec.decodeDiff(encoded, "previous");
        assert decoded.equals("current");
    }

    @Test
    void diffStringSame() {
        String encoded = LogCodec.encodeDiff("current", "current");
        String decoded = LogCodec.decodeDiff(encoded, "current");
        assert encoded.equals("");
        assert decoded.equals("current");
    }

    @Test
    void decodeChar() {
        for (int i = 0; i <= 186; i++) {
            String encoded = LogCodec.encodeInt(i);
            int decoded = LogCodec.decodeInt(encoded.charAt(0));

            assert decoded == i;
        }
    }

    @Test
    void encodeLong() {
        for (long i = -3000; i <= 3000; i++) {
            String encoded = LogCodec.encodeLong(i);
            long decoded = LogCodec.decodeLong(encoded);

            assert decoded == i;
        }
    }
}
