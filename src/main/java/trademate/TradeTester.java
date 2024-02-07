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
import trademate.dock.TradeMateDockProvider;
import viewtify.Viewtify;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.dock.Dock;
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
        DockSystem.initialize(icon -> {
            TradeMateDockProvider docks = I.make(TradeMateDockProvider.class);

            icon.text(FontAwesome.Glyph.BARS).behaveLikeButton().context(menus -> {
                menus.menu(I.translate("Open new page"), sub -> {
                    for (Dock item : docks.findDocks()) {
                        sub.menu(item.title()).disableWhen(DockSystem.isOpened(item.id())).action(item::show);
                    }
                });
                menus.menu(I.translate("Open market"), sub -> {
                    MarketServiceProvider.availableProviders().to(provider -> {
                        sub.menu(provider.exchange().name(), nest -> {
                            provider.markets().forEach(service -> {
                                nest.menu(service.marketName).disableWhen(DockSystem.isOpened("Trade " + service.id)).action(() -> {
                                    docks.trade.show(service);
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
                    error.printStackTrace();
                })
                .icon("icon/tester.png")
                .onTerminating(EfficientWebSocket::shutdownNow)
                .activate(TradeTester.class);
    }
}