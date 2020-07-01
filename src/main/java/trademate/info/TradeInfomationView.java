/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.info;

import cointoss.order.OrderBook;
import cointoss.order.Position;
import cointoss.util.Num;
import stylist.Style;
import stylist.StyleDSL;
import trademate.TradingView;
import viewtify.ui.UIButton;
import viewtify.ui.UILabel;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;

public class TradeInfomationView extends View {

    /** UI */
    private UILabel positionSize;

    /** UI */
    private UILabel positionPrice;

    /** UI */
    private UILabel positionProfit;

    /** UI */
    private UIButton add;

    /** UI */
    private UIButton remove;

    /** Parent View */
    private TradingView view;

    class view extends ViewDSL {
        {
            $(vbox, style.root, () -> {
                $(hbox, style.row, () -> {
                    label(en("Amount"), style.label);
                    $(positionSize, style.normal);
                });
                $(hbox, style.row, () -> {
                    label(en("Price"), style.label);
                    $(positionPrice, style.normal);
                });
                $(hbox, style.row, () -> {
                    label(en("Profit"), style.label);
                    $(positionProfit, style.normal);
                });
                $(add);
                $(remove);
            });
        }
    }

    /**
     * @version 2018/09/07 14:14:11
     */
    @SuppressWarnings("unused")
    private interface style extends StyleDSL {

        Style root = () -> {
            display.width(525, px);
            text.unselectable();
        };

        Style row = () -> {
            padding.top(8, px);
            text.verticalAlign.middle();
        };

        Style label = () -> {
            display.width(60, px);
            display.height(27, px);
        };

        Style wide = () -> {
            display.width(120, px);
        };

        Style normal = () -> {
            display.width(100, px);
        };

        Style narrow = () -> {
            display.width(70, px);
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
    }

    /**
     * Request exit order.
     * 
     * @param position
     */
    @SuppressWarnings("unused")
    private void retreat(Position position) {
        OrderBook book = view.market.orderBook.bookFor(position.inverse());
        Num price = book.computeBestPrice(Num.ZERO, Num.TWO);
    }
}