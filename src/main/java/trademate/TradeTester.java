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

import org.controlsfx.glyphfont.FontAwesome;

import cointoss.Market;
import cointoss.market.MarketServiceProvider;
import cointoss.util.EfficientWebSocket;
import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import trademate.dock.TradeMateDockRegister;
import viewtify.Viewtify;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.dock.DockItem;
import viewtify.ui.dock.DockSystem;

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
        TradeMateDockRegister docks = I.make(TradeMateDockRegister.class);

        DockSystem.registerLayout(() -> {
            docks.summary();
            docks.volumes();
            docks.tester();
        });

        DockSystem.registerMenu(icon -> {
            icon.text(FontAwesome.Glyph.BARS).behaveLikeButton().context(menus -> {
                menus.menu(en("Open new page"), views -> {
                    for (DockItem item : docks.queryIndependentDocks()) {
                        menus.menu(item.title).disableWhen(DockSystem.isOpened(item.id)).action(item.registration);
                    }
                });
                menus.menu(en("Open market"), sub -> {
                    MarketServiceProvider.availableProviders().to(provider -> {
                        sub.menu(provider.exchange().name(), nest -> {
                            provider.markets().forEach(service -> {
                                nest.menu(service.marketName).disableWhen(DockSystem.isOpened(service.id)).action(() -> {
                                    docks.trade(service);
                                });
                            });
                        });
                    });
                });
                menus.separator();
                menus.menu(en("Reboot")).action(Viewtify.application()::reactivate);
                menus.menu(en("Exit")).action(Viewtify.application()::deactivate);
            });
        });
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