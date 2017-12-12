/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.order;

import static cointoss.Order.State.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.scene.control.TreeTableRow;

import cointoss.Order;
import cointoss.Order.State;
import cointoss.Side;
import cointoss.util.Num;
import kiss.Variable;
import trademate.TradingView;
import viewtify.View;
import viewtify.Viewtify;
import viewtify.bind.Calculation;
import viewtify.ui.UI;
import viewtify.ui.UISpinner;
import viewtify.ui.UITreeItem;
import viewtify.ui.UITreeTableColumn;
import viewtify.ui.UITreeTableView;

/**
 * @version 2017/11/26 14:05:31
 */
public class OrderCatalog extends View {

    /** The date formatter. */
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");

    /** UI */
    private @FXML UITreeTableView<Object> orderCatalog;

    /** UI */
    private @FXML UITreeTableColumn<Object, ZonedDateTime> requestedOrdersDate;

    /** UI */
    private @FXML UITreeTableColumn<Object, Side> requestedOrdersSide;

    /** UI */
    private @FXML UITreeTableColumn<Object, Num> requestedOrdersAmount;

    /** UI */
    private @FXML UITreeTableColumn<Object, Num> requestedOrdersPrice;

    /** UI */
    private @FXML UISpinner<Num> optimizeThreshold;

    /** Parent View */
    private @FXML TradingView view;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        orderCatalog.selectMultipleRows().render(table -> new CatalogRow()).context($ -> {
            Calculation<Boolean> ordersArePassive = orderCatalog.getSelected().flatVariable(this::state).isNot(ACTIVE);

            $.menu("Cancel").disableWhen(ordersArePassive).whenUserClick(e -> act(this::cancel));
            $.menu("Get Close").disableWhen(ordersArePassive).whenUserClick(e -> act(this::reorderClosely));
            $.menu("Get Away").disableWhen(ordersArePassive).whenUserClick(e -> act(this::reorderAway));
        });

        requestedOrdersDate.provideProperty(OrderSet.class, o -> o.date)
                .provideVariable(Order.class, o -> o.child_order_date)
                .render((ui, item) -> ui.text(formatter.format(item)));
        requestedOrdersSide.provideProperty(OrderSet.class, o -> o.side)
                .provideValue(Order.class, Order::side)
                .render((ui, item) -> ui.text(item).style(item));
        requestedOrdersAmount.provideProperty(OrderSet.class, o -> o.amount).provideVariable(Order.class, o -> o.outstanding_size);
        requestedOrdersPrice.provideProperty(OrderSet.class, o -> o.averagePrice).provideValue(Order.class, o -> o.price);
    }

    /**
     * Compute order state.
     * 
     * @param item
     * @return
     */
    private Variable<State> state(Object item) {
        return item instanceof Order ? ((Order) item).state : Variable.of(ACTIVE);
    }

    /**
     * Add {@link OrderSet}.
     * 
     * @param set
     */
    public void add(OrderSet set) {
        if (set.sub.size() == 1) {
            orderCatalog.createItem(set.sub.get(0)).removeWhen(o -> o.state.observe().take(CANCELED));
        } else {
            UITreeItem item = orderCatalog.createItem(set).expand(true).removeWhenEmpty();

            for (Order order : set.sub) {
                item.createItem(order).removeWhen(order.state.observe().take(CANCELED));
            }
        }
    }

    /**
     * Cancel {@link OrderSet} or {@link Order}.
     * 
     * @param order
     */
    private void act(Consumer<Order> forOrder) {
        for (Object order : orderCatalog.getSelected().getValue()) {
            if (order instanceof Order) {
                forOrder.accept((Order) order);
            } else {
                for (Order child : ((OrderSet) order).sub) {
                    forOrder.accept(child);
                }
            }
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
                view.console.write("{} is canceled.", order);
            });
        });
    }

    /**
     * Reorder {@link Order}.
     * 
     * @param order
     */
    private void reorderClosely(Order order) {
        Viewtify.inWorker(() -> {
            Num price = view.board.book.computeBestPrice(order.side, order.price, optimizeThreshold.value(), Num.of(2));

            view.market().cancel(order).to(o -> {
                view.console.write("{} is canceled.", o);

                view.market().request(Order.limit(order.side, order.size, price)).to(re -> {
                    view.console.write("{} is reorder.", re);
                });
            });
        });
    }

    /**
     * Reorder {@link Order}.
     * 
     * @param order
     */
    private void reorderAway(Order order) {
        Viewtify.inWorker(() -> {
            view.market().cancel(order).to(o -> {
                view.console.write("{} is canceled.", order);
            });
        });
    }

    /**
     * @version 2017/12/04 14:32:07
     */
    private class CatalogRow extends TreeTableRow<Object> {

        private final Calculation<State> orderState = Viewtify.calculate(itemProperty()).flatVariable(o -> {
            if (o instanceof Order) {
                return ((Order) o).state;
            } else {
                return Variable.of(State.ACTIVE);
            }
        });

        /** The enhanced ui. */
        private final UI ui = Viewtify.wrap(this, OrderCatalog.this);

        // /** Context Menu */
        // private final UIContextMenu context = UI.contextMenu($ -> {
        // $.menu("Cancel").disableWhen(orderState.isNot(ACTIVE)).whenUserClick(e ->
        // cancel(getItem()));
        // });

        /**
         * 
         */
        private CatalogRow() {
            ui.style(orderState);

            // contextMenuProperty().bind(Viewtify.calculate(itemProperty()).map(v -> context.ui));
        }
    }
}
