/*
 * Copyright (C) 2021 cointoss Development Team
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

import org.junit.jupiter.api.Test;

import com.google.common.base.Predicate;

import antibug.powerassert.PowerAssertOff;
import cointoss.execution.Execution;
import cointoss.util.Chrono;

class TickerManagerTest {

    ZonedDateTime Base = Chrono.MIN;

    long BaseSec = Base.toEpochSecond();

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
        manager.tickers().take(ticker -> ticker.span == Minute1).to(ticker -> {
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
        manager.tickers().take(ticker -> ticker.span == Minute1).to(ticker -> {
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
        manager.tickers().take(ticker -> ticker.span == Minute1).to(ticker -> {
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
        manager.tickers().take(ticker -> ticker.span == Minute1).to(ticker -> {
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
            assert ticker.ticks.first().longVolume() == 1d;
        });

        // update
        manager.update(Execution.with.buy(1).price(100).date(Base.plusMinutes(1)));
        manager.update(Execution.with.buy(1).price(200).date(Base.plusMinutes(2)));

        // validate
        manager.tickers().take(between(Minute5, Day7)).to(ticker -> {
            assert ticker.ticks.first().longVolume() == 3d;
            assert ticker.ticks.last().longVolume() == 3d;
        });
        manager.tickers().take(ticker -> ticker.span == Minute1).to(ticker -> {
            assert ticker.ticks.first().longVolume() == 1d;
            assert ticker.ticks.last().longVolume() == 1d;
        });

        // update
        manager.update(Execution.with.buy(3).price(300).date(Base.plusMinutes(3)));

        // validate
        Ticker ticker = manager.on(Minute1);
        assert ticker.ticks.at(BaseSec + 0 * 60).longVolume() == 1d;
        assert ticker.ticks.at(BaseSec + 1 * 60).longVolume() == 1d;
        assert ticker.ticks.at(BaseSec + 2 * 60).longVolume() == 1d;
        assert ticker.ticks.at(BaseSec + 3 * 60).longVolume() == 3d;
    }

    @Test
    void updateShortVolume() {
        // update
        manager.update(Execution.with.sell(1).price(300).date(Base));

        // validate
        manager.tickers().to(ticker -> {
            assert ticker.ticks.first().shortVolume() == 1d;
        });

        // update
        manager.update(Execution.with.sell(1).price(100).date(Base.plusMinutes(1)));
        manager.update(Execution.with.sell(1).price(200).date(Base.plusMinutes(2)));

        // validate
        manager.tickers().take(between(Minute5, Day7)).to(ticker -> {
            assert ticker.ticks.first().shortVolume() == 3d;
            assert ticker.ticks.last().shortVolume() == 3d;
        });
        manager.tickers().take(ticker -> ticker.span == Minute1).to(ticker -> {
            assert ticker.ticks.first().shortVolume() == 1d;
            assert ticker.ticks.last().shortVolume() == 1d;
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
        Tick tick = ticker.ticks.at(BaseSec);
        assert tick.openPrice().is(10);
        assert tick.closePrice().is(10);
        assert tick.highPrice().is(10);
        assert tick.lowPrice().is(10);

        tick = ticker.ticks.at(BaseSec + 60);
        assert tick.openPrice().is(10);
        assert tick.closePrice().is(10);
        assert tick.highPrice().is(10);
        assert tick.lowPrice().is(10);

        tick = ticker.ticks.at(BaseSec + 2 * 60);
        assert tick.openPrice().is(10);
        assert tick.closePrice().is(10);
        assert tick.highPrice().is(10);
        assert tick.lowPrice().is(10);

        tick = ticker.ticks.at(BaseSec + 3 * 60);
        assert tick.openPrice().is(10);
        assert tick.closePrice().is(10);
        assert tick.highPrice().is(10);
        assert tick.lowPrice().is(10);

        tick = ticker.ticks.at(BaseSec + 4 * 60);
        assert tick.openPrice().is(10);
        assert tick.closePrice().is(10);
        assert tick.highPrice().is(10);
        assert tick.lowPrice().is(10);

        tick = ticker.ticks.at(BaseSec + 5 * 60);
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
        manager.tickers().take(between(Minute1, Day7)).to(ticker -> {
            assert ticker.ticks.size() == 1;
        });

        manager.update(Execution.with.buy(1).price(30).date(Base.plusMinutes(1)));
        assert manager.on(Minute1).ticks.size() == 2;
        manager.tickers().take(between(Minute5, Day7)).to(ticker -> {
            assert ticker.ticks.size() == 1;
        });

        manager.update(Execution.with.buy(1).price(40).date(Base.plusMinutes(5)));
        assert manager.on(Minute1).ticks.size() == 6;
        assert manager.on(Minute5).ticks.size() == 2;
        manager.tickers().take(between(Span.Minute15, Day7)).to(ticker -> {
            assert ticker.ticks.size() == 1;
        });

        manager.update(Execution.with.buy(1).price(50).date(Base.plusMinutes(7)));
        assert manager.on(Minute1).ticks.size() == 8;
        assert manager.on(Minute5).ticks.size() == 2;
        manager.tickers().take(between(Minute15, Day7)).to(ticker -> {
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
        manager.tickers().take(between(Minute15, Day7)).to(ticker -> {
            assert ticker.ticks.size() == 1;
        });
    }
}