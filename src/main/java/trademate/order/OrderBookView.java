/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.order;

import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import cointoss.order.OrderBook;
import cointoss.order.OrderBookList;
import cointoss.order.OrderBookList.Range;
import cointoss.order.OrderUnit;
import cointoss.util.Num;
import trademate.TradingView;
import viewtify.UI;
import viewtify.View;
import viewtify.Viewtify;
import viewtify.ui.UILabel;
import viewtify.ui.UIListView;
import viewtify.ui.UISpinner;

/**
 * @version 2018/01/23 14:13:06
 */
public class OrderBookView extends View {

    /** UI for long maker. */
    private @UI UIListView<OrderUnit> longList;

    /** UI for maker. */
    private @UI UIListView<OrderUnit> shortList;

    /** UI for interval configuration. */
    private @UI UISpinner<OrderBookList.Range> priceRange;

    /** UI for interval configuration. */
    private @UI UILabel priceLatest;

    /** UI for interval configuration. */
    private @UI UILabel priceSpread;

    /** UI for order. */
    private @UI TextField orderPrice;

    /** UI for interval configuration. */
    private @UI UISpinner<Num> hideSize;

    /** Parent View */
    private @UI TradingView view;

    /** Order Book. */
    private OrderBook book;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        book = view.market().orderBook;

        hideSize.values(0, Num.range(0, 9)).observe(e -> longList.ui.refresh());

        longList.cell(e -> new CellView(Color.rgb(251, 189, 42, 0.2)))
                .filter(hideSize, (unit, size) -> unit.size.isGreaterThanOrEqual(size));
        shortList.cell(e -> new CellView(Color.rgb(247, 105, 77, 0.2)))
                .filter(hideSize, (unit, size) -> unit.size.isGreaterThanOrEqual(size))
                .scrollToBottom();

        priceRange.values(0, Range.class).observeNow(range -> {
            longList.values(book.longs.selectBy(range));
            shortList.values(book.shorts.selectBy(range));
        });

        view.market().tickers.latest.observe().on(Viewtify.UIThread).to(e -> priceLatest.text(e.price));
        view.market().orderBook.spread.observe().on(Viewtify.UIThread).to(price -> priceSpread.text(price));
    }

    /**
     * @version 2018/04/11 12:12:09
     */
    private class CellView extends ListCell<OrderUnit> {

        private final Rectangle back = new Rectangle(0, 16);

        /**
         * @param side
         */
        private CellView(Color color) {
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

            back.setFill(color);
            setGraphic(back);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void updateItem(OrderUnit e, boolean empty) {
            Viewtify.inUI(() -> {
                super.updateItem(e, empty);

                if (empty || e == null) {
                    setText(null);
                } else {
                    Num normalize = e.size.scale(3);
                    setText(e.price() + " " + normalize);

                    double width = Math.min(200, normalize.toDouble());
                    back.setWidth(width);
                    back.setTranslateX(width - 66);
                    setTranslateX(-width);

                    // using inline style makes memory leak
                    // setStyle("-fx-background-insets: 0 " +
                    // base.minus(normalize.multiply(Num.TWO)) + "px 0 0;");
                }
            });
        }
    }
}
