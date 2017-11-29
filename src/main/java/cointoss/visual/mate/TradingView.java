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

    private final BitFlyer market;

    private @FXML ExecutionView executionView;

    private @FXML Console console;

    private @FXML MarketBoardView board;

    private @FXML OrderMaker maker;

    private @FXML OrderCatalog catalog;

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
    public String id() {
        return TradingView.class.getSimpleName() + "-" + market.fullName();
    }
}
