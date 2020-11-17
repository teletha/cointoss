/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import org.apache.logging.log4j.LogManager;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.market.binance.Binance;
import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import trademate.order.OrderView;
import trademate.setting.SettingView;
import trademate.verify.BackTestView;
import viewtify.Theme;
import viewtify.Viewtify;
import viewtify.ui.UITab;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.dock.DockSystem;

@Managed(value = Singleton.class)
public class TradeTester extends View {

    /**
     * {@inheritDoc}
     */
    @Override
    protected ViewDSL declareUI() {
        return new ViewDSL() {
            {
                $(DockSystem.UI);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        DockSystem.register("BackTest").contents(BackTestView.class).closable(false);
        DockSystem.register("Setting").contents(SettingView.class).closable(false);
        DockSystem.register("Order").contents(OrderView.class).closable(false);

        // ========================================================
        // Create Tab for each Markets
        // ========================================================
        MarketService service = Binance.FUTURE_BTC_USDT;
        UITab tab = DockSystem.register(service.marketIdentity())
                .closable(false)
                .text(service.marketReadableName)
                .contents(ui -> new TradingView(ui, service));

        tab.load();
    }

    /**
     * Entry point.
     */
    public static void main(String[] args) {
        I.load(Market.class);

        // activate application
        Viewtify.application().logging(LogManager.getLogger()::error).use(Theme.Dark).icon("icon/tester.png").activate(TradeTester.class);
    }
}