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

import java.util.List;
import java.util.Random;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.market.MarketServiceProvider;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import kiss.I;
import kiss.Managed;
import kiss.Signal;
import kiss.Singleton;
import psychopath.Locator;
import trademate.order.OrderView;
import trademate.setting.AppearanceSetting;
import trademate.setting.BitFlyerSetting;
import trademate.setting.NotificatorSetting;
import trademate.verify.BackTestView;
import viewtify.Viewtify;
import viewtify.keys.KeyBindingSettingView;
import viewtify.preference.PreferenceView;
import viewtify.ui.UITab;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.dock.DockSystem;
import viewtify.update.UpdateSettingView;

@Managed(value = Singleton.class)
public class TradeMate extends View {

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

        DockSystem.register("Setting").contents(preferences).closable(false).text(en("Setting"));
        DockSystem.register("BackTest").contents(BackTestView.class).closable(false).text(en("Back Test"));
        DockSystem.register("Global Volume").contents(GlobalVolumeView.class).closable(false).text(en("Global Volume"));
        DockSystem.register("Order").contents(OrderView.class).closable(false).text(en("Order"));

        // ========================================================
        // Create Tab for each Markets
        // ========================================================
        MarketServiceProvider.availableMarketServices().take(MarketService::supportHistoricalTrade).to(service -> {
            UITab tab = DockSystem.register(service.id).closable(false).text(service.id).contentsLazy(ui -> new TradingView(ui, service));

            TradingViewCoordinator.requestLoading(service, tab);
        });
        DockSystem.validate();

        // ========================================================
        // Clock in Title bar
        // ========================================================
        Chrono.seconds().map(Chrono.DateDayTime::format).combineLatest(Wisdom.random()).on(Viewtify.UIThread).to(v -> {
            stage().v.setTitle(v.ⅰ.substring(5) + "  " + v.ⅱ);
        });
    }

    /**
     * Managing words of widsom.
     */
    private static class Wisdom {

        private static final List<String> words = Locator.file("wisdom.txt").lines().toList();

        private static final Random random = new Random();

        private static Signal<String> random() {
            return Chrono.minutes().map(v -> words.get(random.nextInt(words.size())));
        }
    }

    /**
     * Entry point.
     */
    public static void main(String[] args) {
        I.load(Market.class);

        // activate application
        Viewtify.application() //
                .error((msg, error) -> {
                    I.error(msg);
                    I.error(error);
                })
                .icon("icon/app.png")
                .onTerminating(EfficientWebSocket::shutdownNow)
                .activate(TradeMate.class);
    }
}