/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.order;

import static trademate.CommonText.*;

import java.util.Comparator;
import java.util.function.Consumer;

import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableRow;

import cointoss.Direction;
import cointoss.order.Order;
import cointoss.order.OrderState;
import cointoss.util.arithmetic.Num;
import kiss.I;
import stylist.Style;
import stylist.StyleDSL;
import stylist.ValueStyle;
import trademate.ChartTheme;
import trademate.TradingView;
import viewtify.Viewtify;
import viewtify.ui.UITableColumn;
import viewtify.ui.UITableView;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.helper.StyleHelper;
import viewtify.ui.helper.User;

public class OrderCatalog extends View {

    /** UI */
    private UITableView<Order> table;

    /** UI */
    private UITableColumn<Order, Direction> side;

    /** UI */
    private UITableColumn<Order, Num> amount;

    /** UI */
    private UITableColumn<Order, Num> price;

    /** Parent View */
    private TradingView view;

    /**
     * {@inheritDoc}
     */
    @Override
    protected ViewDSL declareUI() {
        return new ViewDSL() {
            {
                $(table, S.Root, () -> {
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
        table.mode(SelectionMode.MULTIPLE).render(table -> new CatalogRow()).context($ -> {
            $.menu().text(Cancel).when(User.Action, e -> act(this::cancel));
        });

        side.text(Side).model(Order.class, Order::direction).render((label, order, side) -> label.text(side).color(ChartTheme.colorBy(side)));
        amount.text(Amount).modelByVar(Order.class, o -> o.observeExecutedSize().map(s -> o.size.minus(s)).to());
        price.text(Price).model(Order.class, o -> o.price);

        // initialize orders on server
        I.signal(view.market.orders.items).take(Order::isBuy).sort(Comparator.reverseOrder()).to(this::createOrderItem);
        I.signal(view.market.orders.items).take(Order::isSell).sort(Comparator.naturalOrder()).to(this::createOrderItem);

        // observe orders on clinet
        view.market.orders.added.to(this::createOrderItem);
    }

    /**
     * Create tree item for {@link OrderSet}.
     * 
     * @param set
     */
    private void createOrderItem(Order order) {
        if (order != null) {
            table.addItemAtLast(order);
            order.observeTerminating().on(Viewtify.UIThread).to(() -> table.removeItem(order));
        }
    }

    /**
     * Cancel {@link OrderSet} or {@link Order}.
     * 
     * @param order
     */
    private void act(Consumer<Order> forOrder) {
        for (Order order : table.selectedItems()) {
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
            view.market.cancel(order).to(o -> {
            });
        });
    }

    /**
     * 
     */
    private class CatalogRow extends TableRow<Order> implements StyleHelper<CatalogRow, CatalogRow> {

        /**
         * 
         */
        private CatalogRow() {
            styleOnly(Viewtify.observing(itemProperty()).as(Order.class).switchMap(o -> o.observeStateNow()).map(S.State::of));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CatalogRow ui() {
            return this;
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