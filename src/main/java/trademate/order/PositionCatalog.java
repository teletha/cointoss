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

import cointoss.Order;
import cointoss.Side;
import cointoss.util.Num;
import trademate.TradingView;
import viewtify.UI;
import viewtify.View;
import viewtify.ui.UITreeItem;
import viewtify.ui.UITreeTableColumn;
import viewtify.ui.UITreeTableView;

/**
 * @version 2017/12/20 14:37:27
 */
public class PositionCatalog extends View {

    /** The date formatter. */
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");

    /** UI */
    private @UI UITreeTableView<Order> openPositionCatalog;

    /** UI */
    private @UI UITreeTableColumn<Object, ZonedDateTime> openPositionDate;

    /** UI */
    private @UI UITreeTableColumn<Object, Side> openPositionSide;

    /** UI */
    private @UI UITreeTableColumn<Object, Num> openPositionAmount;

    /** UI */
    private @UI UITreeTableColumn<Object, Num> openPositionPrice;

    /** Parent View */
    private @UI TradingView view;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        openPositionDate.provideProperty(OrderSet.class, o -> o.date)
                .provideVariable(Order.class, o -> o.child_order_date)
                .render((ui, item) -> ui.text(formatter.format(item)));
        openPositionSide.provideProperty(OrderSet.class, o -> o.side)
                .provideValue(Order.class, Order::side)
                .render((ui, item) -> ui.text(item).style(item));
        openPositionAmount.provideProperty(OrderSet.class, o -> o.amount).provideVariable(Order.class, o -> o.outstanding_size);
        openPositionPrice.provideProperty(OrderSet.class, o -> o.averagePrice).provideValue(Order.class, o -> o.price);
    }

    /**
     * Create tree item for executed {@link OrderSet}.
     * 
     * @param set
     */
    public void createPositionItem(OrderSet set) {
        if (set.sub.size() == 1) {
            createPositionItem(openPositionCatalog.root, set.sub.get(0));
        } else {
            UITreeItem item = openPositionCatalog.root.createItem(set).expand(true).removeWhenEmpty();

            for (Order order : set.sub) {
                createPositionItem(item, order);
            }
        }
    }

    /**
     * Create tree item for executed {@link Order}.
     */
    private void createPositionItem(UITreeItem item, Order order) {
        item.createItem(order).removeWhen(order.state.observe().take(CANCELED, COMPLETED));
    }
}
