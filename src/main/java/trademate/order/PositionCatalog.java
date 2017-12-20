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

import javafx.fxml.FXML;

import cointoss.Order;
import cointoss.Side;
import cointoss.util.Num;
import viewtify.View;
import viewtify.ui.UITreeTableColumn;
import viewtify.ui.UITreeTableView;

/**
 * @version 2017/12/20 14:37:27
 */
public class PositionCatalog extends View {

    /** UI */
    private @FXML UITreeTableView<Order> openPositionCatalog;

    /** UI */
    private @FXML UITreeTableColumn<Order, ZonedDateTime> openPositionDate;

    /** UI */
    private @FXML UITreeTableColumn<Order, Side> openPositionSide;

    /** UI */
    private @FXML UITreeTableColumn<Order, Num> openPositionAmount;

    /** UI */
    private @FXML UITreeTableColumn<Order, Num> openPositionPrice;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
    }
}
