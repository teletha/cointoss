/*
 * Copyright (C) 2024 The COINTOSS Development Team
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
import cointoss.market.Exchange;
import cointoss.market.MarketServiceProvider;
import cointoss.util.EfficientWebSocket;
import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import trademate.setting.AppearanceSetting;
import trademate.setting.BitFlyerSetting;
import trademate.setting.NotificatorSetting;
import viewtify.Viewtify;
import viewtify.keys.KeyBindingSettingView;
import viewtify.preference.PreferenceView;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.dock.DockSystem;
import viewtify.update.UpdateSettingView;

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
        PreferenceView preferences = new PreferenceView();
        preferences
                .manage(AppearanceSetting.class, KeyBindingSettingView.class, NotificatorSetting.class, BitFlyerSetting.class, UpdateSettingView.class);

        // DockSystem.register("BackTest").contentsLazy(BackTestView.class).text(en("BackTest")).closable(false);
        DockSystem.register("Setting").contentsLazy(tab -> preferences).text(en("Setting")).closable(false);
        // DockSystem.register("Order").contentsLazy(OrderView.class).text(en("Order")).closable(false);
        DockSystem.register("Summary").contentsLazy(SummaryView.class).text("一覧").closable(false);
        // DockSystem.register("Global").contentsLazy(GlobalVolumeView.class).text("流動性").closable(false);

        MarketServiceProvider.availableMarketServices()
                .take(MarketService::supportHistoricalTrade)
                .take(e -> e.exchange == Exchange.Binance)
                .take(1)
                .to(service -> {
                    // UITab tab = DockSystem.register(service.id)
                    // .closable(false)
                    // .text(service.id)
                    // .contentsLazy(ui -> new TradingView(ui, service));
                    //
                    // TradingViewCoordinator.requestLoading(service, tab);
                });

        DockSystem.validate();
    }

    /**
     * Entry point.
     * 
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        I.load(Market.class);

        // activate application
        Viewtify.application() //
                .error((msg, error) -> {
                    I.error(msg);
                    I.error(error);
                })
                .icon("icon/tester.png")
                .onTerminating(EfficientWebSocket::shutdownNow)
                .activate(TradeTester.class);
    }
}