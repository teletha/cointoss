/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import static cointoss.ticker.Span.*;

import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.google.common.base.Predicate;

import antibug.powerassert.PowerAssertOff;
import cointoss.execution.Execution;
import cointoss.util.Chrono;

/**
 * @version 2018/07/04 10:46:56
 */
class TickerManagerTest {

    ZonedDateTime Base = Chrono.MIN;

    TickerManager manager = new TickerManager();

    @Test
    void tickerBy() {
        Ticker ticker = manager.on(Minute1);
        assert ticker != null;
    }

    @Test
    void updateHighPrice() {
        // update
        manager.update(Execution.with.buy(1).price(100).date(Base));

        // validate
        manager.tickers().to(ticker -> {
            assert ticker.ticks.first().highPrice.is(100);
        });

        // update
        manager.update(Execution.with.buy(1).price(300).date(Base.plusMinutes(1)));
        manager.update(Execution.with.buy(1).price(200).date(Base.plusMinutes(2)));

        // validate
        manager.tickers().take(between(Minute5, Day7)).to(ticker -> {
            assert ticker.ticks.first().highPrice.is(300);
            assert ticker.ticks.last().highPrice.is(300);
        });
        manager.tickers().take(between(Second5, Minute1)).to(ticker -> {
            assert ticker.ticks.first().highPrice.is(100);
            assert ticker.ticks.last().highPrice.is(200);
        });

        // update and validate
        manager.update(Execution.with.buy(1).price(300).date(Base.plusMinutes(3)));
        assert manager.on(Minute5).ticks.last().highPrice.is(300);
        assert manager.on(Minute5).ticks.last().highPrice.is(300);
        // update and validate
        manager.update(Execution.with.buy(1).price(400).date(Base.plusMinutes(4)));
        assert manager.on(Minute5).ticks.last().highPrice.is(400);
        assert manager.on(Minute5).ticks.last().highPrice.is(400);
        // update and validate
        manager.update(Execution.with.buy(1).price(500).date(Base.plusMinutes(5)));
        assert manager.on(Minute5).ticks.last().highPrice.is(500);
        assert manager.on(Minute5).ticks.last().highPrice.is(500);
    }

    @Test
    void updateLowPrice() {
        // update
        manager.update(Execution.with.buy(1).price(300).date(Base));

        // validate
        manager.tickers().to(ticker -> {
            assert ticker.ticks.first().lowPrice.is(300);
        });

        // update
        manager.update(Execution.with.buy(1).price(100).date(Base.plusMinutes(1)));
        manager.update(Execution.with.buy(1).price(200).date(Base.plusMinutes(2)));

        // validate
        manager.tickers().take(between(Minute5, Day7)).to(ticker -> {
            assert ticker.ticks.first().lowPrice.is(100);
            assert ticker.ticks.last().lowPrice.is(100);
        });
        manager.tickers().take(between(Second5, Minute1)).to(ticker -> {
            assert ticker.ticks.first().lowPrice.is(300);
            assert ticker.ticks.last().lowPrice.is(200);
        });

        // update and validate
        manager.update(Execution.with.buy(1).price(30).date(Base.plusMinutes(3)));
        assert manager.on(Minute5).ticks.last().lowPrice.is(30);
        assert manager.on(Minute5).ticks.last().lowPrice.is(30);
        // update and validate
        manager.update(Execution.with.buy(1).price(20).date(Base.plusMinutes(4)));
        assert manager.on(Minute5).ticks.last().lowPrice.is(20);
        assert manager.on(Minute5).ticks.last().lowPrice.is(20);
        // update and validate
        manager.update(Execution.with.buy(1).price(10).date(Base.plusMinutes(5)));
        assert manager.on(Minute5).ticks.last().lowPrice.is(10);
        assert manager.on(Minute5).ticks.last().lowPrice.is(10);
    }

