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

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.function.Function;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.util.Callback;

import cointoss.Order;
import cointoss.OrderSet;
import cointoss.OrderState;
import cointoss.Side;
import kiss.Disposable;
import trademate.TradingView;
import viewtify.View;
import viewtify.Viewtify;
import viewtify.ui.UI;
import viewtify.ui.UIContextMenu;
import viewtify.ui.UIMenuItem;

/**
 * @version 2017/11/26 14:05:31
 */
public class OrderCatalog extends View {

    /** UI */
    private @FXML TreeTableView<Object> requestedOrders;

    /** UI */
    private @FXML TreeTableColumn<Object, Object> requestedOrdersDate;

    /** UI */
    private @FXML TreeTableColumn<Object, Object> requestedOrdersSide;

    /** UI */
    private @FXML TreeTableColumn<Object, Object> requestedOrdersAmount;

    /** UI */
    private @FXML TreeTableColumn<Object, Object> requestedOrdersPrice;

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
        requestedOrdersDate.setCellValueFactory(new OrderStateValueCell(OrderSet::date, Order::childOrderDate));
        requestedOrdersDate.setCellFactory(t -> new DateCell());
        requestedOrdersSide.setCellValueFactory(new OrderStateValueCell(OrderSet::side, o -> new SimpleObjectProperty(o.side)));
        requestedOrdersSide.setCellFactory(table -> new OrderStateCell());
        requestedOrdersAmount.setCellValueFactory(new OrderStateValueCell(OrderSet::amount, o -> new SimpleObjectProperty(o.size)));
        requestedOrdersPrice.setCellValueFactory(new OrderStateValueCell(OrderSet::averagePrice, o -> new SimpleObjectProperty(o.price)));
    }

    /**
     * @param set
     */
    public void add(OrderSet set) {
        TreeItem item;

        if (set.sub.size() == 1) {
            Order order = set.sub.get(0);
            item = new TreeItem(order);

            order.cancel.to(o -> {
                root.getChildren().remove(item);
            });
        } else {
            item = new TreeItem(set);
            item.setExpanded(true);

            // create sub orders for UI
            for (Order order : set.sub) {
                TreeItem subItem = new TreeItem(order);
                item.getChildren().add(subItem);

                order.cancel.to(o -> {
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
        order.child_order_state.set(OrderState.REQUESTING);

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
                    bind = ((Order) n).child_order_state.observeNow().to(ui::style);
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

    /**
     * @version 2017/11/26 12:45:18
     */
    private static class OrderStateValueCell
            implements Callback<TreeTableColumn.CellDataFeatures<Object, Object>, ObservableValue<Object>> {

        /** The value converter. */
        private final Function<OrderSet, ObservableValue> forSet;

        /** The value converter. */
        private final Function<Order, ObservableValue> forOrder;

        /**
         * @param forSet
         * @param forOrder
         */
        private OrderStateValueCell(Function<OrderSet, ObservableValue> forSet, Function<Order, ObservableValue> forOrder) {
            this.forSet = forSet;
            this.forOrder = forOrder;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ObservableValue<Object> call(CellDataFeatures<Object, Object> features) {
            Object value = features.getValue().getValue();

            if (value instanceof OrderSet) {
                return forSet.apply((OrderSet) value);
            } else {
                return forOrder.apply((Order) value);
            }
        }
    }

    /**
     * @version 2017/11/27 17:12:43
     */
    private class OrderStateCell extends TreeTableCell<Object, Object> {

        /** The enhanced ui. */
        private final UI ui = Viewtify.wrap(this, OrderCatalog.this);

        /**
         * {@inheritDoc}
         */
        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
            } else {
                setText(item.toString());

                if (item instanceof Side) {
                    ui.style((Side) item);
                }
            }
        }
    }

    /**
     * @version 2017/12/01 23:30:30
     */
    private static class DateCell extends TreeTableCell<Object, Object> {

        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");

        /**
         * {@inheritDoc}
         */
        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
            } else {
                if (item instanceof TemporalAccessor) {
                    setText(formatter.format((TemporalAccessor) item));
                }
            }
        }
    }
}
