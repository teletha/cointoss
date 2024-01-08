/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.execution;

import java.util.function.Function;

import cointoss.Direction;
import cointoss.util.Chrono;
import cointoss.util.arithmetic.Num;
import kiss.I;
import kiss.Signal;

class FastLog implements Function<Signal<Execution>, Signal<Execution>> {

    private final int scale;

    private long start = -1;

    private long end;

    private long latestId;

    private double open;

    private double close;

    private double highest;

    private double lowest;

    private double buys;

    private double sells;

    /**
     * @param scale
     */
    FastLog(int scale) {
        this.scale = scale;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> apply(Signal<Execution> signal) {
        return signal.flatMap(e -> {
            if (start == -1) {
                return createTick(e);
            } else if (e.mills < end) {
                return updateTick(e);
            } else {
                return completeTick(e);
            }
        }).concat(completeTick(null));
    }

    private Signal<Execution> createTick(Execution e) {
        start = e.mills - e.mills % 5000;
        end = start + 5000;
        open = highest = lowest = close = e.price.doubleValue();
        latestId = e.id;
        if (e.isBuy()) {
            buys = e.size.doubleValue();
            sells = 0;
        } else {
            sells = e.size.doubleValue();
            buys = 0;
        }

        return I.signal();
    }

    private Signal<Execution> updateTick(Execution e) {
        double price = e.price.doubleValue();
        if (highest < price) {
            highest = price;
        } else if (price < lowest) {
            lowest = price;
        }
        close = price;
        latestId = e.id;

        if (e.isBuy()) {
            buys += e.size.doubleValue();
        } else {
            sells += e.size.doubleValue();
        }

        return I.signal();
    }

    private Signal<Execution> completeTick(Execution next) {
        return new Signal<>((observer, disposer) -> {
            try {
                Num buy = Num.of(buys).scale(scale).divide(2);
                Num sell = Num.of(sells).scale(scale).divide(2);
                Direction buySide = Direction.BUY;
                Direction sellSide = Direction.SELL;

                if (buy.isZero()) {
                    if (sell.isZero()) {
                        return disposer;
                    } else if (open == close && open == highest && open == lowest) {
                        observer.accept(Execution.with.sell(sells).price(open).id(latestId).date(Chrono.utcByMills(start)));
                        return disposer;
                    }

                    buy = sell = sell.divide(2);
                    buySide = sellSide;
                } else if (sell.isZero()) {
                    if (open == close && open == highest && open == lowest) {
                        observer.accept(Execution.with.buy(buys).price(open).id(latestId).date(Chrono.utcByMills(start)));
                        return disposer;
                    }

                    buy = sell = buy.divide(2);
                    sellSide = buySide;
                }

                boolean bull = open <= close;

                Direction[] sides = bull ? new Direction[] {buySide, sellSide, buySide, sellSide}
                        : new Direction[] {sellSide, buySide, sellSide, buySide};

                Num[] sizes = bull ? new Num[] {buy, sell, buy, sell} : new Num[] {sell, buy, sell, buy};
                double[] prices = bull ? new double[] {open, lowest, highest, close} : new double[] {open, highest, lowest, close};

                for (int i = 0; i < prices.length; i++) {
                    observer.accept(Execution.with.direction(sides[i], sizes[i])
                            .price(prices[i])
                            .id(latestId - 3 + i)
                            .date(Chrono.utcByMills(start + 1000 * i)));
                }

                return disposer;
            } finally {
                observer.complete();

                if (next != null) {
                    createTick(next);
                } else {
                    start = -1;
                }
            }
        });
    }
}