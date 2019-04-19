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

import static cointoss.execution.Execution.*;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.util.Num;

class ExecutionDeltaLoggerTest {

    @Test
    void deltaIntIncrease() {
        String encoded = ExecutionDeltaLogger.encodeDelta(10, 9, 0);
        long decoded = ExecutionDeltaLogger.decodeDelta(encoded, 9, 0);
        assert decoded == 10;
    }

    @Test
    void deltaIntDecrease() {
        String encoded = ExecutionDeltaLogger.encodeDelta(8, 9, 0);
        long decoded = ExecutionDeltaLogger.decodeDelta(encoded, 9, 0);
        assert decoded == 8;
    }

    @Test
    void deltaIntSame() {
        String encoded = ExecutionDeltaLogger.encodeDelta(5, 5, 1);
        long decoded = ExecutionDeltaLogger.decodeDelta(encoded, 5, 1);
        assert encoded.isEmpty() == false;
        assert decoded == 5;
    }

    @Test
    void deltaIntDefaultValue() {
        String encoded = ExecutionDeltaLogger.encodeDelta(5, 5, 0);
        long decoded = ExecutionDeltaLogger.decodeDelta(encoded, 5, 0);
        assert encoded.equals("");
        assert decoded == 5;
    }

    @Test
    void deltaLongIncrease() {
        String encoded = ExecutionDeltaLogger.encodeDelta(10L, 9L, 0);
        long decoded = ExecutionDeltaLogger.decodeDelta(encoded, 9L, 0);
        assert decoded == 10L;
    }

    @Test
    void deltaLongDecrease() {
        String encoded = ExecutionDeltaLogger.encodeDelta(8L, 9L, 0);
        long decoded = ExecutionDeltaLogger.decodeDelta(encoded, 9L, 0);
        assert decoded == 8L;
    }

    @Test
    void deltaLongSame() {
        String encoded = ExecutionDeltaLogger.encodeDelta(5L, 5L, 1);
        long decoded = ExecutionDeltaLogger.decodeDelta(encoded, 5L, 1);
        assert encoded.isEmpty() == false;
        assert decoded == 5L;
    }

    @Test
    void deltaLongDefaultValue() {
        String encoded = ExecutionDeltaLogger.encodeDelta(5L, 5L, 0);
        long decoded = ExecutionDeltaLogger.decodeDelta(encoded, 5L, 0);
        assert encoded.equals("");
        assert decoded == 5L;
    }

    @Test
    void deltaTimeIncrease() {
        ZonedDateTime current = ZonedDateTime.now();
        ZonedDateTime previous = current.minus(300, ChronoUnit.MILLIS);

        String encoded = ExecutionDeltaLogger.encodeDelta(current, previous, 0);
        ZonedDateTime decoded = ExecutionDeltaLogger.decodeDelta(encoded, previous, 0);
        assert decoded.equals(current);
    }

    @Test
    void deltaTimeDecrease() {
        ZonedDateTime current = ZonedDateTime.now();
        ZonedDateTime previous = current.plus(300, ChronoUnit.MILLIS);

        String encoded = ExecutionDeltaLogger.encodeDelta(current, previous, 0);
        ZonedDateTime decoded = ExecutionDeltaLogger.decodeDelta(encoded, previous, 0);
        assert decoded.equals(current);
    }

    @Test
    void deltaTimeSame() {
        ZonedDateTime current = ZonedDateTime.now();
        ZonedDateTime previous = current;

        String encoded = ExecutionDeltaLogger.encodeDelta(current, previous, 100);
        ZonedDateTime decoded = ExecutionDeltaLogger.decodeDelta(encoded, previous, 100);
        assert encoded.isEmpty() == false;
        assert decoded.equals(current);
    }

    @Test
    void deltaTimeDefaultValue() {
        ZonedDateTime current = ZonedDateTime.now();
        ZonedDateTime previous = current;

        String encoded = ExecutionDeltaLogger.encodeDelta(current, previous, 0);
        ZonedDateTime decoded = ExecutionDeltaLogger.decodeDelta(encoded, previous, 0);
        assert encoded.equals("");
        assert decoded.equals(current);
    }

    @Test
    void deltaIntegralNumIncrease() {
        String encoded = ExecutionDeltaLogger.encodeIntegralDelta(Num.ONE, Num.ZERO, 0);
        Num decoded = ExecutionDeltaLogger.decodeIntegralDelta(encoded, Num.ZERO, 0);
        assert decoded.is(1);
    }

    @Test
    void deltaIntegralNumDecrease() {
        String encoded = ExecutionDeltaLogger.encodeIntegralDelta(Num.ONE, Num.TWO, 0);
        Num decoded = ExecutionDeltaLogger.decodeIntegralDelta(encoded, Num.TWO, 0);
        assert decoded.is(1);
    }

