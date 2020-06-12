/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.magicwerk.brownies.collections.GapList;

import cointoss.Direction;
import cointoss.MarketSetting;
import cointoss.util.Num;
import cointoss.util.Primitives;
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

    /** The board list replacer. (OPTIONAL) */
    private UnaryOperator<List<OrderBookPage>> replacer;

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
        this.scale = setting.targetCurrencyScaleSize;
        this.group = new GroupedOrderBook(setting.baseCurrencyMinimumBidPrice);
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
        if (replacer != null) {
            this.replacer = replacer;

            group.pages = replacer.apply(group.pages);
        }
    }

    /**
     * Get the grouped view of this {@link OrderBook}.
     * 
     * @param range The price range.
     * @return A grouped view.
     */
    public final List<OrderBookPage> groupBy(Num range) {
        if (group.range.isNot(range)) {
            group = new GroupedOrderBook(range);
        }
        return group.pages;
    }

    /**
     * Iterate all pages of the current selected grouped view by ascending order.
     * 
     * @return
     */
    public final Iterator<OrderBookPage> ascendingPages() {
        return group.original.iterator();
    }

    /**
     * Iterate all pages of the current selected grouped view by descending order.
     * 
     * @return
     */
    public final Iterator<OrderBookPage> descendingPages() {
        return group.original.descendingIterator();
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

        /** The original page list. */
        private final SafeGapList<OrderBookPage> original = new SafeGapList();

        /** The page list. */
        private List<OrderBookPage> pages = original;

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

            // replace container if needed
            if (replacer != null) pages = replacer.apply(pages);
        }

        /**
         * Update price and size.
         * 
         * @param price
         * @param size
         */
        private void update(Num price, double size) {
            price = calculateGroupedPrice(price, range);

            if (side == Direction.BUY) {
                head(price, size);
            } else {
                tail(price, size);
            }
        }

        /**
         * Update {@link OrderBookPage}.
         * 
         * @param add
         */
        private void head(Num price, double size) {
            for (int i = 0; i < pages.size(); i++) {
                OrderBookPage unit = pages.get(i);

                if (unit == null) {
                    pages.set(i, new OrderBookPage(price, size));
                    return;
                } else if (unit.price.is(price)) {
                    double remaining = unit.size + size;

                    if (Primitives.roundDecimal(remaining, scale, RoundingMode.DOWN) <= 0) {
                        pages.remove(i);
                    } else {
                        pages.set(i, new OrderBookPage(unit.price, remaining));
                    }
                    return;
                } else if (unit.price.isLessThan(price)) {
                    pages.add(i, new OrderBookPage(price, size));
                    return;
                }
            }
            pages.add(new OrderBookPage(price, size));
        }

        /**
         * Update {@link OrderBookPage}.
         * 
         * @param add
         */
        private void tail(Num price, double size) {
            for (int i = pages.size() - 1; 0 <= i; i--) {
                OrderBookPage unit = pages.get(i);

                if (unit == null) {
                    pages.set(i, new OrderBookPage(price, size));
                    return;
                } else if (unit.price.is(price)) {
                    double remaining = unit.size + size;

                    if (Primitives.roundDecimal(remaining, scale, RoundingMode.DOWN) <= 0) {
                        pages.remove(i);
                    } else {
                        pages.set(i, new OrderBookPage(unit.price, remaining));
                    }
                    return;
                } else if (unit.price.isGreaterThan(price)) {
                    pages.add(i + 1, new OrderBookPage(price, size));
                    return;
                }
            }
            pages.add(0, new OrderBookPage(price, size));
        }

        /**
         * Fix error price.
         * 
         * @param hint A price hint.
         */
        private void fix(Num price) {
            int index = side == Direction.BUY ? 0 : pages.size() - 1;
            price = calculateGroupedPrice(price, range);

            if (!pages.isEmpty()) {
                OrderBookPage unit = pages.get(index);

                if (unit.price.isGreaterThan(side, price)) {
                    pages.remove(index);
                }
            }
        }
    }

    /**
     * Index-safe implementation. Because JavaFX's FilteredList is broken.
     */
    @SuppressWarnings("serial")
    private static class SafeGapList<E> extends GapList<E> {

        /**
         * {@inheritDoc}
         */
        @Override
        public E get(int index) {
            int size = size();

            if (size <= index) {
                index = size - 1;
            }

            if (index < 0) {
                index = 0;
            }
            return super.doGet(index);
        }
    }
}
