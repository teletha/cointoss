/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.trade;

import org.junit.jupiter.api.Test;

import cointoss.Direction;
import cointoss.execution.Execution;
import cointoss.trade.extension.HoldTimePart;
import cointoss.trade.extension.PricePart;
import cointoss.trade.extension.ScenePart;
import cointoss.trade.extension.SidePart;
import cointoss.trade.extension.SizePart;
import cointoss.trade.extension.TradeTest;
import cointoss.util.Num;

class TraderSnapshotTest extends TraderTestSupport {

    @TradeTest
    void realizedProfit(ScenePart scene, SidePart side, SizePart size, PricePart price) {
        scenario(scene, side, size, price, new HoldTimePart(5 * 60));
        for (int i = 0; i <= 4; i++) {
            Snapshot notExitedYet = snapshotAt(epochAfterMinute(i));
            assert notExitedYet.realizedProfit().is(0);
        }

        for (int i = 6; i <= 10; i++) {
            Snapshot alreadyExited = snapshotAt(epochAfterMinute(i));
            switch (scene) {
            case ExitCompletely:
            case ExitMultiple:
            case ExitSeparately:
            case ExitCanceledThenOtherExitCompletely:
                assert alreadyExited.realizedProfit().is(price.diff * size.num * side.sign);
                break;

            case ExitPartially:
            case ExitPartiallyCancelled:
            case EntryPartiallyCanceledAndExitCompletely:
                assert alreadyExited.realizedProfit().is(price.diff * size.half * side.sign);
                break;

            default:
                assert alreadyExited.realizedProfit().is(0);
                break;
            }
        }
    }

    @TradeTest
    void unrealizedProfit(ScenePart scene, SidePart side, SizePart size, PricePart price) {
        scenario(scene, side, size, price, new HoldTimePart(5 * 60));
        for (int i = 1; i <= 4; i++) {
            Snapshot notExitedYet = snapshotAt(epochAfterMinute(i));
            switch (scene) {
            case Entry:
            case EntryCanceled:
                assert notExitedYet.unrealizedProfit(price.profitN).is(0);
                assert notExitedYet.unrealizedProfit(price.profitN.multiply(100)).is(0);
                break;

            case EntryPartially:
            case EntryPartiallyCanceled:
            case EntryPartiallyCanceledAndExitCompletely:
                assert notExitedYet.unrealizedProfit(price.exitN).is(price.diff * size.half * side.sign);
                assert notExitedYet.unrealizedProfit(price.entryN.plus(price.diffHalfN)).is(price.diffHalf * size.half * side.sign);
                assert notExitedYet.unrealizedProfit(price.entryN.minus(price.diffHalfN)).is(-price.diffHalf * size.half * side.sign);
                break;

            default:
                assert notExitedYet.unrealizedProfit(price.exitN).is(price.diff * size.num * side.sign);
                assert notExitedYet.unrealizedProfit(price.entryN.plus(price.diffHalfN)).is(price.diffHalf * size.num * side.sign);
                assert notExitedYet.unrealizedProfit(price.entryN.minus(price.diffHalfN)).is(-price.diffHalf * size.num * side.sign);
                break;
            }
        }

        for (int i = 6; i <= 10; i++) {
            Snapshot alreadyExited = snapshotAt(epochAfterMinute(i));
            switch (scene) {
            case Entry:
            case EntryCanceled:
            case EntryPartiallyCanceledAndExitCompletely:
            case ExitCompletely:
            case ExitMultiple:
            case ExitSeparately:
            case ExitCanceledThenOtherExitCompletely:
                assert alreadyExited.unrealizedProfit(price.profitN).is(0);
                assert alreadyExited.unrealizedProfit(price.profitN.multiply(100)).is(0);
                break;

            case EntryPartially:
            case EntryPartiallyCanceled:
            case ExitPartially:
            case ExitPartiallyCancelled:
                assert alreadyExited.unrealizedProfit(price.exitN).is(price.diff * size.half * side.sign);
                assert alreadyExited.unrealizedProfit(price.entryN.plus(price.diffHalfN)).is(price.diffHalf * size.half * side.sign);
                assert alreadyExited.unrealizedProfit(price.entryN.minus(price.diffHalfN)).is(-price.diffHalf * size.half * side.sign);
                break;

            default:
                assert alreadyExited.unrealizedProfit(price.exitN).is(price.diff * size.num * side.sign);
                assert alreadyExited.unrealizedProfit(price.entryN.plus(price.diffHalfN)).is(price.diffHalf * size.num * side.sign);
                assert alreadyExited.unrealizedProfit(price.entryN.minus(price.diffHalfN)).is(-price.diffHalf * size.num * side.sign);
                break;
            }
        }
    }

