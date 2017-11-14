/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.visual.mate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import org.magicwerk.brownies.collections.GapList;

import cointoss.Board.Unit;
import cointoss.Side;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.util.Num;

/**
 * @version 2017/11/13 22:45:35
 */
public class MarketMakerBoard extends View {

    private @FXML ListView<Group> longList;

    private ObservableList<Group> bids = FXCollections.observableList(GapList.create());

    /** UI for short maker. */
    private @FXML ListView<Unit> shortList;

    /** Model for short maker. */
    private final SortedList short1 = new SortedList(0);

    /** Model for short maker. */
    private final SortedList short10 = new SortedList(-1);

    /** UI for short maker. */
    private @FXML ListView<Unit> short10List;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        // longList.setItems(bids);
        // longList.setCellFactory(e -> new Cell(Side.BUY));
        shortList.setItems(short1.list);
        shortList.setCellFactory(e -> new Cell(Side.SELL));
        short10List.setItems(short10.list);
        short10List.setCellFactory(e -> new Cell(Side.SELL));

        inWorker(() -> {
            return BitFlyer.FX_BTC_JPY.service().getBoard().on(UIThread).to(board -> {
                for (Unit unit : board.asks) {
                    short1.updateShort(unit);
                }

                // for (Unit bid : board.bids) {
                // addLong(bids, bid);
                // }
            });
        });
    }

    private void add(ObservableList<Group> list, Unit add, int scale, int interval) {
        for (int i = list.size() - 1; 0 <= i; i--) {
            Group unit = list.get(i);

            if (unit.is(add.price)) {
                if (add.size.isZero()) {
                    list.remove(i);
                } else {
                    list.set(i, new Group(add, scale, interval));
                }
                return;
            } else if (unit.isGreaterThan(add.price)) {
                if (add.size.isNotZero()) {
                    list.add(i + 1, new Group(add, scale, interval));
                }
                return;
            }
        }

        if (add.size.isNotZero()) {
            list.add(0, new Group(add, scale, interval));
        }
    }

    private void addLong(ObservableList<Unit> list, Unit add) {
        for (int i = 0; i < list.size(); i++) {
            Unit unit = list.get(i);

            if (unit.price.is(add.price)) {
                if (add.size.isZero()) {
                    list.remove(i);
                } else {
                    list.set(i, add);
                }
                return;
            } else if (unit.price.isLessThan(add.price)) {
                if (add.size.isNotZero()) {
                    list.add(i, add);
                }
                return;
            }
        }

        if (add.size.isNotZero()) {
            list.add(add);
        }
    }

    /**
     * @version 2017/11/13 21:35:32
     */
    private static class Cell extends ListCell<Unit> {

        private Side side;

        /**
         * @param side
         */
        private Cell(Side side) {
            this.side = side;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void updateItem(Unit e, boolean empty) {
            super.updateItem(e, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                setText(e.price() + "  " + e.size().scale(4));

                ObservableList<String> classes = getStyleClass();
                classes.clear();
                classes.add(side.name());
            }
        }
    }

    /**
     * @version 2017/11/14 15:53:29
     */
    static class SortableUnitList {

        /** The direction. */
        private boolean ascending;

        final ObservableList<Unit> list = FXCollections.observableList(GapList.create());

        /**
         * @param ascending
         */
        SortableUnitList(boolean ascending) {
            this.ascending = ascending;
        }

        /**
         * Update {@link Unit}.
         * 
         * @param add
         */
        void add(Unit add) {
            if (ascending) {
                addAscending(add);
            } else {
                addDecending(add);
            }
        }

        /**
         * Update {@link Unit}.
         * 
         * @param add
         */
        private void addAscending(Unit add) {
            for (int i = 0; i < list.size(); i++) {
                Unit unit = list.get(i);

                if (unit.price.is(add.price)) {
                    Unit old;

                    if (add.size.isZero()) {
                        old = list.remove(i);
                    } else {
                        old = list.set(i, add);
                    }
                    return;
                } else if (unit.price.isLessThan(add.price)) {
                    if (add.size.isNotZero()) {
                        list.add(i + 1, add);
                    }
                    return;
                }
            }

            if (add.size.isNotZero()) {
                list.add(add);
            }
        }

        /**
         * Update {@link Unit}.
         * 
         * @param add
         */
        private void addDecending(Unit add) {
            for (int i = list.size() - 1; 0 <= i; i--) {
                Unit unit = list.get(i);

                if (unit.price.is(add.price)) {
                    Unit old;

                    if (add.size.isZero()) {
                        old = list.remove(i);
                    } else {
                        old = list.set(i, add);
                    }
                    return;
                } else if (unit.price.isGreaterThan(add.price)) {
                    if (add.size.isNotZero()) {
                        list.add(i + 1, add);
                    }
                    return;
                }
            }

            if (add.size.isNotZero()) {
                list.add(0, add);
            }
        }
    }

    /**
     * @version 2017/11/14 14:33:39
     */
    class SortedList {

        final ObservableList<Unit> list = FXCollections.observableList(GapList.create());

        final int scale;

        final int interval;

        /**
         * @param scale
         */
        SortedList(int scale) {
            this.scale = scale;
            this.interval = Num.of(10).pow(scale * -1).toInt();
        }

        /**
         * Update
         * 
         * @param add
         */
        void updateShort(Unit add) {
            for (int i = list.size() - 1; 0 <= i; i--) {
                Unit unit = list.get(i);

                if (unit.price.is(add.price)) {
                    Unit old;

                    if (add.size.isZero()) {
                        old = list.remove(i);
                    } else {
                        old = list.set(i, add);
                    }

                    updateGroupShort(add.price, add.size.minus(old.size));
                    return;
                } else if (unit.isGreaterThan(add.price)) {
                    if (add.size.isNotZero()) {
                        list.add(i + 1, add);

                        updateGroupShort(add.price, add.size);
                    }
                    return;
                }
            }

            if (add.size.isNotZero()) {
                list.add(0, add);

                updateGroupShort(add.price, add.size);
            }
        }

        void updateGroupShort(Num price, Num size) {
            short10.updateGroupShort(price.scaleDown(-1), size);
        }
    }

    /**
     * @version 2017/11/14 13:37:31
     */
    static class Group extends Unit {

        /** The start price. */
        private final Num start;

        /** The end price. */
        private final Num end;

        /** The unit list. */
        private final Unit[] units;

        /** The sum of size. */
        Num size = Num.ZERO;

        /**
         * @param price
         * @param scale
         * @param interval
         */
        Group(Unit unit, int scale, int interval) {
            this.start = unit.price.scaleDown(scale);
            this.end = start.plus(interval);
            this.units = new Unit[interval];

            update(unit);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Num price() {
            return start;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Num size() {
            return size;
        }

        /**
         * Check price range.
         * 
         * @param price
         * @return
         */
        @Override
        public boolean is(Num price) {
            return start.isLessThanOrEqual(price) && price.isLessThan(end);
        }

        /**
         * Update unit.
         * 
         * @param update
         */
        boolean update(Unit update) {
            int index = update.price.minus(start).toInt();
            Unit old = units[index];
            units[index] = update;
            size = size.plus(update.size.minus(old == null ? Num.ZERO : old.size));

            return size.isZero();
        }
    }
}
