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

import static cointoss.Execution.*;
import static cointoss.MarketTestSupport.*;
import static cointoss.Side.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.MarketTestSupport.ChainableExecution;
import cointoss.backtest.VerifiableMarketService;

/**
 * @version 2018/07/12 9:52:30
 */
class MarketServiceTest {

    MarketService service = new VerifiableMarketService();

    @Test
    void compactId() {
        List<Execution> exes = new ArrayList();
        exes.add(buy(1, 10).id(10));
        exes.add(buy(1, 10).id(12));
        exes.add(sell(1, 10).id(13));
        exes.add(sell(1, 10).id(19));

        for (int i = 1; i < exes.size(); i++) {
            Execution current = exes.get(i);
            Execution previous = exes.get(i - 1);

            String[] encoded = service.encode(previous, current);
            Execution decoded = service.decode(previous, encoded);
            assert decoded.equals(current);
        }
    }

    @Test
    void compactSide() {
        List<Execution> exes = new ArrayList();
        exes.add(buy(1, 10));
        exes.add(buy(1, 10));
        exes.add(sell(1, 10));
        exes.add(sell(1, 10));

        for (int i = 1; i < exes.size(); i++) {
            Execution current = exes.get(i);
            Execution previous = exes.get(i - 1);

            String[] encoded = service.encode(previous, current);
            Execution decoded = service.decode(previous, encoded);
            assert decoded.equals(current);
        }
    }

    @Test
    void compactDate() {
        List<Execution> exes = new ArrayList();
        exes.add(buy(1, 10).date(2018, 5, 1, 10, 0, 0, 0));
        exes.add(buy(1, 10).date(2018, 5, 1, 10, 0, 0, 100));
        exes.add(buy(1, 10).date(2018, 5, 1, 10, 0, 40, 100));
        exes.add(buy(1, 10).date(2018, 5, 1, 10, 33, 40, 100));
        exes.add(buy(1, 10).date(2018, 5, 1, 22, 33, 40, 100));
        exes.add(buy(1, 10).date(2018, 5, 9, 22, 33, 40, 100));
        exes.add(buy(1, 10).date(2018, 11, 9, 22, 33, 40, 100));
        exes.add(buy(1, 10).date(2020, 11, 9, 22, 33, 40, 100));

        for (int i = 1; i < exes.size(); i++) {
            Execution current = exes.get(i);
            Execution previous = exes.get(i - 1);

            String[] encoded = service.encode(previous, current);
            Execution decoded = service.decode(previous, encoded);
            assert decoded.equals(current);
        }
    }

    @Test
    void compactSize() {
        List<Execution> exes = new ArrayList();
        exes.add(buy(1, 10));
        exes.add(buy(1, 10));
        exes.add(buy(2, 10));
        exes.add(buy(5.4, 10));
        exes.add(buy(3, 10));
        exes.add(buy(0.1, 10));

        for (int i = 1; i < exes.size(); i++) {
            Execution current = exes.get(i);
            Execution previous = exes.get(i - 1);

            String[] encoded = service.encode(previous, current);
            Execution decoded = service.decode(previous, encoded);
            assert decoded.equals(current);
        }
    }

    @Test
    void compactPrice() {
        List<Execution> exes = new ArrayList();
        exes.add(buy(1, 10));
        exes.add(buy(1, 10));
        exes.add(buy(1, 12));
        exes.add(buy(1, 13.4));
        exes.add(buy(1, 10));
        exes.add(buy(1, 3.33));

        for (int i = 1; i < exes.size(); i++) {
            Execution current = exes.get(i);
            Execution previous = exes.get(i - 1);

            String[] encoded = service.encode(previous, current);
            Execution decoded = service.decode(previous, encoded);
            assert decoded.equals(current);
        }
    }

    @Test
    void compactConsecutiveType() {
        List<Execution> exes = new ArrayList();
        exes.add(buy(1, 10).consecutive(Execution.ConsecutiveDifference));
        exes.add(buy(1, 10).consecutive(Execution.ConsecutiveDifference));
        exes.add(buy(1, 10).consecutive(Execution.ConsecutiveSameBuyer));
        exes.add(buy(1, 10).consecutive(Execution.ConsecutiveSameSeller));
        exes.add(buy(1, 10).consecutive(Execution.ConsecutiveSameSeller));
        exes.add(buy(1, 10).consecutive(Execution.ConsecutiveSameBuyer));
        exes.add(buy(1, 10).consecutive(Execution.ConsecutiveDifference));
        exes.add(buy(1, 10).consecutive(Execution.ConsecutiveSameBoth));
        exes.add(buy(1, 10).consecutive(Execution.ConsecutiveSameBoth));

        for (int i = 1; i < exes.size(); i++) {
            Execution current = exes.get(i);
            Execution previous = exes.get(i - 1);

            String[] encoded = service.encode(previous, current);
            Execution decoded = service.decode(previous, encoded);
            assert decoded.equals(current);
        }
    }

    @Test
    void compactDelay() {
        List<Execution> exes = new ArrayList();
        exes.add(buy(1, 10).delay(1));
        exes.add(buy(1, 10).delay(1));
        exes.add(buy(1, 10).delay(3));
        exes.add(buy(1, 10).delay(10));
        exes.add(buy(1, 10).delay(5));
        exes.add(buy(1, 10).delay(Execution.DelayHuge));
        exes.add(buy(1, 10).delay(Execution.DelayInestimable));
        exes.add(buy(1, 10).delay(Execution.DelayInestimable));

        for (int i = 1; i < exes.size(); i++) {
            Execution current = exes.get(i);
            Execution previous = exes.get(i - 1);

            String[] encoded = service.encode(previous, current);
            Execution decoded = service.decode(previous, encoded);
            assert decoded.equals(current);
        }
    }

    @Test
    void complex() {
        List<Execution> exes = new ArrayList();
        exes.add(new ChainableExecution().id(17003792)
                .date(2017, 3, 6, 10, 28, 2, 873)
                .side(BUY)
                .price(148500)
                .size(3.4094257)
                .consecutive(ConsecutiveSameSeller)
                .delay(1));
        exes.add(new ChainableExecution().id(17003800)
                .date(2017, 3, 6, 10, 28, 23, 523)
                .side(BUY)
                .price(148500)
                .size(0.01)
                .consecutive(ConsecutiveDifference)
                .delay(1));
        exes.add(new ChainableExecution().id(17003881)
                .date(2017, 3, 6, 10, 29, 51, 960)
                .side(BUY)
                .price(148450)
                .size(0.45)
                .consecutive(ConsecutiveDifference)
                .delay(1));

        for (int i = 1; i < exes.size(); i++) {
            Execution current = exes.get(i);
            Execution previous = exes.get(i - 1);

            String[] encoded = service.encode(previous, current);
            Execution decoded = service.decode(previous, encoded);
            assert decoded.equals(current);
        }
    }
}
