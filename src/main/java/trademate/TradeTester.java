/*
 * Copyright (C) 2023 The COINTOSS Development Team
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
import trademate.order.OrderView;
import trademate.setting.AppearanceSetting;
import trademate.setting.BitFlyerSetting;
import trademate.setting.NotificatorSetting;
import trademate.verify.BackTestView;
import viewtify.Theme;
import viewtify.Viewtify;
import viewtify.keys.KeyBindingSettingView;
import viewtify.preference.PreferenceView;
import viewtify.ui.UITab;
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
                .add(AppearanceSetting.class, KeyBindingSettingView.class, NotificatorSetting.class, BitFlyerSetting.class, UpdateSettingView.class);

        DockSystem.register("BackTest").contents(BackTestView.class).closable(false);
        DockSystem.register("Setting").contents(preferences).closable(false);
        DockSystem.register("Order").contents(OrderView.class).closable(false);
        // DockSystem.register("Summary").contents(SummaryView.class).closable(false);
        // DockSystem.register("Global").contents(GlobalVolumeView.class).closable(false);

        MarketServiceProvider.availableMarketServices()
                .take(MarketService::supportHistoricalTrade)
                .take(e -> e.exchange == Exchange.BitFlyer)
                .skip(5)
                .to(service -> {
                    UITab tab = DockSystem.register(service.id)
                            .closable(false)
                            .text(service.id)
                            .contentsLazy(ui -> new TradingView(ui, service));

                    TradingViewCoordinator.requestLoading(service, tab);
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
        Viewtify.application()
                .error(I::error)
                .use(Theme.Dark)
                .icon("icon/tester.png")
                .onTerminating(EfficientWebSocket::shutdownNow)
                .activate(TradeTester.class);
    }
}