/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

import java.math.RoundingMode;
import java.util.Collection;
import java.util.Objects;
import java.util.function.UnaryOperator;

import cointoss.Direction;
import cointoss.MarketSetting;
import cointoss.order.OrderBookChanges.Listener;
import cointoss.util.arithmetic.Num;
import cointoss.util.arithmetic.Primitives;
import cointoss.util.map.ConcurrentNavigableDoubleMap;
import cointoss.util.map.DoubleMap;
import cointoss.util.map.DoubleMap.DoubleEntry;
import kiss.Signal;
import kiss.Signaling;
import kiss.Variable;

public class OrderBook implements Listener {

    /** The best order. */
    public final Variable<OrderBookPage> best = Variable.empty();

    /** The search direction. */
    private final Direction side;

    /** The scale for base currency. */
    private final int scaleBase;

    /** The scale for target currency. */
    private final int scaleTarget;

    /** The taker fee calculator. */
    private final UnaryOperator<Num> takerFee;

    /** The base boards. */
    private final ConcurrentNavigableDoubleMap<OrderBookPage> base;

    /** The initial price range. */
    private final float initialRange;

    /** The price range for group. */
    private float range;

    /** The grouped boards. */
    private ConcurrentNavigableDoubleMap<OrderBookPage> grouped;

    private final Signaling<Object> updating = new Signaling();

    public final Signal<Object> update = updating.expose;

    /**
     * Build {@link OrderBook}.
     * 
     * @param setting A market setting.
     * @param side Order direction.
     */
    OrderBook(MarketSetting setting, Direction side) {
        this.side = Objects.requireNonNull(side);
        this.base = side.isBuy() ? DoubleMap.createReversedMap() : DoubleMap.createSortedMap();
        this.scaleBase = setting.base.scale;
        this.scaleTarget = setting.target.scale;
        this.takerFee = setting.takerFee;
        this.initialRange = this.range = setting.base.minimumSize.floatValue();
    }

    // /**
    // * Set book operation thread.
    // *
    // * @param operator
    // */
    // public final void operateOn(Consumer<Runnable> operator) {
    // if (operator != null) {
    // this.operator = operator;
    // }
    // }
    //
    // /**
    // * Replace the order book management container with your container.
    // *
    // * @param replacer A list replacer.
    // * @return
    // */
    // public final void replaceBy(UnaryOperator<List<OrderBookPage>> replacer) {
    // if (replacer != null) {
    // this.replacer = replacer;
    //
    // group.pages = replacer.apply(group.pages);
    // }
    // }

    public final Num bestSize() {
        return Num.of(Primitives.roundDecimal(best.v.size, scaleTarget));
    }

    /**
     * Get the grouped view of this {@link OrderBook}.
     * 
     * @param range The price range.
     * @return A grouped view.
     */
    public final Collection<OrderBookPage> groupBy(float range) {
        if (range <= initialRange) {
            grouped = null;
        } else if (this.range != range) {
            grouping(range);
        }
        return grouped().values();
    }

    private ConcurrentNavigableDoubleMap grouped() {
        return grouped == null ? base : grouped;
    }

    /**
     * Build {@link GroupedOrderBook}.
     * 
     * @param range A price range to group.
     */
    private void grouping(float range) {
        this.range = range;
        this.grouped = side.isBuy() ? DoubleMap.createReversedMap() : DoubleMap.createSortedMap();

        // grouping the current boards
        for (OrderBookPage board : base.values()) {
            updateGroup(board.price, board.size);
        }
    }

    /**
     * Update price and size.
     * 
     * @param price
     * @param size
     */
    private void updateGroup(double price, double size) {
        double p = floor(price, range, scaleBase);

        OrderBookPage page = grouped.computeIfAbsent(p, key -> new OrderBookPage(key, 0, side.isBuy() ? 0 : range));
        page.size += size;

        if (Primitives.roundDecimal(page.size, scaleTarget, RoundingMode.DOWN) <= 0) {
            grouped.remove(p);
        }
    }

