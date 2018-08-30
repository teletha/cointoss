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

import static cointoss.order.OrderState.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import javafx.scene.control.TreeTableRow;

import cointoss.Side;
import cointoss.order.Order;
import cointoss.order.OrderState;
import cointoss.util.Num;
import kiss.Variable;
import trademate.TradingView;
import viewtify.View;
import viewtify.Viewtify;
import viewtify.bind.Calculation;
import viewtify.dsl.Style;
import viewtify.dsl.StyleDSL;
import viewtify.dsl.UIDefinition;
import viewtify.ui.UITreeItem;
import viewtify.ui.UITreeTableColumn;
import viewtify.ui.UITreeTableView;
import viewtify.ui.UserInterface;

/**
 * @version 2018/08/30 16:54:37
 */
public class OrderCatalog extends View {

    /** The date formatter. */
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");

    /** UI */
    private UITreeTableView<Object> orderCatalog;

    /** UI */
    private UITreeTableColumn<Object, ZonedDateTime> requestedOrdersDate;

    /** UI */
    private UITreeTableColumn<Object, Side> requestedOrdersSide;

    /** UI */
    private UITreeTableColumn<Object, Num> requestedOrdersAmount;

    /** UI */
    private UITreeTableColumn<Object, Num> requestedOrdersPrice;

    /** Parent View */
    private TradingView view;

    /**
     * {@inheritDoc}
     */
    @Override
    protected UIDefinition declareUI() {
        return new UIDefinition() {
            {
                $(orderCatalog, S.OrderCatalog, () -> {
                    $(requestedOrdersDate, S.OrderCatalog1);
                    $(requestedOrdersSide, S.OrderCatalog2);
                    $(requestedOrdersPrice, S.OrderCatalog1);
                    $(requestedOrdersAmount, S.OrderCatalog3);
                });
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        orderCatalog.selectMultipleRows().render(table -> new CatalogRow()).context($ -> {
            Calculation<Boolean> ordersArePassive = orderCatalog.selection().flatVariable(this::state).isNot(ACTIVE);

            $.menu("Cancel").disableWhen(ordersArePassive).whenUserClick(e -> act(this::cancel));
        });

        requestedOrdersDate.modelByProperty(OrderSet.class, o -> o.date)
                .modelByVar(Order.class, o -> o.created)
                .render((ui, item) -> ui.text(formatter.format(item)));
        requestedOrdersSide.modelByProperty(OrderSet.class, o -> o.side)
                .model(Order.class, Order::side)
                .render((ui, item) -> ui.text(item).styleOnly(item));
        requestedOrdersAmount.modelByProperty(OrderSet.class, o -> o.amount).model(Order.class, o -> o.remainingSize);
        requestedOrdersPrice.modelByProperty(OrderSet.class, o -> o.averagePrice).model(Order.class, o -> o.price.v);
    }

    /**
     * Compute order state.
     * 
     * @param item
     * @return
     */
    private Variable<OrderState> state(Object item) {
        return item instanceof Order ? ((Order) item).state : Variable.of(ACTIVE);
    }

    /**
     * Create tree item for {@link OrderSet}.
     * 
     * @param set
     */
    public void createOrderItem(OrderSet set) {
        UITreeItem item = orderCatalog.root.createItem(set).expand(set.sub.size() != 1).removeWhenEmpty();

        for (Order order : set.sub) {
            createOrderItem(item, order);
        }
    }

    /**
     * Create tree item for {@link Order}.
     */
    private void createOrderItem(UITreeItem item, Order order) {
        item.createItem(order).removeWhen(order.observeTerminating());
    }

    /**
     * Cancel {@link OrderSet} or {@link Order}.
     * 
     * @param order
     */
    private void act(Consumer<Order> forOrder) {
        for (Object order : orderCatalog.selection()) {
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
            });
        });
    }

    /**
     * @version 2017/12/04 14:32:07
     */
    private class CatalogRow extends TreeTableRow<Object> {

        private final Calculation<OrderState> orderState = Viewtify.calculate(itemProperty()).flatVariable(o -> {
            if (o instanceof Order) {
                return ((Order) o).state;
            } else {
                return Variable.of(OrderState.ACTIVE);
            }
        });

        /** The enhanced ui. */
        private final UserInterface ui = Viewtify.wrap(this, OrderCatalog.this);

        // /** Context Menu */
        // private final UIContextMenu context = UI.contextMenu($ -> {
        // $.menu("Cancel").disableWhen(orderState.isNot(ACTIVE)).whenUserClick(e ->
        // cancel(getItem()));
        // });

        /**
         * 
         */
        private CatalogRow() {
            ui.styleOnly(orderState);

            // contextMenuProperty().bind(Viewtify.calculate(itemProperty()).map(v -> context.ui));
        }
    }

    /**
     * @version 2018/08/30 12:50:36
     */
    private static class S extends StyleDSL {

        static Style OrderCatalog = empty();

        static Style OrderCatalog1 = empty();

        static Style OrderCatalog2 = empty();

        static Style OrderCatalog3 = empty();
    }
}
