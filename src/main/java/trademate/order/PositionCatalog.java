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

import static trademate.TradeMateStyle.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import cointoss.Position;
import cointoss.PositionManager;
import cointoss.Side;
import cointoss.order.Order;
import cointoss.order.OrderBook;
import cointoss.util.Num;
import kiss.Extensible;
import stylist.StyleDSL;
import trademate.TradingView;
import trademate.order.PositionCatalog.Lang;
import viewtify.Style;
import viewtify.UI;
import viewtify.Viewtify;
import viewtify.ui.UITableColumn;
import viewtify.ui.UITableView;
import viewtify.ui.View;

/**
 * @version 2018/09/08 18:33:32
 */
public class PositionCatalog extends View<Lang> {

    /** The date formatter. */
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");

    /** UI */
    private UITableView<Position> table;

    /** UI */
    private UITableColumn<Position, ZonedDateTime> date;

    /** UI */
    private UITableColumn<Position, Side> side;

    /** UI */
    private UITableColumn<Position, Num> amount;

    /** UI */
    private UITableColumn<Position, Num> price;

    /** UI */
    private UITableColumn<Position, Num> profit;

    /** Parent View */
    private TradingView view;

    /**
     * {@inheritDoc}
     */
    @Override
    protected UI declareUI() {
        return new UI() {
            {
                $(table, S.Root, () -> {
                    $(date, S.Wide);
                    $(side, S.Narrow);
                    $(price, S.Wide);
                    $(amount, S.Normal);
                    $(profit, S.Normal);
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

        date.header($.date()).model(o -> o.date).render((ui, item) -> ui.text(formatter.format(item)));
        side.header($.side()).model(o -> o.side).render((ui, side) -> ui.text(side).styleOnly(Side.of(side)));
        price.model(o -> o.price).header($.price(), manager.price);
        amount.modelByVar(o -> o.size).header($.amount(), manager.size);
        profit.modelByVar(o -> o.profit).header($.profit(), manager.profit);

        table.ui.setItems(Viewtify.observe(manager.items, manager.added, manager.removed));
        table.selectMultipleRows().context($ -> {
            $.menu("撤退").whenUserClick(() -> table.selection().forEach(this::retreat));
        });
    }

    /**
     * Request exit order.
     * 
     * @param position
     */
    private void retreat(Position position) {
        OrderBook book = view.market().orderBook.bookFor(position.inverse());
        Num price = book.computeBestPrice(Num.ZERO, Num.TWO);

        view.order(Order.limit(position.inverse(), position.size.v, price));
    }

    /**
     * @version 2018/09/07 14:14:11
     */
    private interface S extends StyleDSL {

        Style Root = () -> {
            display.width(525, px);
            text.unselectable();
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

    /**
     * @version 2018/09/07 10:29:37
     */
    static class Lang implements Extensible {

        String date() {
            return "Date";
        }

        String side() {
            return "Side";
        }

        String amount() {
            return "Amount";
        }

        String price() {
            return "Price";
        }

        String profit() {
            return "Profit";
        }

        /**
         * @version 2018/09/07 10:44:14
         */
        private static class Lang_ja extends Lang {

            /**
             * {@inheritDoc}
             */
            @Override
            String date() {
                return "日付";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String side() {
                return "売買";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String amount() {
                return "数量";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String price() {
                return "値段";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String profit() {
                return "損益";
            }
        }
    }
}