    @Test
    void deltaIntegralNumSame() {
        String encoded = ExecutionDeltaLogger.encodeIntegralDelta(Num.ONE, Num.ONE, 1);
        Num decoded = ExecutionDeltaLogger.decodeIntegralDelta(encoded, Num.ONE, 1);
        assert encoded.isEmpty() == false;
        assert decoded.is(1);
    }

    @Test
    void deltaIntegralNumDefaultValue() {
        String encoded = ExecutionDeltaLogger.encodeIntegralDelta(Num.ONE, Num.ONE, 0);
        Num decoded = ExecutionDeltaLogger.decodeIntegralDelta(encoded, Num.ONE, 0);
        assert encoded.equals("");
        assert decoded.is(1);
    }

    @Test
    void diffNumIncrease() {
        String encoded = ExecutionDeltaLogger.encodeDiff(Num.ONE, Num.ZERO, Num.HUNDRED);
        Num decoded = ExecutionDeltaLogger.decodeDiff(encoded, Num.ZERO, Num.HUNDRED);
        assert decoded.is(Num.ONE);
    }

    @Test
    void diffNumDecrease() {
        String encoded = ExecutionDeltaLogger.encodeDiff(Num.ONE, Num.TWO, Num.HUNDRED);
        Num decoded = ExecutionDeltaLogger.decodeDiff(encoded, Num.TWO, Num.HUNDRED);
        assert decoded.is(Num.ONE);
    }

    @Test
    void diffNumSame() {
        String encoded = ExecutionDeltaLogger.encodeDiff(Num.ONE, Num.ONE, Num.HUNDRED);
        Num decoded = ExecutionDeltaLogger.decodeDiff(encoded, Num.ONE, Num.HUNDRED);
        assert encoded.equals("");
        assert decoded.is(Num.ONE);
    }

    @Test
    void diffNum() {
        Num current = Num.of("0.12345678987654321234567898765432123456789876543212345678987654321");
        String encoded = ExecutionDeltaLogger.encodeDiff(current, Num.ZERO);
        Num decoded = ExecutionDeltaLogger.decodeDiff(encoded, Num.ZERO);
        assert decoded.is(current);

        current = Num.of("200000000000000000000000000000000000000000000000000000000000000000000000000");
        encoded = ExecutionDeltaLogger.encodeDiff(current, Num.ZERO);
        decoded = ExecutionDeltaLogger.decodeDiff(encoded, Num.ZERO);
        assert decoded.is(current);
    }

    @Test
    void diffString() {
        String encoded = ExecutionDeltaLogger.encodeDiff("current", "previous");
        String decoded = ExecutionDeltaLogger.decodeDiff(encoded, "previous");
        assert decoded.equals("current");
    }

    @Test
    void diffStringSame() {
        String encoded = ExecutionDeltaLogger.encodeDiff("current", "current");
        String decoded = ExecutionDeltaLogger.decodeDiff(encoded, "current");
        assert encoded.equals("");
        assert decoded.equals("current");
    }

    ExecutionDeltaLogger logger = new ExecutionDeltaLogger();

    @Test
    void compactId() {
        List<Execution> exes = new ArrayList();
        exes.add(Executed.buy(1).price(10).id(10));
        exes.add(Executed.buy(1).price(10).id(12));
        exes.add(Executed.sell(1).price(10).id(13));
        exes.add(Executed.sell(1).price(10).id(19));

        for (int i = 1; i < exes.size(); i++) {
            Execution current = exes.get(i);
            Execution previous = exes.get(i - 1);

            String[] encoded = logger.encode(previous, current);
            Execution decoded = logger.decode(previous, encoded);
            assert decoded.equals(current);
        }
    }

    @Test
    void compactSide() {
        List<Execution> exes = new ArrayList();
        exes.add(Executed.buy(1).price(10));
        exes.add(Executed.buy(1).price(10));
        exes.add(Executed.sell(1).price(10));
        exes.add(Executed.sell(1).price(10));

        for (int i = 1; i < exes.size(); i++) {
            Execution current = exes.get(i);
            Execution previous = exes.get(i - 1);

            String[] encoded = logger.encode(previous, current);
            Execution decoded = logger.decode(previous, encoded);
            assert decoded.equals(current);
        }
    }

    @Test
    void compactDate() {
        List<Execution> exes = new ArrayList();
        exes.add(Executed.buy(1).price(10).date(2018, 5, 1, 10, 0, 0, 0));
        exes.add(Executed.buy(1).price(10).date(2018, 5, 1, 10, 0, 0, 100));
        exes.add(Executed.buy(1).price(10).date(2018, 5, 1, 10, 0, 40, 100));
        exes.add(Executed.buy(1).price(10).date(2018, 5, 1, 10, 33, 40, 100));
        exes.add(Executed.buy(1).price(10).date(2018, 5, 1, 22, 33, 40, 100));
        exes.add(Executed.buy(1).price(10).date(2018, 5, 9, 22, 33, 40, 100));
        exes.add(Executed.buy(1).price(10).date(2018, 11, 9, 22, 33, 40, 100));
        exes.add(Executed.buy(1).price(10).date(2020, 11, 9, 22, 33, 40, 100));

        for (int i = 1; i < exes.size(); i++) {
            Execution current = exes.get(i);
            Execution previous = exes.get(i - 1);

            String[] encoded = logger.encode(previous, current);
            Execution decoded = logger.decode(previous, encoded);
            assert decoded.equals(current);
        }
    }

