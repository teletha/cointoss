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

import java.util.function.BiFunction;

import javafx.collections.FXCollections;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;

import cointoss.order.OrderBookManager;
import cointoss.order.OrderBookPage;
import cointoss.util.Num;
import cointoss.util.Primitives;
import kiss.WiseRunnable;
import stylist.Style;
import stylist.StyleDSL;
import trademate.TradingView;
import viewtify.Viewtify;
import viewtify.ui.UILabel;
import viewtify.ui.UIListView;
import viewtify.ui.UISpinner;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.util.FXUtils;

public class OrderBookView extends View {

    /** UI for long maker. */
    private UIListView<OrderBookPage> longList;

    /** UI for maker. */
    private UIListView<OrderBookPage> shortList;

    /** UI for interval configuration. */
    private UISpinner<Num> priceRange;

    /** UI for interval configuration. */
    private UILabel priceSpread;

    /** UI for interval configuration. */
    private UISpinner<Num> hideSize;

    /** Parent View */
    private TradingView view;

    /** Order Book. */
    private OrderBookManager book;

    /**
     * UI definition.
     */
    class view extends ViewDSL {
        {
            $(vbox, style.root, () -> {
                $(shortList, style.book);
                $(hbox, () -> {
                    $(priceRange, style.priceRange);
                    $(priceSpread, style.priceSpread);
                    $(hideSize, style.hideSize);
                });
                $(longList, style.book);
            });
        }
    }

    /**
     * 
     */
    interface style extends StyleDSL {

        Style root = () -> {
            display.minWidth(158, px).maxWidth(158, px).height.fill();
        };

        Style book = () -> {
            display.height.fill();
            text.unselectable();

            $.select(".scroll-bar:horizontal").descendant(() -> {
                padding.size(0, px);
            });

            $.select(".cell", () -> {
                padding.size(0, px);
            });
        };

        Style priceRange = () -> {
            display.width(84, px);
        };

        Style priceSpread = () -> {
            display.width(50, px).height(25, px);
            text.indent(5, px);
        };

        Style hideSize = () -> {
            display.width(66, px);
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        book = view.market.orderBook;
        book.longs.replaceBy(FXCollections::observableList);
        book.shorts.replaceBy(FXCollections::observableList);
        book.longs.operateOn(Viewtify.UIThread);
        book.shorts.operateOn(Viewtify.UIThread);

        hideSize.initialize(Num.range(0, 99));

        int scale = view.market.service.setting.targetCurrencyScaleSize;
        // longList.renderByNode(Canvas::new, displayOrderUnit(TradeMateStyle.BUY, scale, false))
        // .take(hideSize, (unit, size) -> unit.size >= size.doubleValue())
        // .when(User.LeftClick, calculatePrice(longList));
        // shortList.renderByNode(Canvas::new, displayOrderUnit(TradeMateStyle.SELL, scale, true))
        // .take(hideSize, (unit, size) -> unit.size >= size.doubleValue())
        // .when(User.LeftClick, calculatePrice(shortList))
        // .scrollToBottom();

        priceRange.initialize(view.market.service.setting.orderBookGroupRangesWithBase()).observing(range -> {
            book.longs.groupBy(range);
            book.shorts.groupBy(range);
            // longList.items((ObservableList) book.longs.groupBy(range));
            // shortList.items((ObservableList) book.shorts.groupBy(range));
        });

        view.market.orderBook.spread.observe()
                .take(view.chart.showRealtimeUpdate)
                .on(Viewtify.UIThread)
                .to(price -> priceSpread.text(price));
    }

    /**
     * Calculate the best price.
     * 
     * @param list
     * @return
     */
    private WiseRunnable calculatePrice(UIListView<OrderBookPage> list) {
        return () -> {
            if (list == longList) {
                list.selectedItem().to(unit -> {
                    Num min = unit.price;
                    Num max = min.plus(priceRange.value());
                    Num best = book.longs.computeBestPrice(max, Num.of(3), Num.ONE);
                    view.builder.orderPrice.value(best.toString());
                });
            } else {
                list.selectedItem().to(unit -> {
                    Num min = unit.price;
                    Num best = book.shorts.computeBestPrice(min, Num.of(3), Num.ONE);
                    view.builder.orderPrice.value(best.toString());
                });
            }
        };
    }

    /**
     * Rendering {@link OrderBookPage}.
     * 
     * @param color
     * @param scale
     * @return
     */
    private BiFunction<Canvas, OrderBookPage, Canvas> displayOrderUnit(stylist.value.Color color, int scale, boolean isShort) {
        double width = longList.ui.widthProperty().doubleValue();
        double height = 22;
        double fontSize = 12;
        Color foreground = FXUtils.color(color);
        Color background = foreground.deriveColor(0, 1, 1, 0.2);
        Font font = Font.font(fontSize);
        Num minSize = view.market.service.setting.targetCurrencyMinimumBidSize();

        return (canvas, e) -> {
            canvas.setWidth(width);
            canvas.setHeight(height);

            double size = Primitives.roundDecimal(e.size, scale);
            double range = Math.min(width, size);
            Num pr = priceRange.value();
            Num price = isShort && pr.isNot(minSize) ? e.price.plus(pr) : e.price;

            GraphicsContext c = canvas.getGraphicsContext2D();
            c.clearRect(0, 0, width, height);
            c.setFill(background);
            c.fillRect(0, 0, range, height);

            c.setFont(font);
            c.setFill(foreground);
            c.setFontSmoothingType(FontSmoothingType.LCD);
            c.fillText(price + " " + size, 33, height - 7, width - 20);

            return canvas;
        };
    }
}
