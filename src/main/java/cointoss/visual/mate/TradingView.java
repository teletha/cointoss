/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.visual.mate;

import javafx.fxml.FXML;

import cointoss.market.bitflyer.BitFlyer;
import cointoss.visual.mate.board.MarketBoardView;
import cointoss.visual.mate.console.Console;
import cointoss.visual.mate.order.OrderCatalog;
import cointoss.visual.mate.order.OrderMaker;
import viewtify.View;

/**
 * @version 2017/11/29 10:50:06
 */
public class TradingView extends View {

    public final BitFlyer market;

    public @FXML ExecutionView executionView;

    public @FXML Console console;

    public @FXML MarketBoardView board;

    public @FXML OrderMaker maker;

    public @FXML OrderCatalog catalog;

    /**
     * 
     */
    public TradingView(BitFlyer market) {
        this.market = market;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String name() {
        return TradingView.class.getSimpleName() + View.IDSeparator + market.fullName();
    }
}
