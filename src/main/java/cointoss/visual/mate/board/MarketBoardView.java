/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.visual.mate.board;

import static java.util.concurrent.TimeUnit.*;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;

import org.magicwerk.brownies.collections.GapList;

import cointoss.Execution;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.order.OrderUnit;
import cointoss.util.Num;
import kiss.I;
import viewtify.View;
import viewtify.Viewtify;
import viewtify.ui.UIListView;
import viewtify.ui.UISpinner;

/**
 * @version 2017/11/14 19:16:13
 */
public class MarketBoardView extends View {

    /** Model for maker. */
    private final SortableGroupList long1000 = new SortableGroupList(true, -3);

    /** Model for maker. */
    private final SortableGroupList long100 = new SortableGroupList(true, -2);

    /** Model for maker. */
    private final SortableGroupList long10 = new SortableGroupList(true, -1);

    /** Model for maker. */
    private SortableUnitList long1 = new SortableUnitList(true, long10, long100, long1000);

    /** UI for long maker. */
    private @FXML UIListView<OrderUnit> longList;

    /** Model for maker. */
    private final SortableGroupList short1000 = new SortableGroupList(false, -3);

    /** Model for maker. */
    private final SortableGroupList short100 = new SortableGroupList(false, -2);

    /** Model for maker. */
    private final SortableGroupList short10 = new SortableGroupList(false, -1);

    /** Model for maker. */
    private final SortableUnitList short1 = new SortableUnitList(false, short10, short100, short1000);

    /** UI for maker. */
    private @FXML UIListView<OrderUnit> shortList;

    /** UI for interval configuration. */
    private @FXML UISpinner<List<SortableUnitList>> priceRange;

    /** UI for interval configuration. */
    private @FXML Label priceLatest;

    /** UI for interval configuration. */
    private @FXML Label priceSpread;

    /** UI for order. */
    private @FXML TextField orderPrice;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        longList.values(long1.list).cell(e -> new CellView());
        shortList.values(short1.list).cell(e -> new CellView()).scrollTo(short1.list.size() - 1);
        priceRange.values(I.signal(long1, short1, long10, short10, long100, short100, long1000, short1000).buffer(2).toList())
                .text(e -> e.get(0).toString())
                .observe(e -> {
                    longList.values(e.get(0).list);
                    shortList.values(e.get(1).list);
                });

        // read data from backend service
        Viewtify.inWorker(() -> {
            return BitFlyer.FX_BTC_JPY.service().getBoard().on(Viewtify.UIThread).to(board -> {
                for (OrderUnit unit : board.asks) {
                    short1.add(unit);
                }

                for (OrderUnit unit : board.bids) {
                    long1.add(unit);
                }

                OrderUnit bestAsk = short1.list.get(short1.list.size() - 1);
                OrderUnit bestBid = long1.list.get(0);

                if (bestAsk != null && bestBid != null) {
                    priceSpread.setText(bestAsk.price.minus(bestBid.price).toString());
                }
            });
        });

