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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.magicwerk.brownies.collections.GapList;

import cointoss.MarketSetting;
import cointoss.Side;
import cointoss.util.Num;
import kiss.Signal;
import kiss.Signaling;
import kiss.Variable;

/**
 * @version 2018/12/03 20:26:50
 */
public class OrderBook {

    /** The best order. */
    public final Variable<OrderUnit> best = Variable.empty();

    /** The modified event listeners. */
    private final Signaling<Boolean> modify = new Signaling();

    /** The modified event stream. */
    public final Signal<Boolean> modified = modify.expose;

    /** The search direction. */
    private final Side side;

    /** The base list. */
    final ObservableList<OrderUnit> base;

    /** The grouped list. */
    private final List<Grouped> groups = new ArrayList();

    /** The book operator thread. */
    private Consumer<Runnable> operator = task -> {
        task.run();
    };

    /**
     * @param side
     * @param base
     * @param ranges
     */
    OrderBook(MarketSetting setting, Side side) {
        this.side = Objects.requireNonNull(side);

        this.base = FXCollections.observableList(new GapList());
        for (Num range : setting.orderBookGroupRanges()) {
            groups.add(new Grouped(side, range, FXCollections.observableList(new GapList()), setting));
        }
    }

    /**
     * Set book operator thread.
     * 
     * @param operator
     */
    public void setOperator(Consumer<Runnable> operator) {
        if (operator != null) {
            this.operator = operator;
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
    public ObservableList<OrderUnit> selectBy(Num range) {
        for (Grouped grouped : groups) {
            if (grouped.range.is(range)) {
                return grouped.list;
            }
        }
        return base;
    }

    public Num computeBestPrice(Num threshold, Num diff) {
        return computeBestPrice(best.v.price, threshold, diff);
    }

    /**
     * <pre>
     * ■板の価格をクリックしたときの仕様
     * 板の価格を１回クリックするとその価格が入力される
     * 板の価格を２回クリックするとその価格グループで大きい板手前の有利な価格が入力される。
     * 例)
     * ２００円でグルーピングしてるときそのグループ内で0.9～3枚目の手前
     * １０００円でグルーピングしてるときそのグループ内で3～10枚目の手前
     * 3～10枚と変動基準は売り板買い板の仲値からの距離で変動する。
     * １グループ目3枚目の手前、１０グループ目以降１０枚になる。
     * </pre>
     * 
     * @return
     */
    public Num computeBestPrice(Num start, Num threshold, Num diff) {
        Num total = Num.ZERO;
        if (side == Side.BUY) {
            for (OrderUnit unit : base) {
                if (unit.price.isLessThan(start)) {
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
            if (side == Side.BUY) {
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
            modify.accept(true);
        });
    }

    /**
     * Update orders.
     * 
     * @param asks
     */
    public void update(List<OrderUnit> units) {
        operator.accept(() -> {
            if (side == Side.BUY) {
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

            modify.accept(true);
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
        ListIterator<OrderUnit> iterator = base.listIterator();

        while (iterator.hasNext()) {
            OrderUnit unit = iterator.next();

            if (unit == null) {
                if (add.size.isNotZero()) {
                    iterator.set(add);
                    update(add.price, add.size);
                }
                return;
            } else if (unit.price.is(add.price)) {
                if (add.size.isZero()) {
                    iterator.remove();
                } else {
                    iterator.set(add);
                }
                update(add.price, add.size.minus(unit.size));
                return;
            } else if (unit.price.isLessThan(add.price)) {
                if (add.size.isNotZero()) {
                    if (iterator.hasPrevious()) iterator.previous();
                    iterator.add(add);
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
        ListIterator<OrderUnit> iterator = base.listIterator(base.size());

        while (iterator.hasPrevious()) {
            OrderUnit unit = iterator.previous();

            if (unit == null) {
                if (add.size.isNotZero()) {
                    iterator.set(add);
                    update(add.price, add.size);
                }
                return;
            } else if (unit.price.is(add.price)) {
                if (add.size.isZero()) {
                    iterator.remove();
                } else {
                    iterator.set(add);
                }
                update(add.price, add.size.minus(unit.size));
                return;
            } else if (unit.price.isGreaterThan(add.price)) {
                if (add.size.isNotZero()) {
                    iterator.next();
                    iterator.add(add);
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
        private final Side side;

        /** The price range. */
        private final Num range;

        /** The base list. */
        private final ObservableList<OrderUnit> list;

        /** The cache */
        private final int size;

        /**
         * @param side
         * @param scaleSize
         */
        private Grouped(Side side, Num range, ObservableList<OrderUnit> list, MarketSetting setting) {
            this.side = side;
            this.range = range;
            this.list = list;
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

            if (side == Side.BUY) {
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
            ListIterator<OrderUnit> iterator = list.listIterator();

            while (iterator.hasNext()) {
                OrderUnit unit = iterator.next();

                if (unit == null) {
                    iterator.set(new OrderUnit(price, size));
                    return;
                } else if (unit.price.is(price)) {
                    Num remaining = unit.size.plus(size);

                    if (remaining.scaleDown(this.size).isNegativeOrZero()) {
                        iterator.remove();
                    } else {
                        iterator.set(unit.size(remaining));
                    }
                    return;
                } else if (unit.price.isLessThan(price)) {
                    if (iterator.hasPrevious()) iterator.previous();
                    iterator.add(new OrderUnit(price, size));
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
            ListIterator<OrderUnit> iterator = list.listIterator(list.size());

            while (iterator.hasPrevious()) {
                OrderUnit unit = iterator.previous();

                if (unit == null) {
                    iterator.set(new OrderUnit(price, size));
                    return;
                } else if (unit.price.is(price)) {
                    Num remaining = unit.size.plus(size);

                    if (remaining.scaleDown(this.size).isNegativeOrZero()) {
                        iterator.remove();
                    } else {
                        iterator.set(unit.size(remaining));
                    }
                    return;
                } else if (unit.price.isGreaterThan(price)) {
                    iterator.next();
                    iterator.add(new OrderUnit(price, size));
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
}