    @Test
    void snapshotFirstProfitSecondLoss() {
        entryAndExit(Execution.with.buy(1).price(10), Execution.with.sell(1).price(20).date(afterMinute(5)));

        // past
        Profitable snapshot = snapshotAt(epochAfterMinute(1));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(12)).is(2);

        // future
        snapshot = snapshotAt(epochAfterMinute(6));
        assert snapshot.realizedProfit().is(10);
        assert snapshot.unrealizedProfit(Num.of(25)).is(0);

        entryAndExit(Execution.with.buy(1).price(10).date(afterMinute(10)), Execution.with.sell(1).price(5).date(afterMinute(15)));

        // past
        snapshot = snapshotAt(epochAfterMinute(11));
        assert snapshot.realizedProfit().is(10);
        assert snapshot.unrealizedProfit(Num.of(8)).is(-2);

        // future
        snapshot = snapshotAt(epochAfterMinute(16));
        assert snapshot.realizedProfit().is(5);
        assert snapshot.unrealizedProfit(Num.of(2)).is(0);
    }

    @Test
    void snapshotDontCareSeconds() {
        when(now(), v -> new Scenario() {
            @Override
            protected void entry() {
                entry(Direction.BUY, 1.5, s -> s.make(10));
            }

            @Override
            protected void exit() {
            }
        });

        market.perform(Execution.with.buy(0.5).price(9).date(afterSecond(20)));
        market.perform(Execution.with.buy(0.5).price(9).date(afterSecond(40)));
        market.perform(Execution.with.buy(0.5).price(9).date(afterSecond(60)));

        // past
        Profitable snapshot = snapshotAt(epochAfterSecond(30));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(6)).is(0);

        // past
        snapshot = snapshotAt(epochAfterSecond(59));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(6)).is(0);

        // now
        snapshot = snapshotAt(epochAfterSecond(60));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(12)).is(3);

        // future
        snapshot = snapshotAt(epochAfterSecond(80));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(12)).is(3);
    }

    @Test
    void snapshotShortLongLong() {
        entryAndExitPartial(Execution.with.sell(2).price(20), Execution.with.buy(2).price(10).date(afterMinute(5)), 1);
        entry(Execution.with.buy(1).price(15).date(afterMinute(7)));
        entry(Execution.with.buy(1).price(17).date(afterMinute(9)));

        // past
        Profitable snapshot = snapshotAt(epochAfterMinute(1));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(12)).is(16);

        // exit first scenario partially
        snapshot = snapshotAt(epochAfterMinute(6));
        assert snapshot.realizedProfit().is(10);
        assert snapshot.unrealizedProfit(Num.of(18)).is(2);

        // entry second scenario
        snapshot = snapshotAt(epochAfterMinute(8));
        assert snapshot.realizedProfit().is(10);
        assert snapshot.unrealizedProfit(Num.of(18)).is(5);

        // entry third scenario
        snapshot = snapshotAt(epochAfterMinute(10));
        assert snapshot.realizedProfit().is(10);
        assert snapshot.unrealizedProfit(Num.of(19)).is(7);
    }

    @Test
    void snapshotLongShortShort() {
        entryAndExitPartial(Execution.with.buy(2).price(10), Execution.with.sell(2).price(20).date(afterMinute(5)), 1);
        entry(Execution.with.sell(1).price(17).date(afterMinute(7)));
        entry(Execution.with.sell(1).price(15).date(afterMinute(9)));

        // past
        Profitable snapshot = snapshotAt(epochAfterMinute(1));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(12)).is(4);

        // exit first scenario partially
        snapshot = snapshotAt(epochAfterMinute(6));
        assert snapshot.realizedProfit().is(10);
        assert snapshot.unrealizedProfit(Num.of(18)).is(8);

        // entry second scenario
        snapshot = snapshotAt(epochAfterMinute(8));
        assert snapshot.realizedProfit().is(10);
        assert snapshot.unrealizedProfit(Num.of(19)).is(7);

        // entry third scenario
        snapshot = snapshotAt(epochAfterMinute(10));
        assert snapshot.realizedProfit().is(10);
        assert snapshot.unrealizedProfit(Num.of(18)).is(4);
    }
}
