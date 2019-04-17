/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.execution;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import cointoss.util.Num;

/**
 * @version 2018/05/23 17:17:21
 */
class DeltaLogWriterTest {

    @Test
    void deltaIntIncrease() {
        String encoded = DeltaLogWriter.encodeDelta(10, 9, 0);
        long decoded = DeltaLogWriter.decodeDelta(encoded, 9, 0);
        assert decoded == 10;
    }

    @Test
    void deltaIntDecrease() {
        String encoded = DeltaLogWriter.encodeDelta(8, 9, 0);
        long decoded = DeltaLogWriter.decodeDelta(encoded, 9, 0);
        assert decoded == 8;
    }

    @Test
    void deltaIntSame() {
        String encoded = DeltaLogWriter.encodeDelta(5, 5, 1);
        long decoded = DeltaLogWriter.decodeDelta(encoded, 5, 1);
        assert encoded.isEmpty() == false;
        assert decoded == 5;
    }

    @Test
    void deltaIntDefaultValue() {
        String encoded = DeltaLogWriter.encodeDelta(5, 5, 0);
        long decoded = DeltaLogWriter.decodeDelta(encoded, 5, 0);
        assert encoded.equals("");
        assert decoded == 5;
    }

    @Test
    void deltaLongIncrease() {
        String encoded = DeltaLogWriter.encodeDelta(10L, 9L, 0);
        long decoded = DeltaLogWriter.decodeDelta(encoded, 9L, 0);
        assert decoded == 10L;
    }

    @Test
    void deltaLongDecrease() {
        String encoded = DeltaLogWriter.encodeDelta(8L, 9L, 0);
        long decoded = DeltaLogWriter.decodeDelta(encoded, 9L, 0);
        assert decoded == 8L;
    }

    @Test
    void deltaLongSame() {
        String encoded = DeltaLogWriter.encodeDelta(5L, 5L, 1);
        long decoded = DeltaLogWriter.decodeDelta(encoded, 5L, 1);
        assert encoded.isEmpty() == false;
        assert decoded == 5L;
    }

    @Test
    void deltaLongDefaultValue() {
        String encoded = DeltaLogWriter.encodeDelta(5L, 5L, 0);
        long decoded = DeltaLogWriter.decodeDelta(encoded, 5L, 0);
        assert encoded.equals("");
        assert decoded == 5L;
    }

    @Test
    void deltaTimeIncrease() {
        ZonedDateTime current = ZonedDateTime.now();
        ZonedDateTime previous = current.minus(300, ChronoUnit.MILLIS);

        String encoded = DeltaLogWriter.encodeDelta(current, previous, 0);
        ZonedDateTime decoded = DeltaLogWriter.decodeDelta(encoded, previous, 0);
        assert decoded.equals(current);
    }

    @Test
    void deltaTimeDecrease() {
        ZonedDateTime current = ZonedDateTime.now();
        ZonedDateTime previous = current.plus(300, ChronoUnit.MILLIS);

        String encoded = DeltaLogWriter.encodeDelta(current, previous, 0);
        ZonedDateTime decoded = DeltaLogWriter.decodeDelta(encoded, previous, 0);
        assert decoded.equals(current);
    }

    @Test
    void deltaTimeSame() {
        ZonedDateTime current = ZonedDateTime.now();
        ZonedDateTime previous = current;

        String encoded = DeltaLogWriter.encodeDelta(current, previous, 100);
        ZonedDateTime decoded = DeltaLogWriter.decodeDelta(encoded, previous, 100);
        assert encoded.isEmpty() == false;
        assert decoded.equals(current);
    }

    @Test
    void deltaTimeDefaultValue() {
        ZonedDateTime current = ZonedDateTime.now();
        ZonedDateTime previous = current;

        String encoded = DeltaLogWriter.encodeDelta(current, previous, 0);
        ZonedDateTime decoded = DeltaLogWriter.decodeDelta(encoded, previous, 0);
        assert encoded.equals("");
        assert decoded.equals(current);
    }

