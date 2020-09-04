/*
 * Copyright (C) 2020 cointoss Development Team
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
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import cointoss.Direction;
import cointoss.MarketSetting;
import cointoss.util.arithmeric.Num;
import cointoss.util.arithmeric.Primitives;
import kiss.Signal;
import kiss.Signaling;
import kiss.Variable;

public class OrderBook {

    /** The best order. */
    public final Variable<OrderBookPage> best = Variable.empty();

    /** The search direction. */
    private final Direction side;

    /** The scale for target currency. */
    private final int scale;

    /** The book operation thread. (OPTIONAL) */
    private Consumer<Runnable> operator = Runnable::run;

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
        this.scale = setting.target.scale;
        this.group = new GroupedOrderBook(setting.base.minimumSize);
    }

    /**
     * Set book operation thread.
     * 
     * @param operator
     */
    public final void operateOn(Consumer<Runnable> operator) {
        if (operator != null) {
            this.operator = operator;
        }
    }

    /**
     * Replace the order book management container with your container.
     * 
     * @param replacer A list replacer.
     * @return
     */
    public final void replaceBy(UnaryOperator<List<OrderBookPage>> replacer) {
        // if (replacer != null) {
        // this.replacer = replacer;
        //
        // group.pages = replacer.apply(group.pages);
        // }
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

            Num lowerRounded = calculateGroupedPrice(Num.max(base.lastKey(), lowerPrice), group.range);
            Num upperRounded = calculateGroupedPrice(Num.min(base.firstKey(), upperPrice), group.range);

            for (OrderBookPage page : group.pages.subMap(upperRounded, true, lowerRounded, true).values()) {
                if (max.size < page.size) {
                    max = page;
                }
            }
        } else {
            if (upperPrice.isLessThan(base.firstKey()) || lowerPrice.isGreaterThan(base.lastKey())) {
                return max;
            }

            Num lowerRounded = calculateGroupedPrice(Num.max(base.firstKey(), lowerPrice), group.range);
            Num upperRounded = calculateGroupedPrice(Num.min(base.lastKey(), upperPrice), group.range);

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
        operator.accept(() -> {
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
        });
    }

    /**
     * Update orders.
     * 
     * @param asks
     */
    public void update(List<OrderBookPage> units) {
        operator.accept(() -> {
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
        });
    }

    /**
     * Calculate the grouped price.
     * 
     * @param price
     * @param range
     * @return
     */
    static Num calculateGroupedPrice(Num price, Num range) {
        return price.minus(price.remainder(range));
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
            price = calculateGroupedPrice(price, range);

            OrderBookPage page = pages.computeIfAbsent(price, key -> new OrderBookPage(key, 0, side.isBuy() ? Num.ZERO : range));
            page.size += size;

            if (Primitives.roundDecimal(page.size, scale, RoundingMode.DOWN) <= 0) {
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
                hint = calculateGroupedPrice(hint, range);

                Entry<Num, OrderBookPage> entry = pages.firstEntry();
                while (entry != null && entry.getKey().isGreaterThan(side, hint)) {
                    pages.pollFirstEntry();
                    entry = pages.firstEntry();
                }
            }
        }
    }
}