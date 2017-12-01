/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.order;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.magicwerk.brownies.collections.GapList;

import cointoss.Side;
import cointoss.util.Num;

/**
 * @version 2017/12/01 1:12:33
 */
public class OrderBookList {

    /** The search direction. */
    private final Side side;

    /** The base list. */
    public final ObservableList<OrderUnit> x1;

    /** The base list. */
    public final ObservableList<OrderUnit> x10;

    /** The base list. */
    public final ObservableList<OrderUnit> x100;

    /** The base list. */
    public final ObservableList<OrderUnit> x1000;

    private Grouped[] group = new Grouped[3];

    /**
     * @param side
     */
    public OrderBookList(Side side) {
        this.side = side;
        this.x1 = FXCollections.observableList(GapList.create(empty()));
        this.x10 = FXCollections.observableList(GapList.create(empty()));
        this.x100 = FXCollections.observableList(GapList.create(empty()));
        this.x1000 = FXCollections.observableList(GapList.create(empty()));

        group[0] = new Grouped(side, -1, x10);
        group[1] = new Grouped(side, -2, x100);
        group[2] = new Grouped(side, -3, x1000);
    }

    /**
     * For test.
     */
    OrderBookList(Side side, boolean empty) {
        this.side = side;
        this.x1 = FXCollections.observableArrayList();
        this.x10 = FXCollections.observableArrayList();
        this.x100 = FXCollections.observableArrayList();
        this.x1000 = FXCollections.observableArrayList();

        group[0] = new Grouped(side, -1, x10);
        group[1] = new Grouped(side, -2, x100);
        group[2] = new Grouped(side, -3, x1000);
    }

    /**
     * Update orders.
     * 
     * @param price A order price.
     * @param size A order size.
     */
    public void update(OrderUnit unit) {
        if (side == Side.BUY) {
            head(unit);
        } else {
            tail(unit);
        }
    }

    /**
     * Fix error price.
     * 
     * @param hint A price hint.
     */
    public void fix(Num hint) {
        if (side == Side.BUY) {
            Iterator<OrderUnit> iterator = x1.iterator();

            while (iterator.hasNext()) {
                OrderUnit unit = iterator.next();

                if (unit.price.isGreaterThan(hint)) {
                    iterator.remove();

                    for (Grouped grouped : group) {
                        grouped.update(unit.price, unit.size.negate());
                    }
                } else {
                    break;
                }
            }
        } else {
            for (int i = x1.size() - 1; 0 <= 0; i--) {
                OrderUnit unit = x1.get(i);

                if (unit.price.isLessThan(hint)) {
                    x1.remove(i);

                    for (Grouped grouped : group) {
                        grouped.update(unit.price, unit.size.negate());
                    }
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Retrieve minimum unit.
     * 
     * @return
     */
    public OrderUnit min() {
        return x1.get(x1.size() - 1);
    }

    /**
     * Retrieve maximum unit.
     * 
     * @return
     */
    public OrderUnit max() {
        return x1.get(0);
    }

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
     * Create empry list.
     * 
     * @return
     */
    private static List<OrderUnit> empty() {
        List<OrderUnit> list = new ArrayList();

        for (int i = 0; i < 30; i++) {
            list.add(null);
        }
        return list;
    }

    /**
     * @version 2017/12/01 2:41:13
     */
    private static class Grouped {

        /** The search direction. */
        private final Side side;

        /** The scale. */
        private final int scale;

        /** The base list. */
        public final ObservableList<OrderUnit> list;

        /**
         * @param side
         * @param scale
         */
        private Grouped(Side side, int scale, ObservableList<OrderUnit> list) {
            this.side = side;
            this.scale = scale;
            this.list = list;
        }

        /**
         * Update price and size.
         * 
         * @param price
         * @param size
         */
        private void update(Num price, Num size) {
            price = price.scaleDown(scale);

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
