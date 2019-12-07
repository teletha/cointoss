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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.magicwerk.brownies.collections.GapList;

import com.google.common.annotations.VisibleForTesting;

import cointoss.Direction;
import cointoss.MarketSetting;
import cointoss.util.Num;
import kiss.Variable;

public class OrderBook {

    /** The best order. */
    public final Variable<OrderUnit> best = Variable.empty();

    /** The search direction. */
    private final Direction side;

    /** The base list. */
    @VisibleForTesting
    List<OrderUnit> base;

    /** The grouped list. */
    private final List<Grouped> groups = new ArrayList();

    /** The book operator thread. */
    private Consumer<Runnable> operator = Runnable::run;

    /**
     * @param side
     * @param base
     * @param ranges
     */
    OrderBook(MarketSetting setting, Direction side) {
        this.side = Objects.requireNonNull(side);

        this.base = new SafeGapList();
        for (Num range : setting.orderBookGroupRanges()) {
            groups.add(new Grouped(side, range, setting));
        }
    }

    /**
     * Set book operator thread.
     * 
     * @param operator
     */
    public final void setOperator(Consumer<Runnable> operator) {
        if (operator != null) {
            this.operator = operator;
        }
    }

    /**
     * Replace the order book management container with your container.
     * 
     * @param <L>
     * @param composer
     * @return
     */
    public final void composeBy(UnaryOperator<List<OrderUnit>> composer) {
        base = composer.apply(base);

        for (Grouped grouped : groups) {
            grouped.list = composer.apply(grouped.list);
        }
    }

    /**
     * Retrieve minimum unit.
     * 
     * @return
     */
    public OrderUnit min() {
        if (base.isEmpty()) {
            return null;
        } else {
            return base.get(base.size() - 1);
        }
    }

    /**
     * Retrieve maximum unit.
     * 
     * @return
     */
    public OrderUnit max() {
        if (base.isEmpty()) {
            return null;
        } else {
            return base.get(0);
        }
    }

