/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.visual.mate.order;

import javafx.fxml.FXML;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

import kiss.I;
import viewtify.View;

/**
 * @version 2017/11/26 14:05:31
 */
class OrderState extends View {

    private final OrderManager manager = I.make(OrderManager.class);

    /** UI */
    private @FXML TreeTableView<Object> requestedOrders;

    /** UI */
    private @FXML TreeTableColumn<Object, String> requestedOrdersDate;

    /** UI */
    private @FXML TreeTableColumn<Object, String> requestedOrdersSide;

    /** UI */
    private @FXML TreeTableColumn<Object, String> requestedOrdersAmount;

    /** UI */
    private @FXML TreeTableColumn<Object, String> requestedOrdersPrice;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {

    }
}
