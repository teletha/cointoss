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
import cointoss.market.bitflyer.BitFlyer;
import cointoss.market.gmo.GMO;
import cointoss.util.EfficientWebSocket;
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

    static {
        Viewtify.Terminator.add(EfficientWebSocket::shutdownNow);
    }

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
        MarketService service = BitFlyer.FX_BTC_JPY;
        UITab tab = DockSystem.register(service.marketIdentity())
                .closable(false)
                .text(service.marketReadableName)
                .contents(ui -> new TradingView(ui, service));

        tab.load();

        MarketService service2 = GMO.BTC_DERIVATIVE;
        tab = DockSystem.register(service2.marketIdentity())
                .closable(false)
                .text(service2.marketReadableName)
                .contents(ui -> new TradingView(ui, service2));

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