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
import cointoss.trade.extension.PricePart;
import cointoss.trade.extension.ScenePart;
import cointoss.trade.extension.SidePart;
import cointoss.trade.extension.SizePart;
import cointoss.trade.extension.TradeTest;
import cointoss.util.Num;

class TraderSnapshotTest extends TraderTestSupport {

    @TradeTest
    void snapshotCompleteEntryAndCompleteExit(SidePart side, PricePart price) {
        entryAndExit(Execution.with.direction(side, 1)
                .price(price.entry), Execution.with.direction(side.inverse(), 1).price(price.exit).date(afterMinute(5)));

        // past
        for (int i = 1; i < 4; i++) {
            Profitable snapshot = snapshotAt(epochAfterMinute(i));
            assert snapshot.realizedProfit().is(0);
            assert snapshot.unrealizedProfit(price.middleN).is(1 * price.diffHalf * side.sign);
        }

        // future
        for (int i = 5; i < 10; i++) {
            Profitable snapshot = snapshotAt(epochAfterMinute(i));
            assert snapshot.realizedProfit().is(1 * price.diff * side.sign);
            assert snapshot.unrealizedProfit(price.profitN).is(0);
        }
    }

    @TradeTest
    void snapshotCompleteEntryAndIncompleteExit(SidePart side, PricePart price) {
        entryAndExit(Execution.with.direction(side, 2)
                .price(price.entry), Execution.with.direction(side.inverse(), 1).price(price.exit).date(afterMinute(5)));

        // past
        for (int i = 1; i < 4; i++) {
            Profitable snapshot = snapshotAt(epochAfterMinute(i));
            assert snapshot.realizedProfit().is(0);
            assert snapshot.unrealizedProfit(price.middleN).is(2 * price.diffHalf * side.sign);
        }

        // future
        for (int i = 5; i < 10; i++) {
            Profitable snapshot = snapshotAt(epochAfterMinute(i));
            assert snapshot.realizedProfit().is(1 * price.diff * side.sign);
            assert snapshot.unrealizedProfit(price.middleN).is(1 * price.diffHalf * side.sign);
        }
    }

    @TradeTest
    void snapshotCompleteEntryAndPartialExit(SidePart side, PricePart price) {
        entryAndExitPartial(Execution.with.direction(side, 2)
                .price(price.entry), Execution.with.direction(side.inverse(), 2).price(price.exit).date(afterMinute(5)), 1);

        // past
        for (int i = 1; i < 4; i++) {
            Profitable snapshot = snapshotAt(epochAfterMinute(i));
            assert snapshot.realizedProfit().is(0);
            assert snapshot.unrealizedProfit(price.middleN).is(2 * price.diffHalf * side.sign);
        }

        // future
        for (int i = 5; i < 10; i++) {
            Profitable snapshot = snapshotAt(epochAfterMinute(i));
            assert snapshot.realizedProfit().is(1 * price.diff * side.sign);
            assert snapshot.unrealizedProfit(price.middleN).is(1 * price.diffHalf * side.sign);
        }
    }

    @TradeTest
    void snapshotCompleteEntryAndNoExit(SidePart side) {
        entry(Execution.with.direction(side, 1).price(10));

        // past
        Profitable snapshot = snapshotAt(epochAfterMinute(1));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(12)).is(2 * side.sign);