    @Test
    void updateOpenPrice() {
        // update
        manager.update(Execution.with.buy(1).price(300).date(Base));

        // validate
        manager.tickers().to(ticker -> {
            assert ticker.ticks.first().openPrice.is(300);
        });

        // update
        manager.update(Execution.with.buy(1).price(100).date(Base.plusMinutes(1)));
        manager.update(Execution.with.buy(1).price(200).date(Base.plusMinutes(2)));

        // validate
        manager.tickers().take(between(Minute5, Day7)).to(ticker -> {
            assert ticker.ticks.first().openPrice.is(300);
            assert ticker.ticks.last().openPrice.is(300);
        });
        manager.tickers().take(between(Second5, Minute1)).to(ticker -> {
            assert ticker.ticks.first().openPrice.is(300);
            assert ticker.ticks.last().openPrice.is(200);
        });
    }

    @Test
    void updateClosePrice() {
        // update
        manager.update(Execution.with.buy(1).price(300).date(Base));

        // validate
        manager.tickers().to(ticker -> {
            assert ticker.ticks.first().closePrice().is(300);
        });

        // update
        manager.update(Execution.with.buy(1).price(100).date(Base.plusMinutes(1)));
        manager.update(Execution.with.buy(1).price(200).date(Base.plusMinutes(2)));

        // validate
        manager.tickers().take(between(Minute5, Day7)).to(ticker -> {
            assert ticker.ticks.first().closePrice().is(200);
            assert ticker.ticks.last().closePrice().is(200);
        });
        manager.tickers().take(between(Second5, Minute1)).to(ticker -> {
            assert ticker.ticks.first().closePrice().is(300);
            assert ticker.ticks.last().closePrice().is(200);
        });
    }

    @Test
    @PowerAssertOff
    void updateLongVolume() {
        // update
        manager.update(Execution.with.buy(1).price(300).date(Base));

        // validate
        manager.tickers().to(ticker -> {
            assert ticker.ticks.first().buyVolume() == 1d;
        });

        // update
        manager.update(Execution.with.buy(1).price(100).date(Base.plusMinutes(1)));
        manager.update(Execution.with.buy(1).price(200).date(Base.plusMinutes(2)));

        // validate
        manager.tickers().take(between(Minute5, Day7)).to(ticker -> {
            assert ticker.ticks.first().buyVolume() == 3d;
            assert ticker.ticks.last().buyVolume() == 3d;
        });
        manager.tickers().take(between(Second5, Minute1)).to(ticker -> {
            assert ticker.ticks.first().buyVolume() == 1d;
            assert ticker.ticks.last().buyVolume() == 1d;
        });

        // update
        manager.update(Execution.with.buy(3).price(300).date(Base.plusMinutes(3)));

        // validate
        Ticker ticker = manager.on(Minute1);
        assert ticker.ticks.getByIndex(0).buyVolume() == 1d;
        assert ticker.ticks.getByIndex(1).buyVolume() == 1d;
        assert ticker.ticks.getByIndex(2).buyVolume() == 1d;
        assert ticker.ticks.getByIndex(3).buyVolume() == 3d;
    }

    @Test
    void updateShortVolume() {
        // update
        manager.update(Execution.with.sell(1).price(300).date(Base));

        // validate
        manager.tickers().to(ticker -> {
            assert ticker.ticks.first().sellVolume() == 1d;
        });

        // update
        manager.update(Execution.with.sell(1).price(100).date(Base.plusMinutes(1)));
        manager.update(Execution.with.sell(1).price(200).date(Base.plusMinutes(2)));

        // validate
        manager.tickers().take(between(Minute5, Day7)).to(ticker -> {
            assert ticker.ticks.first().sellVolume() == 3d;
            assert ticker.ticks.last().sellVolume() == 3d;
        });
        manager.tickers().take(between(Second5, Minute1)).to(ticker -> {
            assert ticker.ticks.first().sellVolume() == 1d;
            assert ticker.ticks.last().sellVolume() == 1d;
        });
    }

    private Predicate<Ticker> between(Span start, Span end) {
        return e -> {
            int ordinal = e.span.ordinal();
            return start.ordinal() <= ordinal && ordinal <= end.ordinal();
        };
    }

