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

/**
 * @version 2017/11/13 22:45:35
 */
public class MarketMakerBoard extends View {

    @FXML
    private ListView<Unit> longList;

    private ObservableList<Unit> bids = FXCollections.observableList(GapList.create());

    @FXML
    private ListView<Unit> shortList;

    private ObservableList<Unit> asks = FXCollections.observableList(GapList.create());

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        longList.setItems(bids);
        longList.setCellFactory(e -> new Cell(Side.BUY));
        shortList.setItems(asks);
        shortList.setCellFactory(e -> new Cell(Side.SELL));

        inWorker(() -> {
            return BitFlyer.FX_BTC_JPY.service().getBoard().on(UIThread).to(board -> {
                for (Unit ask : board.asks) {
                    add(asks, ask);
                }

                for (Unit bid : board.bids) {
                    addLong(bids, bid);
                }
            });
        });
    }

    private void add(ObservableList<Unit> list, Unit add) {
        for (int i = list.size() - 1; 0 <= i; i--) {
            Unit unit = list.get(i);

            if (unit.price.is(add.price)) {
                if (add.size.isZero()) {
                    list.remove(i);
                } else {
                    list.set(i, add);
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
                setText(e.price + "  " + e.size.scale(4));

                ObservableList<String> classes = getStyleClass();
                classes.clear();
                classes.add(side.name());
            }
        }
    }
}
