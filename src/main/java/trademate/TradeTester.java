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

import java.util.LinkedList;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.market.bitfinex.Bitfinex;
import cointoss.market.ftx.FTX;
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

        // MarketService service = Binance.FUTURE_BTCUSD_210625;
        // UITab tab = DockSystem.register(service.id())
        // .closable(false)
        // .text(service.marketReadableName)
        // .contents(ui -> new TradingView(ui, service));
        //
        // tab.load();

        MarketService service2 = FTX.EOS_PERP;
        UITab tab = DockSystem.register(service2.id)
                .closable(false)
                .text(service2.marketName)
                .contents(ui -> new TradingView(ui, service2));

        tab.load();

        MarketService service3 = Bitfinex.ETH_USD;
        tab = DockSystem.register(service3.id).closable(false).text(service3.marketName).contents(ui -> new TradingView(ui, service3));

        tab.load();
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
     * Load in parallel for each exchange.
     */
    private static class LoadingQueue {

        private final LinkedList<UITab> tabs = new LinkedList();

        private UITab loading;

        /**
         * {@link TradeMate} will automatically initialize in the background if any tab has not been
         * activated yet.
         */
        private final synchronized void tryLoading() {
            if (loading == null && !tabs.isEmpty()) {
                loading = tabs.remove(0);
                Viewtify.inUI(loading::load);
            }
        }
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