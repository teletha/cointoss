/*
 * Copyright (C) 2023 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate;

import java.util.concurrent.TimeUnit;

import cointoss.MarketService;
import cointoss.market.MarketServiceProvider;
import kiss.I;
import trademate.order.OrderView;
import trademate.setting.PerformanceSetting;
import trademate.setting.SettingView;
import trademate.verify.BackTestView;
import viewtify.Viewtify;
import viewtify.keys.Key;
import viewtify.preference.Preferences;
import viewtify.ui.UIContextMenu;
import viewtify.ui.UITab;
import viewtify.ui.dock.Dock;
import viewtify.ui.dock.DockProvider;
import viewtify.ui.dock.DockSystem;
import viewtify.ui.dock.TypedDock;

public class TradeMateDockProvider extends DockProvider {

    public final Dock order = Dock.with.view(OrderView.class);

    public final Dock tester = Dock.with.view(BackTestView.class).showOnInitial();

    public final Dock volumes = Dock.with.view(GlobalVolumeView.class).showOnInitial();

    public final Dock summary = Dock.with.view(SummaryView.class).showOnInitial();

    public final Dock setting = Dock.with.view(SettingView.class).location(o -> o.window(750, 600, false));

    public final TypedDock<MarketService> trade = TypedDock.<MarketService> with().id("Trade").registration((tab, service) -> {
        tab.text(service.id).contentsLazy(ui -> new TradingView(tab, service));

        updateTabRealtimely(tab, service);
    });

    public TradeMateDockProvider() {
        TradeMateCommand.OpenBacktest.shortcut(Key.F11).contribute(tester::show);
        TradeMateCommand.OpenSetting.shortcut(Key.F12).contribute(setting::show);
    }

    /**
     * Show the current price and more market info.
     * 
     * @param tab
     * @param service
     */
    private void updateTabRealtimely(UITab tab, MarketService service) {
        tab.style("multiline");

        PerformanceSetting performance = Preferences.of(PerformanceSetting.class);

        service.executionsRealtimely()
                .startWith(service.executionLatest())
                .throttle(performance.refreshRate, TimeUnit.MILLISECONDS)
                .diff()
                .on(Viewtify.UIThread)
                .to(e -> tab.text(service.id + "\n" + e.price), service);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void hookMenu(UIContextMenu menus) {
        menus.menu(I.translate("Open market"), sub -> {
            MarketServiceProvider.availableProviders().to(provider -> {
                sub.menu(provider.exchange().name(), nest -> {
                    provider.markets().stream().forEach(service -> {
                        nest.menu(service.marketName).disableWhen(DockSystem.isOpened("Trade " + service.id)).action(() -> {
                            trade.show(service);
                        });
                    });
                });
            });
        });
    }
}
