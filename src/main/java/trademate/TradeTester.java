/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.market.MarketServiceProvider;
import cointoss.util.EfficientWebSocket;
import kiss.I;
import kiss.Managed;
import kiss.Singleton;
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
        // DockSystem.register("Order").contents(OrderView.class).closable(false);
        // DockSystem.register("Summary").contents(SummaryView.class).closable(false);
        // DockSystem.register("Global").contents(GlobalVolumeView.class).closable(false);

        MarketServiceProvider.availableMarketServices().take(MarketService::supportHistoricalTrade).take(5).to(service -> {
            UITab tab = DockSystem.register(service.id).closable(false).text(service.id).contents(ui -> new TradingView(ui, service));

            TradingViewCoordinator.requestLoading(service, tab);
        });

        //
        // MarketService service4 = GMO.XRP;
        // tab = DockSystem.register(service4.id())
        // .closable(false)
        // .text(service4.marketReadableName)
        // .contents(ui -> new TradingView(ui, service4));
        //
        // tab.load();
        //
        // MarketService service5 = Bitbank.XLM_JPY;
        // tab = DockSystem.register(service5.id())
        // .closable(false)
        // .text(service5.marketReadableName)
        // .contents(ui -> new TradingView(ui, service5));
        //
        // tab.load();
    }

    /**
     * Entry point.
     */
    public static void main(String[] args) {
        I.load(Market.class);

        // activate application
        Viewtify.application().logging(I::error).use(Theme.Dark).icon("icon/tester.png").activate(TradeTester.class);
    }
}