    /**
     * Fix error price.
     * 
     * @param hint A price hint.
     */
    private void fixGroup(double hint) {
        if (!grouped.isEmpty()) {
            double h = floor(hint, range, scaleBase);

            DoubleEntry<OrderBookPage> entry = grouped.firstEntry();
            if (side.isBuy()) {
                while (entry != null && entry.getDoubleKey() > h) {
                    grouped.pollFirstEntry();
                    entry = grouped.firstEntry();
                }
            } else {
                while (entry != null && entry.getDoubleKey() < h) {
                    grouped.pollFirstEntry();
                    entry = grouped.firstEntry();
                }
            }
        }
    }

    /**
     * It finds the largest order in the currently selected OrderBook within the specified price
     * range.
     * 
     * @param lowerPrice
     * @param upperPrice
     * @return
     */
    public final OrderBookPage findLargestOrder(Num lowerPrice, Num upperPrice) {
        OrderBookPage max = new OrderBookPage(lowerPrice.doubleValue(), 0, range);

        if (base.isEmpty()) {
            return max;
        }

        ConcurrentNavigableDoubleMap<OrderBookPage> grouped = grouped();

        if (side.isBuy()) {
            if (lowerPrice.isGreaterThan(base.firstKey()) || upperPrice.isLessThan(base.lastKey())) {
                return max;
            }

            Num lowerRounded = Num.max(Num.of(base.lastDoubleKey()), lowerPrice).floor(range);
            Num upperRounded = Num.min(Num.of(base.firstDoubleKey()), upperPrice).floor(range);

            for (OrderBookPage page : grouped.subMap(upperRounded.doubleValue(), true, lowerRounded.doubleValue(), true).values()) {
                if (max.size < page.size) {
                    max = page;
                }
            }
        } else {
            if (upperPrice.isLessThan(base.firstKey()) || lowerPrice.isGreaterThan(base.lastKey())) {
                return max;
            }

            Num lowerRounded = Num.max(Num.of(base.firstDoubleKey()), lowerPrice).floor(range);
            Num upperRounded = Num.min(Num.of(base.lastDoubleKey()), upperPrice).floor(range);

            for (OrderBookPage page : grouped.subMap(lowerRounded.doubleValue(), true, upperRounded.doubleValue(), true).values()) {
                if (max.size < page.size) {
                    max = page;
                }
            }
        }
        return max;
    }

    /**
     * Iterate all pages of the current selected grouped view by ascending order.
     * 
     * @return
     */
    public final Iterable<OrderBookPage> ascendingPages() {
        return grouped().values();
    }

    public final ConcurrentNavigableDoubleMap<OrderBookPage> headMap(Num price) {
        return grouped().headMap(price.doubleValue(), true);
    }

    public final ConcurrentNavigableDoubleMap<OrderBookPage> tailMap(Num price) {
        return grouped().tailMap(price.doubleValue(), true);
    }

    /**
     * Predict the taking price.
     * 
     * @param size A taking size.
     * @return A predicted price.
     */
    public final Num predictTakingPrice(long size) {
        return predictTakingPrice((double) size);
    }

    /**
     * Predict the taking price.
     * 
     * @param size A taking size.
     * @return A predicted price.
     */
    public final Num predictTakingPrice(double size) {
        if (size == 0) {
            return Num.ZERO;
        }
        double total = 0;
        double remaining = size;

        for (OrderBookPage page : base.values()) {
            double decrease = Math.min(page.size, remaining);
            total += decrease * page.price;
            remaining -= decrease;

            if (remaining <= 0) {
                break;
            }
        }
        return Num.of(total / size).scale(scaleBase);
    }

    /**
     * Predict the taking price.
     * 
     * @param size A taking size.
     * @return A predicted price.
     */
    public final Num predictTakingPrice(String size) {
        return predictTakingPrice(Double.valueOf(size));
    }

    /**
     * Predict the taking price.
     * 
     * @param size A taking size.
     * @return A predicted price.
     */
    public final Num predictTakingPrice(Num size) {
        return predictTakingPrice(size.doubleValue());
    }

    /**
     * Predict the taking price.
     * 
     * @param size A taking size.
     * @return A predicted price.
     */
    public final Num predictTakingPrice(Variable<Num> size) {
        return predictTakingPrice(size.v);
    }