        // past
        snapshot = snapshotAt(epochAfterMinute(2));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(6)).is(-4 * side.sign);

        // past
        snapshot = snapshotAt(epochAfterMinute(4));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(15)).is(5 * side.sign);

        // future
        snapshot = snapshotAt(epochAfterMinute(6));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(25)).is(15 * side.sign);

        // future
        snapshot = snapshotAt(epochAfterMinute(10));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(5)).is(-5 * side.sign);
    }

    @TradeTest
    void snapshotPartialEntryAndCompleteExit(SidePart side) {
        entryPartialAndExit(Execution.with.direction(side, 2)
                .price(10), 1, Execution.with.direction(side.inverse(), 1).price(20).date(afterMinute(5)));

        // past
        Profitable snapshot = snapshotAt(epochAfterMinute(1));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(12)).is(2 * side.sign);

        // past
        snapshot = snapshotAt(epochAfterMinute(2));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(6)).is(-4 * side.sign);

        // past
        snapshot = snapshotAt(epochAfterMinute(4));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(15)).is(5 * side.sign);

        // future
        snapshot = snapshotAt(epochAfterMinute(6));
        assert snapshot.realizedProfit().is(10 * side.sign);
        assert snapshot.unrealizedProfit(Num.of(25)).is(0);

        // future
        snapshot = snapshotAt(epochAfterMinute(10));
        assert snapshot.realizedProfit().is(10 * side.sign);
        assert snapshot.unrealizedProfit(Num.of(5)).is(0);
    }

    @TradeTest
    void realizedProfit(ScenePart scene, SidePart side, SizePart size, PricePart price) {
        scenario(scene, side, size, price);
        Snapshot snapshot = snapshotAt(epochAfterMinute(1));

        switch (scene) {
        case ExitCompletely:
        case ExitMultiple:
        case ExitSeparately:
        case ExitCanceledThenOtherExitCompletely:
            assert snapshot.realizedProfit().is(price.diff * size.num * side.sign);
            break;

        case ExitPartially:
        case ExitPartiallyCancelled:
        case EntryPartiallyCanceledAndExitCompletely:
            assert snapshot.realizedProfit().is(price.diff * size.half * side.sign);
            break;

        default:
            assert snapshot.realizedProfit().is(0);
            break;
        }
    }

    @TradeTest
    void unrealizedProfit(ScenePart scene, SidePart side, SizePart size, PricePart price) {
        scenario(scene, side, size, price);
        Snapshot snapshot = snapshotAt(epochAfterMinute(1));

        switch (scene) {
        case Entry:
        case EntryCanceled:
        case EntryPartiallyCanceledAndExitCompletely:
        case ExitCompletely:
        case ExitMultiple:
        case ExitSeparately:
        case ExitCanceledThenOtherExitCompletely:
            assert snapshot.unrealizedProfit(price.profitN).is(0);
            break;

        case EntryPartially:
        case EntryPartiallyCanceled:
        case ExitPartially:
        case ExitPartiallyCancelled:
            assert snapshot.unrealizedProfit(price.exitN).is(price.diff * size.half * side.sign);
            break;

        default:
            assert snapshot.unrealizedProfit(price.exitN).is(price.diff * size.num * side.sign);
            break;
        }
    }

    @TradeTest
    void snapshotPartialEntryAndIncompleteExit(SidePart side) {
        entryPartialAndExit(Execution.with.direction(side, 3)
                .price(10), 2, Execution.with.direction(side.inverse(), 1).price(20).date(afterMinute(5)));

        // past
        Profitable snapshot = snapshotAt(epochAfterMinute(1));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(12)).is(4 * side.sign);

        // past
        snapshot = snapshotAt(epochAfterMinute(2));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(6)).is(-8 * side.sign);

        // past
        snapshot = snapshotAt(epochAfterMinute(4));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(15)).is(10 * side.sign);

        // future
        snapshot = snapshotAt(epochAfterMinute(6));
        assert snapshot.realizedProfit().is(10 * side.sign);
        assert snapshot.unrealizedProfit(Num.of(25)).is(15 * side.sign);

        // future
        snapshot = snapshotAt(epochAfterMinute(10));
        assert snapshot.realizedProfit().is(10 * side.sign);
        assert snapshot.unrealizedProfit(Num.of(5)).is(-5 * side.sign);
    }

    @Test
    void snapshotPartialEntryAndPartialExit() {
        entryPartialAndExitPartial(Execution.with.buy(2).price(10), 1, Execution.with.sell(2).price(20).date(afterMinute(5)), 1);

        // past
        Profitable snapshot = snapshotAt(epochAfterMinute(1));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(12)).is(2);

        // past
        snapshot = snapshotAt(epochAfterMinute(2));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(6)).is(-4);

        // past
        snapshot = snapshotAt(epochAfterMinute(4));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(15)).is(5);

        // future
        snapshot = snapshotAt(epochAfterMinute(6));
        assert snapshot.realizedProfit().is(10);
        assert snapshot.unrealizedProfit(Num.of(25)).is(0);

        // future
        snapshot = snapshotAt(epochAfterMinute(10));
        assert snapshot.realizedProfit().is(10);
        assert snapshot.unrealizedProfit(Num.of(5)).is(0);
    }

    @Test
    void snapshotPartialEntryAndNoExit() {
        entryPartial(Execution.with.buy(2).price(10), 1);

        // past
        Profitable snapshot = snapshotAt(epochAfterMinute(1));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(12)).is(2);

        // past
        snapshot = snapshotAt(epochAfterMinute(2));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(6)).is(-4);

        // past
        snapshot = snapshotAt(epochAfterMinute(4));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(15)).is(5);

        // future
        snapshot = snapshotAt(epochAfterMinute(6));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(25)).is(15);

        // future
        snapshot = snapshotAt(epochAfterMinute(10));
        assert snapshot.realizedProfit().is(0);
        assert snapshot.unrealizedProfit(Num.of(5)).is(-5);
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
