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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;

import cointoss.Order;
import cointoss.Order.State;
import cointoss.OrderSet;
import cointoss.Side;
import cointoss.util.Num;
import kiss.Disposable;
import trademate.TradingView;
import viewtify.View;
import viewtify.Viewtify;
import viewtify.ui.UI;
import viewtify.ui.UIContextMenu;
import viewtify.ui.UIMenuItem;
import viewtify.ui.UITreeTableColumn;

/**
 * @version 2017/11/26 14:05:31
 */
public class OrderCatalog extends View {

    /** The date formatter. */
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");

    /** UI */
    private @FXML TreeTableView<Object> requestedOrders;

    /** UI */
    private @FXML UITreeTableColumn<Object, ZonedDateTime> requestedOrdersDate;

    /** UI */
    private @FXML UITreeTableColumn<Object, Side> requestedOrdersSide;

    /** UI */
    private @FXML UITreeTableColumn<Object, Num> requestedOrdersAmount;

    /** UI */
    private @FXML UITreeTableColumn<Object, Num> requestedOrdersPrice;

    /** Parent View */
    private @FXML TradingView view;

    /** The root item. */
    private final TreeItem<Object> root = new TreeItem();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        requestedOrders.setRoot(root);
        requestedOrders.setShowRoot(false);
        requestedOrders.setRowFactory(table -> new OrderStateRow());
        requestedOrdersDate.provideProperty(OrderSet.class, OrderSet::date)
                .provideVariable(Order.class, o -> o.child_order_date)
                .render((ui, item) -> ui.text(formatter.format(item)));
        requestedOrdersSide.provideProperty(OrderSet.class, OrderSet::side)
                .provideValue(Order.class, Order::side)
                .render((ui, item) -> ui.text(item).style(item));
        requestedOrdersAmount.provideProperty(OrderSet.class, OrderSet::amount).provideValue(Order.class, o -> o.size);
        requestedOrdersPrice.provideProperty(OrderSet.class, OrderSet::averagePrice).provideValue(Order.class, o -> o.price);
    }

    /**
     * @param set
     */
    public void add(OrderSet set) {
        TreeItem item;

        if (set.sub.size() == 1) {
            Order order = set.sub.get(0);
            item = new TreeItem(order);

            order.cancel.on(Viewtify.UIThread).to(o -> {
                root.getChildren().remove(item);
            });
        } else {
            item = new TreeItem(set);
            item.setExpanded(true);

            // create sub orders for UI
            for (Order order : set.sub) {
                TreeItem subItem = new TreeItem(order);
                item.getChildren().add(subItem);

                order.cancel.on(Viewtify.UIThread).to(o -> {
                    item.getChildren().remove(subItem);

                    if (item.getChildren().isEmpty()) {
                        root.getChildren().remove(item);
                    }
                });
            }
        }
        root.getChildren().add(item);
    }

    /**
     * Cancel the specified {@link Order}.
     * 
     * @param order
     */
    private void cancel(Order order) {
        order.state.set(State.REQUESTING);

        Viewtify.inWorker(() -> {
            view.market().cancel(order).to(o -> {
                view.console.write("{} is canceled.", order);
            });
        });
    }

    /**
     * @version 2017/11/27 14:59:36
     */
    private class OrderStateRow extends TreeTableRow<Object> {

        /** The enhanced ui. */
        private final UI ui = Viewtify.wrap(this, OrderCatalog.this);

        /** The bind manager. */
        private Disposable bind = Disposable.empty();

        /** Context Menu */
        private final UIMenuItem cancel = UI.menuItem().label("Cancel").whenUserClick(e -> {
            Object item = getItem();

            if (item instanceof Order) {
                cancel((Order) item);
            } else if (item instanceof OrderSet) {
                for (Order sub : ((OrderSet) item).sub) {
                    cancel(sub);
                }
            }
        });

        /** Context Menu */
        private final UIContextMenu context = UI.contextMenu().item(cancel);

        /**
         * 
         */
        private OrderStateRow() {
            itemProperty().addListener((s, o, n) -> {
                if (o instanceof Order) {
                    bind.dispose();
                    ui.unstyle(Side.class);
                }

                if (n instanceof Order) {
                    bind = ((Order) n).state.observeNow().to(ui::style);
                }
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setContextMenu(null);
            } else {
                setContextMenu(context.ui);
            }
        }
    }
}
