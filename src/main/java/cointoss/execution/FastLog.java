/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.execution;

import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
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

    private NavigableMap<Num, Num> buying;

    private double sells;

    private NavigableMap<Num, Num> selling;

    /**
     * @param scale
     * @param initial
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
        buying = new ConcurrentSkipListMap();
        selling = new ConcurrentSkipListMap();
        latestId = e.id;
        if (e.isBuy()) {
            buys = e.size.doubleValue();
            buying.put(e.price, e.size);
            sells = 0;
        } else {
            sells = e.size.doubleValue();
            selling.put(e.price, e.size);
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
            buying.compute(e.price, (k, o) -> o == null ? e.size : o.plus(e.size));
        } else {
            sells += e.size.doubleValue();
            selling.compute(e.price, (k, o) -> o == null ? e.size : o.plus(e.size));
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

                System.out.println(buying.size() + "  " + selling.size());
                Num[] buyV = bull ? volumes(buying, highest, open, true) : volumes(buying, highest, close, true);
                Num[] sellV = bull ? volumes(selling, close, lowest, false) : volumes(selling, open, lowest, false);
                Num[] sizes = bull ? new Num[] {buyV[1], sellV[1], buyV[0], sellV[0]} : new Num[] {sellV[0], buyV[0], sellV[1], buyV[1]};
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

    private Num[] volumes(NavigableMap<Num, Num> volumes, double upper, double lower, boolean buy) {
        Num middle = Num.of((upper + lower) / 2);
        double head = volumes.headMap(middle, buy).values().stream().mapToDouble(Num::doubleValue).sum();
        double tail = volumes.tailMap(middle, !buy).values().stream().mapToDouble(Num::doubleValue).sum();

        return Num.of(tail, head);
    }

    private Num[] volumes2(NavigableMap<Num, Num> volumes, double upper, double lower, boolean buy) {
        double middle = volumes.values().stream().mapToDouble(Num::doubleValue).sum();

        return Num.of(middle / 2d, middle / 2);
    }
}