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

class DeltaLogCompressorTest {

    @Test
    void deltaIntIncrease() {
        String encoded = DeltaLogCompressor.encodeDelta(10, 9, 0);
        long decoded = DeltaLogCompressor.decodeDelta(encoded, 9, 0);
        assert decoded == 10;
    }

    @Test
    void deltaIntDecrease() {
        String encoded = DeltaLogCompressor.encodeDelta(8, 9, 0);
        long decoded = DeltaLogCompressor.decodeDelta(encoded, 9, 0);
        assert decoded == 8;
    }

    @Test
    void deltaIntSame() {
        String encoded = DeltaLogCompressor.encodeDelta(5, 5, 1);
        long decoded = DeltaLogCompressor.decodeDelta(encoded, 5, 1);
        assert encoded.isEmpty() == false;
        assert decoded == 5;
    }

    @Test
    void deltaIntDefaultValue() {
        String encoded = DeltaLogCompressor.encodeDelta(5, 5, 0);
        long decoded = DeltaLogCompressor.decodeDelta(encoded, 5, 0);
        assert encoded.equals("");
        assert decoded == 5;
    }

    @Test
    void deltaLongIncrease() {
        String encoded = DeltaLogCompressor.encodeDelta(10L, 9L, 0);
        long decoded = DeltaLogCompressor.decodeDelta(encoded, 9L, 0);
        assert decoded == 10L;
    }

    @Test
    void deltaLongDecrease() {
        String encoded = DeltaLogCompressor.encodeDelta(8L, 9L, 0);
        long decoded = DeltaLogCompressor.decodeDelta(encoded, 9L, 0);
        assert decoded == 8L;
    }

    @Test
    void deltaLongSame() {
        String encoded = DeltaLogCompressor.encodeDelta(5L, 5L, 1);
        long decoded = DeltaLogCompressor.decodeDelta(encoded, 5L, 1);
        assert encoded.isEmpty() == false;
        assert decoded == 5L;
    }

    @Test
    void deltaLongDefaultValue() {
        String encoded = DeltaLogCompressor.encodeDelta(5L, 5L, 0);
        long decoded = DeltaLogCompressor.decodeDelta(encoded, 5L, 0);
        assert encoded.equals("");
        assert decoded == 5L;
    }

    @Test
    void deltaTimeIncrease() {
        ZonedDateTime current = ZonedDateTime.now();
        ZonedDateTime previous = current.minus(300, ChronoUnit.MILLIS);

        String encoded = DeltaLogCompressor.encodeDelta(current, previous, 0);
        ZonedDateTime decoded = DeltaLogCompressor.decodeDelta(encoded, previous, 0);
        assert decoded.equals(current);
    }

    @Test
    void deltaTimeDecrease() {
        ZonedDateTime current = ZonedDateTime.now();
        ZonedDateTime previous = current.plus(300, ChronoUnit.MILLIS);

        String encoded = DeltaLogCompressor.encodeDelta(current, previous, 0);
        ZonedDateTime decoded = DeltaLogCompressor.decodeDelta(encoded, previous, 0);
        assert decoded.equals(current);
    }

    @Test
    void deltaTimeSame() {
        ZonedDateTime current = ZonedDateTime.now();
        ZonedDateTime previous = current;

        String encoded = DeltaLogCompressor.encodeDelta(current, previous, 100);
        ZonedDateTime decoded = DeltaLogCompressor.decodeDelta(encoded, previous, 100);
        assert encoded.isEmpty() == false;
        assert decoded.equals(current);
    }

    @Test
    void deltaTimeDefaultValue() {
        ZonedDateTime current = ZonedDateTime.now();
        ZonedDateTime previous = current;

        String encoded = DeltaLogCompressor.encodeDelta(current, previous, 0);
        ZonedDateTime decoded = DeltaLogCompressor.decodeDelta(encoded, previous, 0);
        assert encoded.equals("");
        assert decoded.equals(current);
    }

    @Test
    void deltaIntegralNumIncrease() {
        String encoded = DeltaLogCompressor.encodeIntegralDelta(Num.ONE, Num.ZERO, 0);
        Num decoded = DeltaLogCompressor.decodeIntegralDelta(encoded, Num.ZERO, 0);
        assert decoded.is(1);
    }

    @Test
    void deltaIntegralNumDecrease() {
        String encoded = DeltaLogCompressor.encodeIntegralDelta(Num.ONE, Num.TWO, 0);
        Num decoded = DeltaLogCompressor.decodeIntegralDelta(encoded, Num.TWO, 0);
        assert decoded.is(1);
    }

    @Test
    void deltaIntegralNumSame() {
        String encoded = DeltaLogCompressor.encodeIntegralDelta(Num.ONE, Num.ONE, 1);
        Num decoded = DeltaLogCompressor.decodeIntegralDelta(encoded, Num.ONE, 1);
        assert encoded.isEmpty() == false;
        assert decoded.is(1);
    }

    @Test
    void deltaIntegralNumDefaultValue() {
        String encoded = DeltaLogCompressor.encodeIntegralDelta(Num.ONE, Num.ONE, 0);
        Num decoded = DeltaLogCompressor.decodeIntegralDelta(encoded, Num.ONE, 0);
        assert encoded.equals("");
        assert decoded.is(1);
    }

    @Test
    void diffNumIncrease() {
        String encoded = DeltaLogCompressor.encodeDiff(Num.ONE, Num.ZERO, Num.HUNDRED);
        Num decoded = DeltaLogCompressor.decodeDiff(encoded, Num.ZERO, Num.HUNDRED);
        assert decoded.is(Num.ONE);
    }

    @Test
    void diffNumDecrease() {
        String encoded = DeltaLogCompressor.encodeDiff(Num.ONE, Num.TWO, Num.HUNDRED);
        Num decoded = DeltaLogCompressor.decodeDiff(encoded, Num.TWO, Num.HUNDRED);
        assert decoded.is(Num.ONE);
    }

    @Test
    void diffNumSame() {
        String encoded = DeltaLogCompressor.encodeDiff(Num.ONE, Num.ONE, Num.HUNDRED);
        Num decoded = DeltaLogCompressor.decodeDiff(encoded, Num.ONE, Num.HUNDRED);
        assert encoded.equals("");
        assert decoded.is(Num.ONE);
    }

    @Test
    void diffNum() {
        Num current = Num.of("0.12345678987654321234567898765432123456789876543212345678987654321");
        String encoded = DeltaLogCompressor.encodeDiff(current, Num.ZERO);
        Num decoded = DeltaLogCompressor.decodeDiff(encoded, Num.ZERO);
        assert decoded.is(current);

        current = Num.of("200000000000000000000000000000000000000000000000000000000000000000000000000");
        encoded = DeltaLogCompressor.encodeDiff(current, Num.ZERO);
        decoded = DeltaLogCompressor.decodeDiff(encoded, Num.ZERO);
        assert decoded.is(current);
    }

    @Test
    void diffString() {
        String encoded = DeltaLogCompressor.encodeDiff("current", "previous");
        String decoded = DeltaLogCompressor.decodeDiff(encoded, "previous");
        assert decoded.equals("current");
    }

    @Test
    void diffStringSame() {
        String encoded = DeltaLogCompressor.encodeDiff("current", "current");
        String decoded = DeltaLogCompressor.decodeDiff(encoded, "current");
        assert encoded.equals("");
        assert decoded.equals("current");
    }
}