    /**
     * Predict the taking price.
     * 
     * @param size A taking size.
     * @return A predicted price.
     */
    public final Signal<Num> predictTakingPrice(Signal<Num> size) {
        return best.observing().combineLatest(size).map(e -> predictTakingPrice(e.ⅱ));
    }

    /**
     * Predict the real taking price.
     * 
     * @param size A taking size.
     * @return A predicted price.
     */
    public final Signal<Num> predictRealTakingPrice(Signal<Num> size) {
        return best.observing().combineLatest(size).map(e -> {
            Num price = predictTakingPrice(e.ⅱ);

            if (side == Direction.BUY) {
                return price.minus(takerFee.apply(price));
            } else {
                return price.plus(takerFee.apply(price));
            }
        });
    }

    /**
     * Predict the making price.
     * 
     * @param size A threshold size.
     * @return A predicted price.
     */
    public final Num predictMakingPrice(Num size) {
        return computeBestPrice(size, Num.ZERO);
    }

    /**
     * Compute the best price with your conditions (diff price).
     * 
     * @param diff A difference price.
     * @return A computed best price.
     */
    public final Num computeBestPrice(Num diff) {
        return computeBestPrice(best.v.price, Num.ZERO, diff);
    }

    /**
     * Compute the best price with your conditions (threashold volume and diff price).
     * 
     * @param threshold A threashold volume.
     * @param diff A difference price.
     * @return A computed best price.
     */
    public final Num computeBestPrice(Num threshold, Num diff) {
        return computeBestPrice(best.v.price, threshold, diff);
    }

    /**
     * Compute the best price with your conditions (start price, threashold volume and diff price).
     * 
     * @param start A start price.
     * @param threshold A threashold volume.
     * @param diff A difference price.
     * @return A computed best price.
     */
    public final Num computeBestPrice(Num start, Num threshold, Num diff) {
        return computeBestPrice(start.doubleValue(), threshold, diff);
    }

    /**
     * Compute the best price with your conditions (start price, threashold volume and diff price).
     * 
     * @param start A start price.
     * @param threshold A threashold volume.
     * @param diff A difference price.
     * @return A computed best price.
     */
    final Num computeBestPrice(double start, Num threshold, Num diff) {
        Num total = Num.ZERO;
        boolean buy = side.isBuy();
        for (OrderBookPage board : base.values()) {
            if (buy ? board.price <= start : board.price >= start) {
                total = total.plus(board.size);

                if (total.isGreaterThanOrEqual(threshold)) {
                    return Num.of(board.price).plus(side, diff);
                }
            }
        }
        return null;
    }

    /**
     * Fix error price.
     * 
     * @param hint A price hint.
     */
    public void fix(double hint) {
        if (!base.isEmpty()) {
            double price = base.firstDoubleKey();

            while (side.isBuy() ? price > hint : price < hint) {
                OrderBookPage removed = base.remove(price);

                if (grouped != null) {
                    updateGroup(price, removed.size * -1);
                    fixGroup(hint);
                }

                if (base.isEmpty()) {
                    break;
                } else {
                    price = base.firstDoubleKey();
                }
            }
        }
    }

    /**
     * Update orderbook.
     * 
     * @param changes
     */
    public void update(OrderBookChanges changes) {
        changes.each(side, this);

        int size = base.size();
        if (0 < size) {
            best.set(base.firstEntry().getValue());

            if (5000 < size) {
                for (int i = 0; i < 250; i++) {
                    OrderBookPage removed = base.pollLastEntry().getValue();
                    if (grouped != null) {
                        updateGroup(removed.price, removed.size * -1);
                    }
                }
            }
        }
        updating.accept(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void change(double price, float size) {
        if (size == 0d) {
            // remove
            OrderBookPage removed = base.remove(price);

            if (removed != null && grouped != null) {
                updateGroup(removed.price, removed.size * -1);
            }
        } else {
            // add
            OrderBookPage previous = base.put(price, new OrderBookPage(price, size));

            if (grouped != null) {
                if (previous == null) {
                    updateGroup(price, size);
                } else {
                    updateGroup(price, size - previous.size);
                }
            }
        }
    }

    private static double floor(double value, double base, int scale) {
        return Primitives.roundDecimal(value - value % base, scale);
    }
}