    @Test
    void compactSize() {
        List<Execution> exes = new ArrayList();
        exes.add(Executed.buy(1).price(10));
        exes.add(Executed.buy(1).price(10));
        exes.add(Executed.buy(2).price(10));
        exes.add(Executed.buy(5.4).price(10));
        exes.add(Executed.buy(3).price(10));
        exes.add(Executed.buy(0.1).price(10));

        for (int i = 1; i < exes.size(); i++) {
            Execution current = exes.get(i);
            Execution previous = exes.get(i - 1);

            String[] encoded = logger.encode(previous, current);
            Execution decoded = logger.decode(previous, encoded);
            assert decoded.equals(current);
        }
    }

    @Test
    void compactPrice() {
        List<Execution> exes = new ArrayList();
        exes.add(Executed.buy(1).price(10));
        exes.add(Executed.buy(1).price(10));
        exes.add(Executed.buy(1).price(12));
        exes.add(Executed.buy(1).price(13.4));
        exes.add(Executed.buy(1).price(10));
        exes.add(Executed.buy(1).price(3.33));

        for (int i = 1; i < exes.size(); i++) {
            Execution current = exes.get(i);
            Execution previous = exes.get(i - 1);

            String[] encoded = logger.encode(previous, current);
            Execution decoded = logger.decode(previous, encoded);
            assert decoded.equals(current);
        }
    }

    @Test
    void compactConsecutiveType() {
        List<Execution> exes = new ArrayList();
        exes.add(Executed.buy(1).price(10).consecutive(Execution.ConsecutiveDifference));
        exes.add(Executed.buy(1).price(10).consecutive(Execution.ConsecutiveDifference));
        exes.add(Executed.buy(1).price(10).consecutive(Execution.ConsecutiveSameBuyer));
        exes.add(Executed.buy(1).price(10).consecutive(Execution.ConsecutiveSameSeller));
        exes.add(Executed.buy(1).price(10).consecutive(Execution.ConsecutiveSameSeller));
        exes.add(Executed.buy(1).price(10).consecutive(Execution.ConsecutiveSameBuyer));
        exes.add(Executed.buy(1).price(10).consecutive(Execution.ConsecutiveDifference));
        exes.add(Executed.buy(1).price(10).consecutive(Execution.ConsecutiveSameBoth));
        exes.add(Executed.buy(1).price(10).consecutive(Execution.ConsecutiveSameBoth));

        for (int i = 1; i < exes.size(); i++) {
            Execution current = exes.get(i);
            Execution previous = exes.get(i - 1);

            String[] encoded = logger.encode(previous, current);
            Execution decoded = logger.decode(previous, encoded);
            assert decoded.equals(current);
        }
    }

    @Test
    void compactDelay() {
        List<Execution> exes = new ArrayList();
        exes.add(Executed.buy(1).price(10).delay(1));
        exes.add(Executed.buy(1).price(10).delay(1));
        exes.add(Executed.buy(1).price(10).delay(3));
        exes.add(Executed.buy(1).price(10).delay(10));
        exes.add(Executed.buy(1).price(10).delay(5));
        exes.add(Executed.buy(1).price(10).delay(Execution.DelayHuge));
        exes.add(Executed.buy(1).price(10).delay(Execution.DelayInestimable));
        exes.add(Executed.buy(1).price(10).delay(Execution.DelayInestimable));

        for (int i = 1; i < exes.size(); i++) {
            Execution current = exes.get(i);
            Execution previous = exes.get(i - 1);

            String[] encoded = logger.encode(previous, current);
            Execution decoded = logger.decode(previous, encoded);
            assert decoded.equals(current);
        }
    }

    @Test
    void complex() {
        List<Execution> exes = new ArrayList();
        exes.add(Executed.buy(3.4094257)
                .id(17003792)
                .date(2017, 3, 6, 10, 28, 2, 873)
                .price(148500)
                .consecutive(ConsecutiveSameSeller)
                .delay(1));
        exes.add(Executed.buy(0.01)
                .id(17003800)
                .date(2017, 3, 6, 10, 28, 23, 523)
                .price(148500)
                .consecutive(ConsecutiveDifference)
                .delay(1));
        exes.add(Executed.buy(0.45)
                .id(17003881)
                .date(2017, 3, 6, 10, 29, 51, 960)
                .price(148450)
                .consecutive(ConsecutiveDifference)
                .delay(1));

        for (int i = 1; i < exes.size(); i++) {
            Execution current = exes.get(i);
            Execution previous = exes.get(i - 1);

            String[] encoded = logger.encode(previous, current);
            Execution decoded = logger.decode(previous, encoded);
            assert decoded.equals(current);
        }
    }
}
