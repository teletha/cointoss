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

import java.util.List;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;

import cointoss.market.bitflyer.BitFlyer;
import cointoss.order.OrderBook;
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
    private final OrderBook book = new OrderBook();

    /** UI for long maker. */
    private @FXML UIListView<OrderUnit> longList;

    /** UI for maker. */
    private @FXML UIListView<OrderUnit> shortList;

    /** UI for interval configuration. */
    private @FXML UISpinner<List<ObservableList<OrderUnit>>> priceRange;

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
        longList.values(book.longs.x1).cell(e -> new CellView());
        shortList.values(book.shorts.x1).cell(e -> new CellView()).scrollTo(book.shorts.x1.size() - 1);
        priceRange.values(I
                .signal(book.longs.x1, book.shorts.x1, book.longs.x10, book.shorts.x10, book.longs.x100, book.shorts.x100, book.longs.x1000, book.shorts.x1000)
                .buffer(2)
                .toList()).text(e -> e.get(0).toString()).observe(e -> {
                    longList.values(e.get(0));
                    shortList.values(e.get(1));
                });

        // read data from backend service
        Viewtify.inWorker(() -> {
            return BitFlyer.FX_BTC_JPY.service().getBoard().on(Viewtify.UIThread).to(board -> {
                for (OrderUnit unit : board.asks) {
                    book.shorts.update(unit);
                }

                for (OrderUnit unit : board.bids) {
                    book.longs.update(unit);
                }

                OrderUnit bestAsk = book.shorts.min();
                OrderUnit bestBid = book.longs.max();

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
                book.shorts.fix(e.price);
                book.longs.fix(e.price);
                System.out.println("Fix erro board");
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
}
