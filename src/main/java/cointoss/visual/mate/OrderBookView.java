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

import static java.util.concurrent.TimeUnit.*;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;

import cointoss.market.bitflyer.BitFlyer;
import cointoss.order.OrderBook;
import cointoss.order.OrderBookList;
import cointoss.order.OrderBookList.Ratio;
import cointoss.order.OrderUnit;
import cointoss.util.Num;
import viewtify.View;
import viewtify.Viewtify;
import viewtify.ui.UIListView;
import viewtify.ui.UISpinner;

/**
 * @version 2017/11/14 19:16:13
 */
public class OrderBookView extends View<TradingView> {

    /** Order Book. */
    private final OrderBook book = new OrderBook();

    /** UI for long maker. */
    private @FXML UIListView<OrderUnit> longList;

    /** UI for maker. */
    private @FXML UIListView<OrderUnit> shortList;

    /** UI for interval configuration. */
    private @FXML UISpinner<OrderBookList.Ratio> priceRange;

    /** UI for interval configuration. */
    private @FXML Label priceLatest;

    /** UI for interval configuration. */
    private @FXML Label priceSpread;

    /** UI for order. */
    private @FXML TextField orderPrice;

    /** UI for interval configuration. */
    private @FXML UISpinner<Num> hideSize;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        longList.values(book.longs.x1).cell(e -> new CellView());
        shortList.values(book.shorts.x1).cell(e -> new CellView()).scrollTo(book.shorts.x1.size() - 1);
        hideSize.values(Num.range(0, 9)).initial(Num.ZERO).observe(e -> longList.ui.refresh());

        priceRange.values(Ratio.class).initial(Ratio.x1).observeNow(ratio -> {
            longList.values(book.longs.selectByRatio(ratio));
            shortList.values(book.shorts.selectByRatio(ratio));
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
            return parent.market.timeline.on(Viewtify.UIThread).effect(e -> {
                priceLatest.setText(e.price.toString());
            }).throttle(3, SECONDS).to(e -> {
                // fix error board
                book.shorts.fix(e.price);
                book.longs.fix(e.price);
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
                if (getListView() == longList.ui) {
                    Num min = getItem().price;
                    Num max = min.plus(priceRange.value().ratio);
                    Num best = book.longs.computeBestPrice(max, Num.of(3), Num.ONE);
                    orderPrice.setText(best.toString());
                } else {
                    Num min = getItem().price;
                    Num best = book.shorts.computeBestPrice(min, Num.of(3), Num.ONE);
                    orderPrice.setText(best.toString());
                }
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
                Num normalize = e.size.scale(3);
                setText(e.price() + "  " + normalize);

                StringBuilder style = new StringBuilder();
                style.append("-fx-background-insets: 0 " + Num.of(210).minus(normalize.multiply(Num.TWO)) + "px 0 0;");
                style.append("-fx-font-size: " + (normalize.isLessThan(hideSize.value()) ? "0.1px;" : "100%;"));
                setStyle(style.toString());
            }
        }
    }
}
