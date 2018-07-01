/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import static cointoss.MarketTestSupport.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.google.common.base.Predicate;

/**
 * @version 2018/06/30 9:04:39
 */
class TickerManagerTest {

    TickerManager manager = new TickerManager();

    @Test
    void tickerBy() {
        Ticker2 ticker = manager.tickerBy(TickSpan.Minute1);
        assert ticker != null;
    }

    @Test
    void updateHighPrice() {
        // update
        manager.update(buy(100, 1).date(BaseDate));

        // validate
        manager.tickers().to(ticker -> {
            assert ticker.first().highPrice.is(100);
        });

        // update
        manager.update(buy(300, 1).date(BaseDate.plusMinutes(1)));
        manager.update(buy(200, 1).date(BaseDate.plusMinutes(2)));

        // validate
        manager.tickers().take(between(TickSpan.Minute3, TickSpan.Day7)).to(ticker -> {
            assert ticker.first().highPrice.is(300);
            assert ticker.last().highPrice.is(300);
        });
        manager.tickers().take(between(TickSpan.Second5, TickSpan.Minute1)).to(ticker -> {
            assert ticker.first().highPrice.is(100);
            assert ticker.last().highPrice.is(200);
        });
    }

    @Test
    void updateLowPrice() {
        // update
        manager.update(buy(300, 1).date(BaseDate));

        // validate
        manager.tickers().to(ticker -> {
            assert ticker.first().lowPrice.is(300);
        });

        // update
        manager.update(buy(100, 1).date(BaseDate.plusMinutes(1)));
        manager.update(buy(200, 1).date(BaseDate.plusMinutes(2)));

        // validate
        manager.tickers().take(between(TickSpan.Minute3, TickSpan.Day7)).to(ticker -> {
            assert ticker.first().lowPrice.is(100);
            assert ticker.last().lowPrice.is(100);
        });
        manager.tickers().take(between(TickSpan.Second5, TickSpan.Minute1)).to(ticker -> {
            assert ticker.first().lowPrice.is(300);
            assert ticker.last().lowPrice.is(200);
        });
    }

    @Test
    void updateOpenPrice() {
        // update
        manager.update(buy(300, 1).date(BaseDate));

        // validate
        manager.tickers().to(ticker -> {
            assert ticker.first().openPrice.is(300);
        });

        // update
        manager.update(buy(100, 1).date(BaseDate.plusMinutes(1)));
        manager.update(buy(200, 1).date(BaseDate.plusMinutes(2)));

        // validate
        manager.tickers().take(between(TickSpan.Minute3, TickSpan.Day7)).to(ticker -> {
            assert ticker.first().openPrice.is(300);
            assert ticker.last().openPrice.is(300);
        });
        manager.tickers().take(between(TickSpan.Second5, TickSpan.Minute1)).to(ticker -> {
            assert ticker.first().openPrice.is(300);
            assert ticker.last().openPrice.is(200);
        });
    }

    @Test
    void updateClosePrice() {
        // update
        manager.update(buy(300, 1).date(BaseDate));

        // validate
        manager.tickers().to(ticker -> {
            assert ticker.first().closePrice().is(300);
        });

        // update
        manager.update(buy(100, 1).date(BaseDate.plusMinutes(1)));
        manager.update(buy(200, 1).date(BaseDate.plusMinutes(2)));

        // validate
        manager.tickers().take(between(TickSpan.Minute3, TickSpan.Day7)).to(ticker -> {
            assert ticker.first().closePrice().is(200);
            assert ticker.last().closePrice().is(200);
        });
        manager.tickers().take(between(TickSpan.Second5, TickSpan.Minute1)).to(ticker -> {
            assert ticker.first().closePrice().is(300);
            assert ticker.last().closePrice().is(200);
        });
    }

    @Test
    void updateLongVolume() {
        // update
        manager.update(buy(300, 1).date(BaseDate));

        // validate
        manager.tickers().to(ticker -> {
            assert ticker.first().longVolume().is(1);
        });

        // update
        manager.update(buy(100, 1).date(BaseDate.plusMinutes(1)));
        manager.update(buy(200, 1).date(BaseDate.plusMinutes(2)));

        // validate
        manager.tickers().take(between(TickSpan.Minute3, TickSpan.Day7)).to(ticker -> {
            assert ticker.first().longVolume().is(3);
            assert ticker.last().longVolume().is(3);
        });
        manager.tickers().take(between(TickSpan.Second5, TickSpan.Minute1)).to(ticker -> {
            assert ticker.first().longVolume().is(1);
            assert ticker.last().longVolume().is(1);
        });
    }

    @Test
    void updateShortVolume() {
        // update
        manager.update(sell(300, 1).date(BaseDate));

        // validate
        manager.tickers().to(ticker -> {
            assert ticker.first().shortVolume().is(1);
        });

        // update
        manager.update(sell(100, 1).date(BaseDate.plusMinutes(1)));
        manager.update(sell(200, 1).date(BaseDate.plusMinutes(2)));

        // validate
        manager.tickers().take(between(TickSpan.Minute3, TickSpan.Day7)).to(ticker -> {
            assert ticker.first().shortVolume().is(3);
            assert ticker.last().shortVolume().is(3);
        });
        manager.tickers().take(between(TickSpan.Second5, TickSpan.Minute1)).to(ticker -> {
            assert ticker.first().shortVolume().is(1);
            assert ticker.last().shortVolume().is(1);
        });
    }

    private Predicate<Ticker2> between(TickSpan start, TickSpan end) {
        return e -> {
            int ordinal = e.span.ordinal();
            return start.ordinal() <= ordinal && ordinal <= end.ordinal();
        };
    }

    @Test
    void signalUpdate() {
        AtomicInteger counter = new AtomicInteger();
        manager.tickers().flatMap(t -> t.update).to(counter::incrementAndGet);

        int size = TickSpan.values().length;

        manager.update(buy(10, 1));
        assert counter.get() == size;
        manager.update(buy(20, 1));
        assert counter.get() == size * 2;
        manager.update(buy(30, 1).date(BaseDate.plusHours(1)));
        assert counter.get() == size * 3;
        manager.update(buy(40, 1).date(BaseDate.plusHours(6)));
        assert counter.get() == size * 4;
    }
}
