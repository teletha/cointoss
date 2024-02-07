/*
 * Copyright (C) 2023 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.dock;

import cointoss.MarketService;
import cointoss.market.MarketServiceProvider;
import trademate.GlobalVolumeView;
import trademate.SummaryView;
import trademate.TradingView;
import trademate.TradingViewCoordinator;
import trademate.order.OrderView;
import trademate.setting.SettingView;
import trademate.verify.BackTestView;
import viewtify.ui.dock.Dock;
import viewtify.ui.dock.DockProvider;
import viewtify.ui.dock.TypedDock;

public class TradeMateDockProvider extends DockProvider {

    public final Dock order = Dock.with.view(OrderView.class);

    public final Dock tester = Dock.with.view(BackTestView.class).showOnInitial();

    public final Dock volumes = Dock.with.view(GlobalVolumeView.class).showOnInitial().location(loc -> loc.left().ratio(0.3));

    public final Dock summary = Dock.with.view(SummaryView.class).showOnInitial();

    public final Dock setting = Dock.with.view(SettingView.class);

    public final TypedDock<MarketService> trade = TypedDock.<MarketService> with().id("Trade").registration((tab, service) -> {
        tab.text(service.id).contentsLazy(ui -> new TradingView(tab, service));

        TradingViewCoordinator.requestLoading(service, tab);
    }).showOnInitial(MarketServiceProvider.by("Binance BTCUSDT").exact());
}
