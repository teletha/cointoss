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
import kiss.Variable;
import trademate.GlobalVolumeView;
import trademate.SummaryView;
import trademate.TradingView;
import trademate.TradingViewCoordinator;
import trademate.order.OrderView;
import trademate.setting.SettingView;
import trademate.verify.BackTestView;
import viewtify.Viewtify;
import viewtify.ui.UITab;
import viewtify.ui.dock.DockItem;
import viewtify.ui.dock.DockRegister;
import viewtify.ui.dock.DockSystem;

public class TradeMateDockRegister extends DockRegister {

    public void order() {
        register(OrderView.class);
    }

    public void tester() {
        register(BackTestView.class);
    }

    public void volumes() {
        register(GlobalVolumeView.class);
    }

    public void summary() {
        register(SummaryView.class);
    }

    public void setting() {
        register(SettingView.class);
    }

    public void trade(MarketService service) {
        UITab tab = DockSystem.register("Trade " + service.id).text(service.id).contentsLazy(ui -> new TradingView(ui, service));

        TradingViewCoordinator.requestLoading(service, tab);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean queryBy(String id) {
        if (id.startsWith("Trade ")) {
            Variable<MarketService> service = MarketServiceProvider.by(id.substring(6));
            if (service.isPresent()) {
                trade(service.exact());
                return true;
            }
        }

        return super.queryBy(id);
    }

    /**
     * Register dock menu.
     */
    public void registerMenu() {
        DockSystem.registerMenu(icon -> {
            icon.text(FontAwesome.Glyph.BARS).behaveLikeButton().context(menus -> {
                menus.menu(I.translate("Open new page"), sub -> {
                    for (DockItem item : queryIndependentDocks()) {
                        sub.menu(item.title()).disableWhen(DockSystem.isOpened(item.id())).action(item.registration());
                    }
                });
                menus.menu(I.translate("Open market"), sub -> {
                    MarketServiceProvider.availableProviders().to(provider -> {
                        sub.menu(provider.exchange().name(), nest -> {
                            provider.markets().forEach(service -> {
                                nest.menu(service.marketName).disableWhen(DockSystem.isOpened("Trade " + service.id)).action(() -> {
                                    trade(service);
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
            tester();
            volumes();
            summary();
        });
    }
}
