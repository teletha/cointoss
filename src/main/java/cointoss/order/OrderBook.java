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
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.UnaryOperator;

import cointoss.Direction;
import cointoss.MarketSetting;
import cointoss.util.arithmetic.Num;
import cointoss.util.arithmetic.Primitives;
import kiss.Signal;
import kiss.Signaling;
import kiss.Variable;

public class OrderBook {

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
    private final ConcurrentSkipListMap<Num, OrderBookPage> base;

    /** The grouped order book. */
    private GroupedOrderBook group;

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
        this.base = new ConcurrentSkipListMap(side.isBuy() ? Comparator.reverseOrder() : Comparator.naturalOrder());
        this.scaleBase = setting.base.scale;
        this.scaleTarget = setting.target.scale;
        this.takerFee = setting.takerFee;
        this.group = new GroupedOrderBook(setting.base.minimumSize);
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
    public final Collection<OrderBookPage> groupBy(Num range) {
        if (range.isPositive() && group.range.isNot(range)) {
            group = new GroupedOrderBook(range);
        }
        return group.pages.values();
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
        OrderBookPage max = new OrderBookPage(lowerPrice, 0, group.range);

        if (base.isEmpty()) {
            return max;
        }

        if (side.isBuy()) {
            if (lowerPrice.isGreaterThan(base.firstKey()) || upperPrice.isLessThan(base.lastKey())) {
                return max;
            }

            Num lowerRounded = Num.max(base.lastKey(), lowerPrice).floor(group.range);
            Num upperRounded = Num.min(base.firstKey(), upperPrice).floor(group.range);

            for (OrderBookPage page : group.pages.subMap(upperRounded, true, lowerRounded, true).values()) {
                if (max.size < page.size) {
                    max = page;
                }
            }
        } else {
            if (upperPrice.isLessThan(base.firstKey()) || lowerPrice.isGreaterThan(base.lastKey())) {
                return max;
            }

            Num lowerRounded = Num.max(base.firstKey(), lowerPrice).floor(group.range);
            Num upperRounded = Num.min(base.lastKey(), upperPrice).floor(group.range);

            for (OrderBookPage page : group.pages.subMap(lowerRounded, true, upperRounded, true).values()) {
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
        return group.pages.values();
    }

    public final ConcurrentNavigableMap<Num, OrderBookPage> headMap(Num price) {
        return group.pages.headMap(price, true);
    }

    public final ConcurrentNavigableMap<Num, OrderBookPage> tailMap(Num price) {
        return group.pages.tailMap(price, true);
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
            total += decrease * page.price.doubleValue();
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
        Num total = Num.ZERO;
        for (OrderBookPage board : base.values()) {
            if (board.price.isLessThanOrEqual(side, start)) {
                total = total.plus(board.size);

                if (total.isGreaterThanOrEqual(threshold)) {
                    return board.price.plus(side, diff);
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
    public void fix(Num hint) {
        if (!base.isEmpty()) {
            Num price = base.firstKey();

            while (price != null && price.isGreaterThan(side, hint)) {
                OrderBookPage removed = base.remove(price);

                group.update(price, removed.size * -1);
                group.fix(hint);

                if (base.isEmpty()) {
                    break;
                } else {
                    price = base.firstKey();
                }
            }
        }
    }

    /**
     * Update orders.
     * 
     * @param units
     */
    public void update(List<OrderBookPage> units) {
        for (OrderBookPage board : units) {
            if (board.size == 0d) {
                // remove
                OrderBookPage removed = base.remove(board.price);

                if (removed != null) {
                    group.update(removed.price, removed.size * -1);
                }
            } else {
                // add
                OrderBookPage previous = base.put(board.price, board);

                if (previous == null) {
                    group.update(board.price, board.size);
                } else {
                    group.update(board.price, board.size - previous.size);
                }
            }
        }

        if (base.isEmpty() == false) {
            best.set(base.firstEntry().getValue());
        }
        updating.accept(this);
    }

    /**
     * 
     */
    private class GroupedOrderBook {

        /** The price range. */
        private final Num range;

        private final ConcurrentSkipListMap<Num, OrderBookPage> pages = new ConcurrentSkipListMap(side.isBuy() ? Comparator.reverseOrder()
                : Comparator.naturalOrder());

        /**
         * Build {@link GroupedOrderBook}.
         * 
         * @param range A price range to group.
         */
        private GroupedOrderBook(Num range) {
            this.range = Objects.requireNonNull(range);

            // grouping the current boards
            for (OrderBookPage board : base.values()) {
                update(board.price, board.size);
            }
        }

        /**
         * Update price and size.
         * 
         * @param price
         * @param size
         */
        private void update(Num price, double size) {
            price = price.floor(range);

            OrderBookPage page = pages.computeIfAbsent(price, key -> new OrderBookPage(key, 0, side.isBuy() ? Num.ZERO : range));
            page.size += size;

            if (Primitives.roundDecimal(page.size, scaleTarget, RoundingMode.DOWN) <= 0) {
                pages.remove(price);
            }
        }

        /**
         * Fix error price.
         * 
         * @param hint A price hint.
         */
        private void fix(Num hint) {
            if (!pages.isEmpty()) {
                hint = hint.floor(range);

                Entry<Num, OrderBookPage> entry = pages.firstEntry();
                while (entry != null && entry.getKey().isGreaterThan(side, hint)) {
                    pages.pollFirstEntry();
                    entry = pages.firstEntry();
                }
            }
        }
    }
}