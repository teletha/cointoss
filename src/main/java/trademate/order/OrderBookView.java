/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.order;

import static trademate.TradeShacklesStyle.*;

import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import cointoss.order.OrderBookManager;
import cointoss.order.OrderUnit;
import cointoss.util.Num;
import stylist.Style;
import stylist.StyleDSL;
import trademate.TradingView;
import viewtify.Viewtify;
import viewtify.ui.UI;
import viewtify.ui.UILabel;
import viewtify.ui.UIListView;
import viewtify.ui.UISpinner;
import viewtify.ui.View;

/**
 * @version 2018/09/07 12:39:29
 */
public class OrderBookView extends View {

    /** UI for long maker. */
    private UIListView<OrderUnit> longList;

    /** UI for maker. */
    private UIListView<OrderUnit> shortList;

    /** UI for interval configuration. */
    private UISpinner<Num> priceRange;

    /** UI for interval configuration. */
    private UILabel priceLatest;

    /** UI for interval configuration. */
    private UILabel priceSpread;

    /** UI for interval configuration. */
    private UISpinner<Num> hideSize;

    /** Parent View */
    private TradingView view;

    /** Order Book. */
    private OrderBookManager book;

    /**
     * {@inheritDoc}
     */
    @Override
    protected UI declareUI() {
        return new UI() {
            {
                $(vbox, S.Root, () -> {
                    $(shortList, S.Book, Short);
                    $(hbox, () -> {
                        $(priceRange, S.PriceRange);
                        $(priceLatest, S.PriceLatest);
                        $(priceSpread, S.PriceSpread);
                        $(hideSize, S.HideSize);
                    });
                    $(longList, S.Book, Long);
                });
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        book = view.market().orderBook;
        book.longs.setOperator(Viewtify.UIThread);
        book.shorts.setOperator(Viewtify.UIThread);

        hideSize.values(0, Num.range(0, 99));

        longList.cell(e -> new CellView(Color.rgb(251, 189, 42, 0.2))).take(hideSize, (unit, size) -> unit.size.isGreaterThanOrEqual(size));
        shortList.cell(e -> new CellView(Color.rgb(247, 105, 77, 0.2)))
                .take(hideSize, (unit, size) -> unit.size.isGreaterThanOrEqual(size))
                .scrollToBottom();

        priceRange.values(0, view.market().service.setting.orderBookGroupRangesWithBase()).observeNow(range -> {
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

        private final int scaleSize;

        /**
         * @param side
         */
        private CellView(Color color) {
            setOnMouseClicked(e -> {
                if (getListView() == longList.ui) {
                    Num min = getItem().price;
                    Num max = min.plus(priceRange.value());
                    Num best = book.longs.computeBestPrice(max, Num.of(3), Num.ONE);
                    view.builder.orderPrice.value(best.toString());
                } else {
                    Num min = getItem().price;
                    Num best = book.shorts.computeBestPrice(min, Num.of(3), Num.ONE);
                    view.builder.orderPrice.value(best.toString());
                }
            });

            back.setFill(color);
            setGraphic(back);
            scaleSize = view.market().service.setting.targetCurrencyScaleSize();
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
                    Num normalize = e.size.scale(scaleSize);
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

    /**
     * @version 2018/09/07 10:49:03
     */
    private interface S extends StyleDSL {

        Style Root = () -> {
            display.minWidth(220, px);
        };

        Style Book = () -> {
            text.unselectable();

            $.descendant(() -> {
                font.color("inherit");
                text.indent(66, px);
            });

            $.select(".scroll-bar:horizontal").descendant(() -> {
                padding.size(0, px);
            });
        };

        Style PriceRange = () -> {
            display.width(72, px);
        };

        Style PriceLatest = () -> {
            display.width(60, px).height(25, px);
            text.indent(12, px);
        };

        Style PriceSpread = () -> {
            display.width(50, px).height(25, px);
        };

        Style HideSize = () -> {
            display.width(60, px);
        };
    }
}
