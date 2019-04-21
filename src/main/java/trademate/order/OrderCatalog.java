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

import static cointoss.order.OrderState.ACTIVE;
import static trademate.CommonText.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import javafx.scene.control.TreeTableRow;

import cointoss.Direction;
import cointoss.order.Order;
import cointoss.order.OrderManager;
import cointoss.order.OrderState;
import cointoss.util.Num;
import stylist.Style;
import stylist.StyleDSL;
import stylist.ValueStyle;
import trademate.TradeMateStyle;
import trademate.TradingView;
import viewtify.Viewtify;
import viewtify.bind.Calculation;
import viewtify.ui.UI;
import viewtify.ui.UITreeTableColumn;
import viewtify.ui.UITreeTableView;
import viewtify.ui.UserInterface;
import viewtify.ui.View;

public class OrderCatalog extends View {

    /** The date formatter. */
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");

    /** UI */
    private UITreeTableView<Order> table;

    /** UI */
    private UITreeTableColumn<Order, ZonedDateTime> date;

    /** UI */
    private UITreeTableColumn<Order, Direction> side;

    /** UI */
    private UITreeTableColumn<Order, Num> amount;

    /** UI */
    private UITreeTableColumn<Order, Num> price;

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
                    $(amount, S.Narrow);
                });
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        OrderManager orders = view.market().orders;
        orders.added.to(order -> {
            table.root.createItem(order).removeWhen(order.observeTerminating());
        });

        table.selectMultipleRows().render(table -> new CatalogRow()).context($ -> {
            Calculation<Boolean> ordersArePassive = table.selection().flatVariable(Order::state).isNot(ACTIVE);

            $.menu(Cancel).disableWhen(ordersArePassive).whenUserClick(e -> act(this::cancel));
        });

        date.header(Date).modelByVar(Order.class, o -> o.created).render((ui, item) -> ui.text(formatter.format(item)));
        side.header(SiDe).model(Order.class, Order::direction).render((ui, side) -> ui.text(side).styleOnly(TradeMateStyle.Side.of(side)));
        amount.header(Amount).model(Order.class, o -> o.remainingSize);
        price.header(Price).model(Order.class, o -> o.price.v);
    }

    /**
     * Cancel {@link OrderSet} or {@link Order}.
     * 
     * @param order
     */
    private void act(Consumer<Order> forOrder) {
        for (Order order : table.selection()) {
            forOrder.accept(order);
        }
    }

    /**
     * Cancel {@link Order}.
     * 
     * @param order
     */
    private void cancel(Order order) {
        Viewtify.inWorker(() -> {
            view.market().cancel(order).to(o -> {
            });
        });
    }

    /**
     * @version 2018/12/08 14:55:02
     */
    private class CatalogRow extends TreeTableRow<Order> {

        /** The enhanced ui. */
        private final UserInterface ui = Viewtify.wrap(this, OrderCatalog.this);

        /**
         * 
         */
        private CatalogRow() {
            ui.styleOnly(Viewtify.signalNow(itemProperty()).skipNull().switchVariable(Order::state).map(S.State::of));
        }
    }

    /**
     * @version 2018/09/07 14:14:11
     */
    private interface S extends StyleDSL {

        Style Root = () -> {
            display.width(400, px).minHeight(300, px);
            text.unselectable();
        };

        ValueStyle<OrderState> State = state -> {
            switch (state) {
            case REQUESTING:
                $.descendant(() -> {
                    font.color($.rgb(80, 80, 80));
                });
                break;

            default:
                break;
            }
        };

        Style Wide = () -> {
            display.width(120, px);
        };

        Style Narrow = () -> {
            display.width(65, px);
        };
    }
}
