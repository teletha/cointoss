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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.magicwerk.brownies.collections.GapList;

import cointoss.Direction;
import cointoss.MarketSetting;
import cointoss.util.Num;
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

    /** The base list. */
    private final ConcurrentSkipListMap<Num, OrderBoard> base = new ConcurrentSkipListMap();

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
        if (side == Direction.BUY) {
            for (OrderBoard board : base.descendingMap().values()) {
                if (board.price.isLessThanOrEqual(start)) {
                    total = total.plus(board.size);

                    if (total.isGreaterThanOrEqual(threshold)) {
                        return board.price.plus(diff);
                    }
                }
            }
        } else {
            for (OrderBoard board : base.values()) {
                if (board.price.isGreaterThanOrEqual(start)) {
                    total = total.plus(board.size);

                    if (total.isGreaterThanOrEqual(threshold)) {
                        return board.price.minus(diff);
                    }
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
            if (side == Direction.BUY) {
                Num price = base.lastKey();

                while (price != null && price.isGreaterThan(hint)) {
                    OrderBoard removed = base.remove(price);

                    group.update(price, removed.size.negate());
                    group.fixHead(hint);

                    price = base.lastKey();
                }
            } else {
                Num price = base.firstKey();

                while (price != null && price.isLessThan(hint)) {
                    OrderBoard removed = base.remove(price);

                    group.update(price, removed.size.negate());
                    group.fixTail(hint);

                    price = base.firstKey();
                }
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
            if (side == Direction.BUY) {
                for (OrderBoard unit : units) {
                    head(unit);
                }

                if (base.isEmpty() == false) {
                    best.set(base.lastEntry().getValue());
                }
            } else {
                for (OrderBoard unit : units) {
                    tail(unit);
                }

                if (base.isEmpty() == false) {
                    best.set(base.firstEntry().getValue());
                }
            }
        });
    }

    // private void calculateTotal(ObservableList<OrderUnit> units) {
    // Num total = Num.ZERO;
    //
    // if (side == Side.BUY) {
    // for (int i = 0; i < units.size(); i++) {
    // OrderUnit unit = units.get(i);
    // total = total.plus(unit.size);
    // units.set(i, unit.total(total));
    // }
    // } else {
    // for (int i = units.size() - 1; 0 <= i; i--) {
    // OrderUnit unit = units.get(i);
    // total = total.plus(unit.size);
    // units.set(i, unit.total(total));
    // }
    // }
    // }

    /**
     * Update {@link OrderBoard}.
     * 
     * @param add
     */
    private void head(OrderBoard add) {
        if (add.size.isZero()) {
            // remove
            OrderBoard removed = base.remove(add.price);

            if (removed != null) {
                group.update(removed.price, removed.size.negate());
            }
        } else {
            // add
            OrderBoard previous = base.put(add.price, add);

            if (previous == null) {
                group.update(add.price, add.size);
            } else {
                group.update(add.price, add.size.minus(previous.size));
            }
        }
    }

    /**
     * Update {@link OrderBoard}.
     * 
     * @param add
     */
    private void tail(OrderBoard add) {
        if (add.size.isZero()) {
            // remove
            OrderBoard removed = base.remove(add.price);

            if (removed != null) {
                group.update(removed.price, removed.size.negate());
            }
        } else {
            // add
            OrderBoard previous = base.put(add.price, add);

            if (previous == null) {
                group.update(add.price, add.size);
            } else {
                group.update(add.price, add.size.minus(previous.size));
            }
        }
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
        private void update(Num price, Num size) {
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
        private void head(Num price, Num size) {
            for (int i = 0; i < boards.size(); i++) {
                OrderBoard unit = boards.get(i);

                if (unit == null) {
                    boards.set(i, new OrderBoard(price, size));
                    return;
                } else if (unit.price.is(price)) {
                    Num remaining = unit.size.plus(size);

                    if (remaining.scaleDown(scale).isNegativeOrZero()) {
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
        private void tail(Num price, Num size) {
            for (int i = boards.size() - 1; 0 <= i; i--) {
                OrderBoard unit = boards.get(i);

                if (unit == null) {
                    boards.set(i, new OrderBoard(price, size));
                    return;
                } else if (unit.price.is(price)) {
                    Num remaining = unit.size.plus(size);

                    if (remaining.scaleDown(scale).isNegativeOrZero()) {
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

        private void fixHead(Num price) {
            price = calculateGroupedPrice(price, range);

            OrderBoard unit = boards.get(0);

            if (unit.price.isGreaterThan(price)) {
                boards.remove(0);
            }
        }

        private void fixTail(Num price) {
            price = calculateGroupedPrice(price, range);

            OrderBoard unit = boards.get(boards.size() - 1);

            if (unit.price.isLessThan(price)) {
                boards.remove(boards.size() - 1);
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
