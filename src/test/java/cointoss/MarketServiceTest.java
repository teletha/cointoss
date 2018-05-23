/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import static cointoss.MarketTestSupport.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.backtest.TestableMarketService;

/**
 * @version 2018/05/23 18:34:39
 */
class MarketServiceTest {

    MarketService service = new TestableMarketService();

    @Test
    void compactId() {
        List<Execution> exes = new ArrayList();
        exes.add(buy(10, 1).id(10));
        exes.add(buy(10, 1).id(12));
        exes.add(sell(10, 1).id(13));
        exes.add(sell(10, 1).id(19));

        for (int i = 1; i < exes.size(); i++) {
            Execution current = exes.get(i);
            Execution previous = exes.get(i - 1);

            String[] encoded = service.encode(current, previous);
            Execution decoded = service.decode(encoded, previous);
            assert decoded.equals(current);
        }
    }

    @Test
    void compactSide() {
        List<Execution> exes = new ArrayList();
        exes.add(buy(10, 1));
        exes.add(buy(10, 1));
        exes.add(sell(10, 1));
        exes.add(sell(10, 1));

        for (int i = 1; i < exes.size(); i++) {
            Execution current = exes.get(i);
            Execution previous = exes.get(i - 1);

            String[] encoded = service.encode(current, previous);
            Execution decoded = service.decode(encoded, previous);
            assert decoded.equals(current);
        }
    }

    @Test
    void compactDate() {
        List<Execution> exes = new ArrayList();
        exes.add(buy(10, 1).date(2018, 5, 1, 10, 0, 0, 0));
        exes.add(buy(10, 1).date(2018, 5, 1, 10, 0, 0, 100));
        exes.add(buy(10, 1).date(2018, 5, 1, 10, 0, 40, 100));
        exes.add(buy(10, 1).date(2018, 5, 1, 10, 33, 40, 100));
        exes.add(buy(10, 1).date(2018, 5, 1, 22, 33, 40, 100));
        exes.add(buy(10, 1).date(2018, 5, 9, 22, 33, 40, 100));
        exes.add(buy(10, 1).date(2018, 11, 9, 22, 33, 40, 100));
        exes.add(buy(10, 1).date(2020, 11, 9, 22, 33, 40, 100));

        for (int i = 1; i < exes.size(); i++) {
            Execution current = exes.get(i);
            Execution previous = exes.get(i - 1);

            String[] encoded = service.encode(current, previous);
            Execution decoded = service.decode(encoded, previous);
            assert decoded.equals(current);
        }
    }

    @Test
    void compactSize() {
        List<Execution> exes = new ArrayList();
        exes.add(buy(10, 1));
        exes.add(buy(10, 1));
        exes.add(buy(10, 2));
        exes.add(buy(10, 5.4));
        exes.add(buy(10, 3));
        exes.add(buy(10, 0.1));

        for (int i = 1; i < exes.size(); i++) {
            Execution current = exes.get(i);
            Execution previous = exes.get(i - 1);

            String[] encoded = service.encode(current, previous);
            Execution decoded = service.decode(encoded, previous);
            assert decoded.equals(current);
        }
    }

    @Test
    void compactPrice() {
        List<Execution> exes = new ArrayList();
        exes.add(buy(10, 1));
        exes.add(buy(10, 1));
        exes.add(buy(12, 1));
        exes.add(buy(13.4, 1));
        exes.add(buy(10, 1));
        exes.add(buy(3.33, 1));

        for (int i = 1; i < exes.size(); i++) {
            Execution current = exes.get(i);
            Execution previous = exes.get(i - 1);

            String[] encoded = service.encode(current, previous);
            Execution decoded = service.decode(encoded, previous);
            assert decoded.equals(current);
        }
    }

    @Test
    void compactConsecutiveType() {
        List<Execution> exes = new ArrayList();
        exes.add(buy(10, 1).consecutive(Execution.ConsecutiveDifference));
        exes.add(buy(10, 1).consecutive(Execution.ConsecutiveDifference));
        exes.add(buy(10, 1).consecutive(Execution.ConsecutiveSameBuyer));
        exes.add(buy(10, 1).consecutive(Execution.ConsecutiveSameSeller));
        exes.add(buy(10, 1).consecutive(Execution.ConsecutiveSameSeller));
        exes.add(buy(10, 1).consecutive(Execution.ConsecutiveSameBuyer));
        exes.add(buy(10, 1).consecutive(Execution.ConsecutiveDifference));

        for (int i = 1; i < exes.size(); i++) {
            Execution current = exes.get(i);
            Execution previous = exes.get(i - 1);

            String[] encoded = service.encode(current, previous);
            Execution decoded = service.decode(encoded, previous);
            assert decoded.equals(current);
        }
    }
}
