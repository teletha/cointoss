/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.magicwerk.brownies.collections.GapList;

import cointoss.Side;
import cointoss.util.Num;
import kiss.Signal;
import kiss.Signaling;
import kiss.Variable;

/**
 * @version 2017/12/01 1:12:33
 */
public class OrderBookList {

    /** The best order. */
    public final Variable<OrderUnit> best = Variable.empty();

    /** The modified event listeners. */
    private final Signaling<Boolean> modify = new Signaling();

    /** The modified event stream. */
    public final Signal<Boolean> modified = modify.expose;

    /** The search direction. */
    private final Side side;

    /** The base list. */
    final ObservableList<OrderUnit> x1;

    /** The base list. */
    final ObservableList<OrderUnit> x10;

    /** The base list. */
    final ObservableList<OrderUnit> x100;

    /** The base list. */
    final ObservableList<OrderUnit> x500;

    /** The base list. */
    final ObservableList<OrderUnit> x1000;

    /** The base list. */
    final ObservableList<OrderUnit> x10000;

    private Grouped[] group = new Grouped[5];

    /**
     * @param side
     */
    public OrderBookList(Side side) {
        this.side = side;
        this.x1 = FXCollections.observableList(GapList.create());
        this.x10 = FXCollections.observableList(GapList.create());
        this.x100 = FXCollections.observableList(GapList.create());
        this.x500 = FXCollections.observableList(GapList.create());
        this.x1000 = FXCollections.observableList(GapList.create());
        this.x10000 = FXCollections.observableList(GapList.create());

        group[0] = new Grouped(side, Num.of(10), x10);
        group[1] = new Grouped(side, Num.of(100), x100);
        group[2] = new Grouped(side, Num.of(500), x500);
        group[3] = new Grouped(side, Num.of(1000), x1000);
        group[4] = new Grouped(side, Num.of(10000), x10000);
    }

    /**
     * Retrieve minimum unit.
     * 
     * @return
     */
    public synchronized OrderUnit min() {
        if (x1.isEmpty()) {
            return null;
        } else {
            return x1.get(x1.size() - 1);
        }
    }

    /**
     * Retrieve maximum unit.
     * 
     * @return
     */
    public synchronized OrderUnit max() {
        if (x1.isEmpty()) {
            return null;
        } else {
            return x1.get(0);
        }
    }

    /**
     * Select list by ratio.
     * 
     * @param ratio
     * @return
     */
    public ObservableList<OrderUnit> selectBy(Range ratio) {
        switch (ratio) {
        case x1:
            return x1;

        case x10:
            return x10;

        case x100:
            return x100;

        case x500:
            return x500;

        case x1000:
            return x1000;

        default:
            return x10000;
        }
    }