        Viewtify.inWorker(() -> {
            return BitFlyer.FX_BTC_JPY.log().fromToday().on(Viewtify.UIThread).effect(e -> {
                priceLatest.setText(e.price.toString());
            }).throttle(1, MINUTES).to(e -> {
                // fix error board
                long1.fix(e);
            });
        });
    }

    /**
     * @version 2017/11/13 21:35:32
     */
    private class CellView extends ListCell<OrderUnit> {

        /**
         * @param side
         */
        private CellView() {
            setOnMouseClicked(e -> {
                orderPrice.setText(getItem().price.toString());
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void updateItem(OrderUnit e, boolean empty) {
            super.updateItem(e, empty);

            if (empty || e == null) {
                setText(null);
                setGraphic(null);
                setStyle("-fx-background-insets: 0 300px 0 0;");
            } else {
                Num normalize = e.size.scale(5);
                setText(e.price() + "  " + normalize);
                setStyle("-fx-background-insets: 0 " + Num.of(210).minus(normalize.multiply(Num.TWO)) + "px 0 0;");
            }
        }
    }

    /**
     * @version 2017/11/14 15:53:29
     */
    static class SortableUnitList {

        /** The direction. */
        boolean fromHead;

        final ObservableList<OrderUnit> list;

        /** The group list. */
        private final SortableGroupList[] groups;

        /**
         * @param fromHead
         */
        SortableUnitList(boolean fromHead, SortableGroupList... groups) {
            this.fromHead = fromHead;
            this.groups = groups;
            this.list = FXCollections.observableList(GapList.create(empty()));
        }

        /**
         * Fix error board.
         * 
         * @param e
         */
        void fix(Execution e) {
            if (fromHead) {
                for (int i = 0; i < list.size(); i++) {
                    OrderUnit unit = list.get(i);
                }
            } else {

            }
        }

        /**
         * For test.
         */
        SortableUnitList(boolean fromHead, boolean empty) {
            this.fromHead = fromHead;
            this.groups = new SortableGroupList[0];
            this.list = FXCollections.observableArrayList();
        }

        /**
         * Update {@link OrderUnit}.
         * 
         * @param add
         */
        void add(OrderUnit add) {
            if (fromHead) {
                head(add);
            } else {
                tail(add);
            }
        }

        /**
         * Update {@link OrderUnit}.
         * 
         * @param add
         */
        private void head(OrderUnit add) {
            for (int i = 0; i < list.size(); i++) {
                OrderUnit unit = list.get(i);

                if (unit == null) {
                    if (add.size.isNotZero()) {
                        list.set(i, add);
                        update(add.price, add.size);
                    }
                    return;
                } else if (unit.price.is(add.price)) {
                    OrderUnit old;

                    if (add.size.isZero()) {
                        old = list.remove(i);
                    } else {
                        old = list.set(i, add);
                    }
                    update(add.price, add.size.minus(old.size));
                    return;
                } else if (unit.price.isLessThan(add.price)) {
                    if (add.size.isNotZero()) {
                        list.add(i, add);
                        update(add.price, add.size);
                    }
                    return;
                }
            }

            if (add.size.isNotZero()) {
                list.add(add);
                update(add.price, add.size);
            }
        }

        /**
         * Update {@link OrderUnit}.
         * 
         * @param add
         */
        private void tail(OrderUnit add) {
            for (int i = list.size() - 1; 0 <= i; i--) {
                OrderUnit unit = list.get(i);

                if (unit == null) {
                    if (add.size.isNotZero()) {
                        list.set(i, add);
                        update(add.price, add.size);
                    }
                    return;
                } else if (unit.price.is(add.price)) {
                    OrderUnit old;

                    if (add.size.isZero()) {
                        old = list.remove(i);
                    } else {
                        old = list.set(i, add);
                    }
                    update(add.price, add.size.minus(old.size));
                    return;
                } else if (unit.price.isGreaterThan(add.price)) {
                    if (add.size.isNotZero()) {
                        list.add(i + 1, add);
                        update(add.price, add.size);
                    }
                    return;
                }
            }

            if (add.size.isNotZero()) {
                list.add(0, add);
                update(add.price, add.size);
            }
        }

        /**
         * Update group list.
         * 
         * @param size
         */
        private void update(Num price, Num size) {
            for (SortableGroupList group : groups) {
                group.add(price, size);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "1";
        }
    }

    /**
     * @version 2017/11/14 15:53:29
     */
    static class SortableGroupList extends SortableUnitList {

        /** The scale. */
        private final int scale;

        /**
         * @param fromHead
         * @param scale
         */
        SortableGroupList(boolean fromHead, int scale) {
            super(fromHead);
            this.scale = scale;
        }

        /**
         * @param fromHead
         * @param scale
         */
        SortableGroupList(boolean fromHead, int scale, boolean empty) {
            super(fromHead, empty);
            this.scale = scale;
        }

        /**
         * Update price and size.
         * 
         * @param price
         * @param size
         */
        void add(Num price, Num size) {
            price = price.scaleDown(scale);

            if (fromHead) {
                head(price, size);
            } else {
                tail(price, size);
            }
        }

        /**
         * For test.
         * 
         * @param price
         * @param size
         */
        void add(int price, int size) {
            add(Num.of(price), Num.of(size));
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

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return Num.TEN.pow(scale * -1).toString();
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
}
