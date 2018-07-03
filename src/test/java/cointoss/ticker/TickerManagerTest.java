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
import static cointoss.ticker.TickSpan.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.google.common.base.Predicate;

import antibug.powerassert.PowerAssertOff;

/**
 * @version 2018/06/30 9:04:39
 */
class TickerManagerTest {

    TickerManager manager = new TickerManager();

    @Test
    void tickerBy() {
        Ticker ticker = manager.tickerBy(Minute1);
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
        manager.tickers().take(between(Minute3, Day7)).to(ticker -> {
            assert ticker.first().highPrice.is(300);
            assert ticker.last().highPrice.is(300);
        });
        manager.tickers().take(between(Second5, Minute1)).to(ticker -> {
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
        manager.tickers().take(between(Minute3, Day7)).to(ticker -> {
            assert ticker.first().lowPrice.is(100);
            assert ticker.last().lowPrice.is(100);
        });
        manager.tickers().take(between(Second5, Minute1)).to(ticker -> {
            assert ticker.first().lowPrice.is(300);
            assert ticker.last().lowPrice.is(200);
        });
    }

    @Test
    @PowerAssertOff
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
        manager.tickers().take(between(Minute3, Day7)).to(ticker -> {
            assert ticker.first().openPrice.is(300);
            assert ticker.last().openPrice.is(300);
        });
        manager.tickers().take(between(Second5, Minute1)).to(ticker -> {
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
        manager.tickers().take(between(Minute3, Day7)).to(ticker -> {
            assert ticker.first().closePrice().is(200);
            assert ticker.last().closePrice().is(200);
        });
        manager.tickers().take(between(Second5, Minute1)).to(ticker -> {
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
        manager.tickers().take(between(Minute3, Day7)).to(ticker -> {
            assert ticker.first().longVolume().is(3);
            assert ticker.last().longVolume().is(3);
        });
        manager.tickers().take(between(Second5, Minute1)).to(ticker -> {
            assert ticker.first().longVolume().is(1);
            assert ticker.last().longVolume().is(1);
        });

        // update
        manager.update(buy(300, 3).date(BaseDate.plusMinutes(3)));

        // validate
        Ticker ticker = manager.tickerBy(Minute1);
        assert ticker.ticks.get(0).longVolume().is(1);
        assert ticker.ticks.get(1).longVolume().is(1);
        assert ticker.ticks.get(2).longVolume().is(1);
        assert ticker.ticks.get(3).longVolume().is(3);
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
        manager.tickers().take(between(Minute3, Day7)).to(ticker -> {
            assert ticker.first().shortVolume().is(3);
            assert ticker.last().shortVolume().is(3);
        });
        manager.tickers().take(between(Second5, Minute1)).to(ticker -> {
            assert ticker.first().shortVolume().is(1);
            assert ticker.last().shortVolume().is(1);
        });
    }

    private Predicate<Ticker> between(TickSpan start, TickSpan end) {
        return e -> {
            int ordinal = e.span.ordinal();
            return start.ordinal() <= ordinal && ordinal <= end.ordinal();
        };
    }

    @Test
    void complementGap() {
        manager.update(buy(10, 1));
        manager.update(buy(30, 1).date(BaseDate.plusMinutes(5)));

        Ticker ticker = manager.tickerBy(Minute1);
        Tick tick = ticker.ticks.get(0);
        assert tick.openPrice().is(10);
        assert tick.closePrice().is(10);
        assert tick.highPrice().is(10);
        assert tick.lowPrice().is(10);

        tick = ticker.ticks.get(1);
        assert tick.openPrice().is(10);
        assert tick.closePrice().is(10);
        assert tick.highPrice().is(10);
        assert tick.lowPrice().is(10);

        tick = ticker.ticks.get(2);
        assert tick.openPrice().is(10);
        assert tick.closePrice().is(10);
        assert tick.highPrice().is(10);
        assert tick.lowPrice().is(10);

        tick = ticker.ticks.get(3);
        assert tick.openPrice().is(10);
        assert tick.closePrice().is(10);
        assert tick.highPrice().is(10);
        assert tick.lowPrice().is(10);

        tick = ticker.ticks.get(4);
        assert tick.openPrice().is(10);
        assert tick.closePrice().is(10);
        assert tick.highPrice().is(10);
        assert tick.lowPrice().is(10);

        tick = ticker.ticks.get(5);
        assert tick.openPrice().is(30);
        assert tick.closePrice().is(30);
        assert tick.highPrice().is(30);
        assert tick.lowPrice().is(30);
    }

    @Test
    void signalAdd() {
        manager.tickers().flatMap(t -> t.add).to();

        manager.update(buy(10, 1));
        manager.tickers().to(ticker -> {
            assert ticker.size() == 1;
        });

        manager.update(buy(20, 1).date(BaseDate.plusSeconds(5)));
        assert manager.tickerBy(Second5).size() == 2;
        manager.tickers().take(between(Second15, Day7)).to(ticker -> {
            assert ticker.size() == 1;
        });

        manager.update(buy(30, 1).date(BaseDate.plusMinutes(1)));
        assert manager.tickerBy(Second5).size() == 13;
        assert manager.tickerBy(Second15).size() == 5;
        assert manager.tickerBy(Second30).size() == 3;
        assert manager.tickerBy(Minute1).size() == 2;
        manager.tickers().take(between(Minute3, Day7)).to(ticker -> {
            assert ticker.size() == 1;
        });

        manager.update(buy(40, 1).date(BaseDate.plusMinutes(3)));
        assert manager.tickerBy(Minute1).size() == 4;
        assert manager.tickerBy(Minute3).size() == 2;
        manager.tickers().take(between(Minute5, Day7)).to(ticker -> {
            assert ticker.size() == 1;
        });

        manager.update(buy(50, 1).date(BaseDate.plusMinutes(5)));
        assert manager.tickerBy(Minute1).size() == 6;
        assert manager.tickerBy(Minute3).size() == 2;
        assert manager.tickerBy(Minute5).size() == 2;
        manager.tickers().take(between(Minute10, Day7)).to(ticker -> {
            assert ticker.size() == 1;
        });
    }

    @Test
    void signalAddWithGap() {
        manager.tickers().flatMap(t -> t.add).to();

        manager.update(buy(10, 1));
        manager.update(buy(30, 1).date(BaseDate.plusMinutes(5)));
        assert manager.tickerBy(Minute1).size() == 6;
        assert manager.tickerBy(Minute3).size() == 2;
        assert manager.tickerBy(Minute5).size() == 2;
        manager.tickers().take(between(Minute10, Day7)).to(ticker -> {
            assert ticker.size() == 1;
        });
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