    public synchronized Num computeBestPrice(Num threshold, Num diff) {
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
    public synchronized Num computeBestPrice(Num start, Num threshold, Num diff) {
        Num total = Num.ZERO;
        if (side == Side.BUY) {
            for (OrderUnit unit : x1) {
                if (unit.price.isLessThan(start)) {
                    total = total.plus(unit.size);

                    if (total.isGreaterThanOrEqual(threshold)) {
                        return unit.price.plus(diff);
                    }
                }
            }
        } else {
            for (int i = x1.size() - 1; 0 <= i; i--) {
                OrderUnit unit = x1.get(i);

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
    public synchronized void fix(Num hint) {
        if (side == Side.BUY) {
            for (int i = 0; i < x1.size();) {
                OrderUnit unit = x1.get(i);

                if (unit != null && unit.price.isGreaterThan(hint)) {
                    x1.remove(i);

                    for (Grouped grouped : group) {
                        grouped.update(unit.price, unit.size.negate());
                    }
                } else {
                    break;
                }
            }
        } else {
            for (int i = x1.size() - 1; 0 <= i; i--) {
                OrderUnit unit = x1.get(i);

                if (unit != null && unit.price.isLessThan(hint)) {
                    x1.remove(i);

                    for (Grouped grouped : group) {
                        grouped.update(unit.price, unit.size.negate());
                    }
                } else {
                    break;
                }
            }
        }
        modify.accept(true);
    }

    /**
     * Update orders.
     * 
     * @param asks
     */
    public synchronized void update(List<OrderUnit> units) {
        if (side == Side.BUY) {
            for (OrderUnit unit : units) {
                head(unit);
            }

            if (x1.isEmpty() == false) {
                best.set(x1.get(0));
            }
        } else {
            for (OrderUnit unit : units) {
                tail(unit);
            }

            if (x1.isEmpty() == false) {
                best.set(x1.get(x1.size() - 1));
            }
        }

        modify.accept(true);
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
        for (int i = 0; i < x1.size(); i++) {
            OrderUnit unit = x1.get(i);

            if (unit == null) {
                if (add.size.isNotZero()) {
                    x1.set(i, add);
                    update(add.price, add.size);
                }
                return;
            } else if (unit.price.is(add.price)) {
                OrderUnit old;

                if (add.size.isZero()) {
                    old = x1.remove(i);
                } else {
                    old = x1.set(i, add);
                }
                update(add.price, add.size.minus(old.size));
                return;
            } else if (unit.price.isLessThan(add.price)) {
                if (add.size.isNotZero()) {
                    x1.add(i, add);
                    update(add.price, add.size);
                }
                return;
            }
        }

        if (add.size.isNotZero()) {
            x1.add(add);
            update(add.price, add.size);
        }
    }

    /**
     * Update {@link OrderUnit}.
     * 
     * @param add
     */
    private void tail(OrderUnit add) {
        for (int i = x1.size() - 1; 0 <= i; i--) {
            OrderUnit unit = x1.get(i);

            if (unit == null) {
                if (add.size.isNotZero()) {
                    x1.set(i, add);
                    update(add.price, add.size);
                }
                return;
            } else if (unit.price.is(add.price)) {
                OrderUnit old;

                if (add.size.isZero()) {
                    old = x1.remove(i);
                } else {
                    old = x1.set(i, add);
                }
                update(add.price, add.size.minus(old.size));
                return;
            } else if (unit.price.isGreaterThan(add.price)) {
                if (add.size.isNotZero()) {
                    x1.add(i + 1, add);
                    update(add.price, add.size);
                }
                return;
            }
        }

        if (add.size.isNotZero()) {
            x1.add(0, add);
            update(add.price, add.size);
        }
    }

    /**
     * Update group list.
     * 
     * @param size
     */
    private void update(Num price, Num size) {
        for (Grouped grouped : group) {
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
     * @version 2017/12/01 13:04:56
     */
    public static enum Range {
        x1(1), x10(10), x100(100), x500(500), x1000(1000), x10000(10000);

        public final int ratio;

        /**
         * @param ratio
         */
        private Range(int ratio) {
            this.ratio = ratio;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return String.valueOf(ratio);
        }
    }

    /**
     * @version 2017/12/01 2:41:13
     */
    private static class Grouped {

        /** The search direction. */
        private final Side side;

        /** The price range. */
        private final Num range;

        /** The base list. */
        public final ObservableList<OrderUnit> list;

        /**
         * @param side
         * @param scale
         */
        private Grouped(Side side, Num range, ObservableList<OrderUnit> list) {
            this.side = side;
            this.range = range;
            this.list = list;
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
            for (int i = 0; i < list.size(); i++) {
                OrderUnit unit = list.get(i);

                if (unit == null) {
                    list.set(i, new OrderUnit(price, size));
                    return;
                } else if (unit.price.is(price)) {
                    Num remaining = unit.size.plus(size);

                    if (remaining.scale(5).isZero()) {
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

                    if (remaining.scale(5).isZero()) {
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
    }
}
