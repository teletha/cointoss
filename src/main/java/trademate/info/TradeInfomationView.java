/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.info;

import static transcript.Transcript.en;

import cointoss.Direction;
import cointoss.order.Order;
import cointoss.order.OrderBook;
import cointoss.position.Position;
import cointoss.position.PositionManager;
import cointoss.util.Num;
import stylist.Style;
import stylist.StyleDSL;
import trademate.TradingView;
import viewtify.ui.UI;
import viewtify.ui.UIButton;
import viewtify.ui.UILabel;
import viewtify.ui.View;
import viewtify.ui.helper.User;

/**
 * @version 2018/09/08 18:33:32
 */
public class TradeInfomationView extends View {

    /** UI */
    private UILabel positionSize;

    /** UI */
    private UILabel positionPrice;

    /** UI */
    private UILabel positionProfit;

    /** UI */
    private UIButton add;

    /** Parent View */
    private TradingView view;

    /**
     * {@inheritDoc}
     */
    @Override
    protected UI declareUI() {
        return new UI() {
            {
                $(vbox, S.Root, () -> {
                    $(hbox, S.Row, () -> {
                        label(en("Amount"), S.Label);
                        $(positionSize, S.Normal);
                    });
                    $(hbox, S.Row, () -> {
                        label(en("Price"), S.Label);
                        $(positionPrice, S.Normal);
                    });
                    $(hbox, S.Row, () -> {
                        label(en("Profit"), S.Label);
                        $(positionProfit, S.Normal);
                    });
                    $(add);
                });
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        PositionManager manager = view.market().positions;

        positionSize.text(manager.size);
        positionPrice.text(manager.price);
        positionProfit.text(manager.profit);

        add.text("ADD").when(User.Action).to(() -> {
            Position position = new Position();
            position.price = Num.of(10000);
            position.size.set(Num.of(1));
            position.side = Direction.BUY;
            System.out.println("add position");

            manager.add(position);
        });
    }

    /**
     * Request exit order.
     * 
     * @param position
     */
    @SuppressWarnings("unused")
    private void retreat(Position position) {
        OrderBook book = view.market().orderBook.bookFor(position.inverse());
        Num price = book.computeBestPrice(Num.ZERO, Num.TWO);

        view.order(Order.of(position.inverse(), position.size.v).price(price));
    }

    /**
     * @version 2018/09/07 14:14:11
     */
    @SuppressWarnings("unused")
    private interface S extends StyleDSL {

        Style Root = () -> {
            display.width(525, px);
            text.unselectable();
        };

        Style Row = () -> {
            padding.top(8, px);
            text.verticalAlign.middle();
        };

        Style Label = () -> {
            display.width(60, px);
            display.height(27, px);
        };

        Style Wide = () -> {
            display.width(120, px);
        };

        Style Normal = () -> {
            display.width(100, px);
        };

        Style Narrow = () -> {
            display.width(70, px);
        };
    }
}