    @Test
    void complementGap() {
        manager.update(Execution.with.buy(1).price(10));
        manager.update(Execution.with.buy(1).price(30).date(Base.plusMinutes(5)));

        Ticker ticker = manager.on(Minute1);
        Tick tick = ticker.ticks.getByIndex(0);
        assert tick.openPrice().is(10);
        assert tick.closePrice().is(10);
        assert tick.highPrice().is(10);
        assert tick.lowPrice().is(10);

        tick = ticker.ticks.getByIndex(1);
        assert tick.openPrice().is(10);
        assert tick.closePrice().is(10);
        assert tick.highPrice().is(10);
        assert tick.lowPrice().is(10);

        tick = ticker.ticks.getByIndex(2);
        assert tick.openPrice().is(10);
        assert tick.closePrice().is(10);
        assert tick.highPrice().is(10);
        assert tick.lowPrice().is(10);

        tick = ticker.ticks.getByIndex(3);
        assert tick.openPrice().is(10);
        assert tick.closePrice().is(10);
        assert tick.highPrice().is(10);
        assert tick.lowPrice().is(10);

        tick = ticker.ticks.getByIndex(4);
        assert tick.openPrice().is(10);
        assert tick.closePrice().is(10);
        assert tick.highPrice().is(10);
        assert tick.lowPrice().is(10);

        tick = ticker.ticks.getByIndex(5);
        assert tick.openPrice().is(30);
        assert tick.closePrice().is(30);
        assert tick.highPrice().is(30);
        assert tick.lowPrice().is(30);
    }

    @Test
    void signalOpen() {
        manager.tickers().flatMap(t -> t.open).to();

        manager.update(Execution.with.buy(1).price(10));
        manager.tickers().to(ticker -> {
            assert ticker.ticks.size() == 1;
        });

        manager.update(Execution.with.buy(1).price(20).date(Base.plusSeconds(5)));
        assert manager.on(Second5).ticks.size() == 2;
        manager.tickers().take(between(Second15, Day7)).to(ticker -> {
            assert ticker.ticks.size() == 1;
        });

        manager.update(Execution.with.buy(1).price(30).date(Base.plusMinutes(1)));
        assert manager.on(Second5).ticks.size() == 13;
        assert manager.on(Second15).ticks.size() == 5;
        assert manager.on(Second30).ticks.size() == 3;
        assert manager.on(Minute1).ticks.size() == 2;
        manager.tickers().take(between(Minute5, Day7)).to(ticker -> {
            assert ticker.ticks.size() == 1;
        });

        manager.update(Execution.with.buy(1).price(40).date(Base.plusMinutes(5)));
        assert manager.on(Minute1).ticks.size() == 6;
        assert manager.on(Minute5).ticks.size() == 2;
        manager.tickers().take(between(Span.Minute10, Day7)).to(ticker -> {
            assert ticker.ticks.size() == 1;
        });

        manager.update(Execution.with.buy(1).price(50).date(Base.plusMinutes(7)));
        assert manager.on(Minute1).ticks.size() == 8;
        assert manager.on(Minute5).ticks.size() == 2;
        manager.tickers().take(between(Minute10, Day7)).to(ticker -> {
            assert ticker.ticks.size() == 1;
        });
    }

    @Test
    void signalOpenWithGap() {
        manager.tickers().flatMap(t -> t.open).to();

        manager.update(Execution.with.buy(1).price(10));
        manager.update(Execution.with.buy(1).price(30).date(Base.plusMinutes(5)));
        assert manager.on(Minute1).ticks.size() == 6;
        assert manager.on(Minute5).ticks.size() == 2;
        manager.tickers().take(between(Minute10, Day7)).to(ticker -> {
            assert ticker.ticks.size() == 1;
        });
    }

    @Test
    void signalUpdate() {
        AtomicInteger counter = new AtomicInteger();
        manager.tickers().flatMap(t -> t.update).to(counter::incrementAndGet);

        int size = Span.values().length;

        manager.update(Execution.with.buy(1).price(10));
        assert counter.get() == size;
        manager.update(Execution.with.buy(1).price(20));
        assert counter.get() == size * 2;
        manager.update(Execution.with.buy(1).price(30).date(Base.plusHours(1)));
        assert counter.get() == size * 3;
        manager.update(Execution.with.buy(1).price(40).date(Base.plusHours(6)));
        assert counter.get() == size * 4;
    }
}