    /**
     * Select list by ratio.
     * 
     * @param ratio
     * @return
     */
    public List<OrderUnit> selectBy(Num range) {
        for (Grouped grouped : groups) {
            if (grouped.range.is(range)) {
                return grouped.list;
            }
        }
        return base;
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
            for (OrderUnit unit : base) {
                if (unit.price.isLessThanOrEqual(start)) {
                    total = total.plus(unit.size);

                    if (total.isGreaterThanOrEqual(threshold)) {
                        return unit.price.plus(diff);
                    }
                }
            }
        } else {
            for (int i = base.size() - 1; 0 <= i; i--) {
                OrderUnit unit = base.get(i);

                if (unit.price.isGreaterThanOrEqual(start)) {
                    total = total.plus(unit.size);

                    if (total.isGreaterThanOrEqual(threshold)) {
                        return unit.price.minus(diff);
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
                ListIterator<OrderUnit> iterator = base.listIterator();

                while (iterator.hasNext()) {
                    OrderUnit unit = iterator.next();

                    if (unit != null && unit.price.isGreaterThan(hint)) {
                        iterator.remove();

                        for (Grouped grouped : groups) {
                            grouped.update(unit.price, unit.size.negate());
                            grouped.fixHead(hint);
                        }
                    } else {
                        break;
                    }
                }
            } else {
                ListIterator<OrderUnit> iterator = base.listIterator(base.size());

                while (iterator.hasPrevious()) {
                    OrderUnit unit = iterator.previous();

                    if (unit != null && unit.price.isLessThan(hint)) {
                        iterator.remove();

                        for (Grouped grouped : groups) {
                            grouped.update(unit.price, unit.size.negate());
                            grouped.fixTail(hint);
                        }
                    } else {
                        break;
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
    public void update(List<OrderUnit> units) {
        operator.accept(() -> {
            if (side == Direction.BUY) {
                for (OrderUnit unit : units) {
                    head(unit);
                }

                if (base.isEmpty() == false) {
                    best.set(base.get(0));
                }
            } else {
                for (OrderUnit unit : units) {
                    tail(unit);
                }

                if (base.isEmpty() == false) {
                    best.set(base.get(base.size() - 1));
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
     * Update {@link OrderUnit}.
     * 
     * @param add
     */
    private void head(OrderUnit add) {
        for (int i = 0; i < base.size(); i++) {
            OrderUnit unit = base.get(i);

            if (unit == null) {
                if (add.size.isNotZero()) {
                    base.set(i, add);
                    update(add.price, add.size);
                }
                return;
            } else if (unit.price.is(add.price)) {
                if (add.size.isZero()) {
                    base.remove(i);
                } else {
                    base.set(i, add);
                }
                update(add.price, add.size.minus(unit.size));
                return;
            } else if (unit.price.isLessThan(add.price)) {
                if (add.size.isNotZero()) {
                    base.add(i, add);
                    update(add.price, add.size);
                }
                return;
            }
        }

        if (add.size.isNotZero()) {
            base.add(add);
            update(add.price, add.size);
        }
    }

    /**
     * Update {@link OrderUnit}.
     * 
     * @param add
     */
    private void tail(OrderUnit add) {
        for (int i = base.size() - 1; 0 <= i; i--) {
            OrderUnit unit = base.get(i);

            if (unit == null) {
                if (add.size.isNotZero()) {
                    base.set(i, add);
                    update(add.price, add.size);
                }
                return;
            } else if (unit.price.is(add.price)) {
                if (add.size.isZero()) {
                    base.remove(i);
                } else {
                    base.set(i, add);
                }
                update(add.price, add.size.minus(unit.size));
                return;
            } else if (unit.price.isGreaterThan(add.price)) {
                if (add.size.isNotZero()) {
                    base.add(i + 1, add);
                    update(add.price, add.size);
                }
                return;
            }
        }

        if (add.size.isNotZero()) {
            base.add(0, add);
            update(add.price, add.size);
        }
    }

    /**
     * Update group list.
     * 
     * @param size
     */
    private void update(Num price, Num size) {
        for (Grouped grouped : groups) {
            grouped.update(price, size);
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
     * @version 2018/12/03 20:26:46
     */
    private class Grouped {

        /** The search direction. */
        private final Direction side;

        /** The price range. */
        private final Num range;

        /** The base list. */
        private List<OrderUnit> list = new SafeGapList();

        /** The cache */
        private final int size;

        /**
         * @param side
         * @param scaleSize
         */
        private Grouped(Direction side, Num range, MarketSetting setting) {
            this.side = side;
            this.range = range;
            this.size = setting.targetCurrencyScaleSize();
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
         * Update {@link OrderUnit}.
         * 
         * @param add
         */
        private void head(Num price, Num size) {

            for (int i = 0; i < list.size(); i++) {
                OrderUnit unit = list.get(i);

                if (unit == null) {
                    list.set(i, new OrderUnit(price, size));
                    return;
                } else if (unit.price.is(price)) {
                    Num remaining = unit.size.plus(size);

                    if (remaining.scaleDown(this.size).isNegativeOrZero()) {
                        list.remove(i);
                    } else {
                        list.set(i, unit.size(remaining));
                    }
                    return;
                } else if (unit.price.isLessThan(price)) {
                    list.add(i, new OrderUnit(price, size));
                    return;
                }
            }
            list.add(new OrderUnit(price, size));
        }

        /**
         * Update {@link OrderUnit}.
         * 
         * @param add
         */
        private void tail(Num price, Num size) {
            for (int i = list.size() - 1; 0 <= i; i--) {
                OrderUnit unit = list.get(i);

                if (unit == null) {
                    list.set(i, new OrderUnit(price, size));
                    return;
                } else if (unit.price.is(price)) {
                    Num remaining = unit.size.plus(size);

                    if (remaining.scaleDown(this.size).isNegativeOrZero()) {
                        list.remove(i);
                    } else {
                        list.set(i, unit.size(remaining));
                    }
                    return;
                } else if (unit.price.isGreaterThan(price)) {
                    list.add(i + 1, new OrderUnit(price, size));
                    return;
                }
            }
            list.add(0, new OrderUnit(price, size));
        }

        private void fixHead(Num price) {
            price = calculateGroupedPrice(price, range);

            OrderUnit unit = list.get(0);

            if (unit.price.isGreaterThan(price)) {
                list.remove(0);
            }
        }

        private void fixTail(Num price) {
            price = calculateGroupedPrice(price, range);

            OrderUnit unit = list.get(list.size() - 1);

            if (unit.price.isLessThan(price)) {
                list.remove(list.size() - 1);
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
