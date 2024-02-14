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

import cointoss.MarketService;
import cointoss.market.MarketServiceProvider;
import kiss.I;
import trademate.order.OrderView;
import trademate.setting.SettingView;
import trademate.verify.BackTestView;
import viewtify.ui.UIContextMenu;
import viewtify.ui.dock.Dock;
import viewtify.ui.dock.DockProvider;
import viewtify.ui.dock.DockSystem;
import viewtify.ui.dock.TypedDock;

public class TradeMateDockProvider extends DockProvider {

    public final Dock order = Dock.with.view(OrderView.class);

    public final Dock tester = Dock.with.view(BackTestView.class).showOnInitial();

    public final Dock volumes = Dock.with.view(GlobalVolumeView.class).showOnInitial();

    public final Dock summary = Dock.with.view(SummaryView.class).showOnInitial();

    public final Dock setting = Dock.with.view(SettingView.class);

    public final TypedDock<MarketService> trade = TypedDock.<MarketService> with().id("Trade").registration((tab, service) -> {
        tab.text(service.id).contentsLazy(ui -> new TradingView(tab, service));
    });

    /**
     * {@inheritDoc}
     */
    @Override
    protected void hookMenu(UIContextMenu menus) {
        menus.menu(I.translate("Open market"), sub -> {
            MarketServiceProvider.availableProviders().to(provider -> {
                sub.menu(provider.exchange().name(), nest -> {
                    provider.markets().stream().filter(MarketService::supportHistoricalTrade).forEach(service -> {
                        nest.menu(service.marketName).disableWhen(DockSystem.isOpened("Trade " + service.id)).action(() -> {
                            trade.show(service);
                        });
                    });
                });
            });
        });
    }
}
