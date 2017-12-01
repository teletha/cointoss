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

import cointoss.Market;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.visual.mate.console.Console;
import cointoss.visual.mate.order.OrderBuilder;
import cointoss.visual.mate.order.OrderCatalog;
import viewtify.View;
import viewtify.Viewtify;

/**
 * @version 2017/11/29 10:50:06
 */
public class TradingView extends View<MainView> {

    public final BitFlyer provider;

    public @FXML ExecutionView executionView;

    public @FXML Console console;

    public @FXML OrderBookView board;

    public @FXML OrderBuilder builder;

    public @FXML OrderCatalog catalog;

    /** Market cache. */
    private Market market;

    /**
     *
     */
    public TradingView(BitFlyer provider) {
        this.provider = provider;
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
        return TradingView.class.getSimpleName() + View.IDSeparator + provider.fullName();
    }

    /**
     * Retrieve the associated market.
     * 
     * @return
     */
    public final synchronized Market market() {
        if (market == null) {
            Viewtify.Terminator.add(market = new Market(provider.service(), provider.log().fromToday()));
        }
        return market;
    }
}
