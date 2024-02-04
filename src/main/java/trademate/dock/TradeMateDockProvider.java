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

import org.controlsfx.glyphfont.FontAwesome;

import cointoss.MarketService;
import cointoss.market.MarketServiceProvider;
import kiss.I;
import trademate.GlobalVolumeView;
import trademate.SummaryView;
import trademate.TradingView;
import trademate.TradingViewCoordinator;
import trademate.order.OrderView;
import trademate.setting.SettingView;
import trademate.verify.BackTestView;
import viewtify.Viewtify;
import viewtify.ui.dock.Dock;
import viewtify.ui.dock.DockProvider;
import viewtify.ui.dock.DockSystem;
import viewtify.ui.dock.TypedDock;

public class TradeMateDockProvider extends DockProvider {

    public final Dock order = Dock.of(OrderView.class);

    public final Dock tester = Dock.of(BackTestView.class);

    public final Dock volumes = Dock.of(GlobalVolumeView.class);

    public final Dock summary = Dock.of(SummaryView.class);

    public final Dock setting = Dock.of(SettingView.class);

    public final TypedDock<MarketService> trade = TypedDock.<MarketService> with().id("Trade").registration((tab, service) -> {
        tab.text(service.id).contentsLazy(ui -> new TradingView(tab, service));

        TradingViewCoordinator.requestLoading(service, tab);
    });

    /**
     * Register dock menu.
     */
    public void registerMenu() {
        DockSystem.registerMenu(icon -> {
            icon.text(FontAwesome.Glyph.BARS).behaveLikeButton().context(menus -> {
                menus.menu(I.translate("Open new page"), sub -> {
                    for (Dock item : queryIndependentDocks()) {
                        sub.menu(item.title()).disableWhen(DockSystem.isOpened(item.id())).action(item::register);
                    }
                });
                menus.menu(I.translate("Open market"), sub -> {
                    MarketServiceProvider.availableProviders().to(provider -> {
                        sub.menu(provider.exchange().name(), nest -> {
                            provider.markets().forEach(service -> {
                                nest.menu(service.marketName).disableWhen(DockSystem.isOpened("Trade " + service.id)).action(() -> {
                                    trade.register(service);
                                });
                            });
                        });
                    });
                });
                menus.separator();
                menus.menu(I.translate("Reboot")).action(Viewtify.application()::reactivate);
                menus.menu(I.translate("Exit")).action(Viewtify.application()::deactivate);
            });
        });
    }

    /**
     * 
     */
    public void registerLayout() {
        DockSystem.initializeLayout(() -> {
            tester.register();
            volumes.register();
            summary.register();
        });
    }
}