    @Test
    void deltaIntegralNumIncrease() {
        String encoded = DeltaLogWriter.encodeIntegralDelta(Num.ONE, Num.ZERO, 0);
        Num decoded = DeltaLogWriter.decodeIntegralDelta(encoded, Num.ZERO, 0);
        assert decoded.is(1);
    }

    @Test
    void deltaIntegralNumDecrease() {
        String encoded = DeltaLogWriter.encodeIntegralDelta(Num.ONE, Num.TWO, 0);
        Num decoded = DeltaLogWriter.decodeIntegralDelta(encoded, Num.TWO, 0);
        assert decoded.is(1);
    }

    @Test
    void deltaIntegralNumSame() {
        String encoded = DeltaLogWriter.encodeIntegralDelta(Num.ONE, Num.ONE, 1);
        Num decoded = DeltaLogWriter.decodeIntegralDelta(encoded, Num.ONE, 1);
        assert encoded.isEmpty() == false;
        assert decoded.is(1);
    }

    @Test
    void deltaIntegralNumDefaultValue() {
        String encoded = DeltaLogWriter.encodeIntegralDelta(Num.ONE, Num.ONE, 0);
        Num decoded = DeltaLogWriter.decodeIntegralDelta(encoded, Num.ONE, 0);
        assert encoded.equals("");
        assert decoded.is(1);
    }

    @Test
    void diffNumIncrease() {
        String encoded = DeltaLogWriter.encodeDiff(Num.ONE, Num.ZERO, Num.HUNDRED);
        Num decoded = DeltaLogWriter.decodeDiff(encoded, Num.ZERO, Num.HUNDRED);
        assert decoded.is(Num.ONE);
    }

    @Test
    void diffNumDecrease() {
        String encoded = DeltaLogWriter.encodeDiff(Num.ONE, Num.TWO, Num.HUNDRED);
        Num decoded = DeltaLogWriter.decodeDiff(encoded, Num.TWO, Num.HUNDRED);
        assert decoded.is(Num.ONE);
    }

    @Test
    void diffNumSame() {
        String encoded = DeltaLogWriter.encodeDiff(Num.ONE, Num.ONE, Num.HUNDRED);
        Num decoded = DeltaLogWriter.decodeDiff(encoded, Num.ONE, Num.HUNDRED);
        assert encoded.equals("");
        assert decoded.is(Num.ONE);
    }

    @Test
    void diffNum() {
        Num current = Num.of("0.12345678987654321234567898765432123456789876543212345678987654321");
        String encoded = DeltaLogWriter.encodeDiff(current, Num.ZERO);
        Num decoded = DeltaLogWriter.decodeDiff(encoded, Num.ZERO);
        assert decoded.is(current);

        current = Num.of("200000000000000000000000000000000000000000000000000000000000000000000000000");
        encoded = DeltaLogWriter.encodeDiff(current, Num.ZERO);
        decoded = DeltaLogWriter.decodeDiff(encoded, Num.ZERO);
        assert decoded.is(current);
    }

    @Test
    void diffString() {
        String encoded = DeltaLogWriter.encodeDiff("current", "previous");
        String decoded = DeltaLogWriter.decodeDiff(encoded, "previous");
        assert decoded.equals("current");
    }

    @Test
    void diffStringSame() {
        String encoded = DeltaLogWriter.encodeDiff("current", "current");
        String decoded = DeltaLogWriter.decodeDiff(encoded, "current");
        assert encoded.equals("");
        assert decoded.equals("current");
    }

    @Test
    void decodeChar() {
        for (int i = 0; i <= 186; i++) {
            String encoded = DeltaLogWriter.encodeInt(i);
            int decoded = DeltaLogWriter.decodeInt(encoded.charAt(0));

            assert decoded == i;
        }
    }

    @Test
    void encodeLong() {
        for (long i = -3000; i <= 3000; i++) {
            String encoded = DeltaLogWriter.encodeLong(i);
            long decoded = DeltaLogWriter.decodeLong(encoded);

            assert decoded == i;
        }
    }
}
