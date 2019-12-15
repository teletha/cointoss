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
import kiss.Variable;

public class OrderBook {

    /** The best order. */
    public final Variable<OrderBoard> best = Variable.empty();

    /** The search direction. */
    private final Direction side;

    /** The scale for target currency. */
    private final int scale;

    /** The board list replacer. (OPTIONAL) */
    private UnaryOperator<List<OrderBoard>> replacer;

    /** The book operation thread. (OPTIONAL) */
    private Consumer<Runnable> operator = Runnable::run;

    /** The base boards. */
    private final ConcurrentSkipListMap<Num, OrderBoard> base;

    /** The grouped order book. */
    private GroupedOrderBook group;

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
    public final void replaceBy(UnaryOperator<List<OrderBoard>> replacer) {
        if (replacer != null) {
            this.replacer = replacer;

            group.boards = replacer.apply(group.boards);
        }
    }

    /**
     * Get the grouped view of this {@link OrderBook}.
     * 
     * @param range The price range.
     * @return A grouped view.
     */
    public final List<OrderBoard> groupBy(Num range) {
        if (group.range.isNot(range)) {
            group = new GroupedOrderBook(range);
        }
        return group.boards;
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
        for (OrderBoard board : base.values()) {
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
            Num price = base.firstKey();

            while (price != null && price.isGreaterThan(side, hint)) {
                OrderBoard removed = base.remove(price);

                group.update(price, removed.size * -1);
                group.fix(hint);

                price = base.firstKey();
            }
        });
    }

    /**
     * Update orders.
     * 
     * @param asks
     */
    public void update(List<OrderBoard> units) {
        operator.accept(() -> {
            for (OrderBoard board : units) {
                if (board.size == 0d) {
                    // remove
                    OrderBoard removed = base.remove(board.price);

                    if (removed != null) {
                        group.update(removed.price, removed.size * -1);
                    }
                } else {
                    // add
                    OrderBoard previous = base.put(board.price, board);

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

        /** The board container. */
        private List<OrderBoard> boards = new SafeGapList();

        /**
         * Build {@link GroupedOrderBook}.
         * 
         * @param range A price range to group.
         */
        private GroupedOrderBook(Num range) {
            this.range = Objects.requireNonNull(range);

            // grouping the current boards
            for (OrderBoard board : base.values()) {
                update(board.price, board.size);
            }

            // replace container if needed
            if (replacer != null) boards = replacer.apply(boards);
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
         * Update {@link OrderBoard}.
         * 
         * @param add
         */
        private void head(Num price, double size) {
            for (int i = 0; i < boards.size(); i++) {
                OrderBoard unit = boards.get(i);

                if (unit == null) {
                    boards.set(i, new OrderBoard(price, size));
                    return;
                } else if (unit.price.is(price)) {
                    double remaining = unit.size + size;

                    if (Primitives.roundDecimal(remaining, scale, RoundingMode.DOWN) <= 0) {
                        boards.remove(i);
                    } else {
                        boards.set(i, new OrderBoard(unit.price, remaining));
                    }
                    return;
                } else if (unit.price.isLessThan(price)) {
                    boards.add(i, new OrderBoard(price, size));
                    return;
                }
            }
            boards.add(new OrderBoard(price, size));
        }

        /**
         * Update {@link OrderBoard}.
         * 
         * @param add
         */
        private void tail(Num price, double size) {
            for (int i = boards.size() - 1; 0 <= i; i--) {
                OrderBoard unit = boards.get(i);

                if (unit == null) {
                    boards.set(i, new OrderBoard(price, size));
                    return;
                } else if (unit.price.is(price)) {
                    double remaining = unit.size + size;

                    if (Primitives.roundDecimal(remaining, scale, RoundingMode.DOWN) <= 0) {
                        boards.remove(i);
                    } else {
                        boards.set(i, new OrderBoard(unit.price, remaining));
                    }
                    return;
                } else if (unit.price.isGreaterThan(price)) {
                    boards.add(i + 1, new OrderBoard(price, size));
                    return;
                }
            }
            boards.add(0, new OrderBoard(price, size));
        }

        /**
         * Fix error price.
         * 
         * @param hint A price hint.
         */
        private void fix(Num price) {
            int index = side == Direction.BUY ? 0 : boards.size() - 1;
            price = calculateGroupedPrice(price, range);

            OrderBoard unit = boards.get(index);

            if (unit.price.isGreaterThan(side, price)) {
                boards.remove(index);
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
