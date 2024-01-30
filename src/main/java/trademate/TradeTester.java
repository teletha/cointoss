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

import org.controlsfx.glyphfont.FontAwesome;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.market.Exchange;
import cointoss.market.MarketServiceProvider;
import cointoss.util.EfficientWebSocket;
import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import trademate.dock.TradeMateDockRegister;
import trademate.setting.SettingView;
import trademate.verify.BackTestView;
import viewtify.Viewtify;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
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

        // DockSystem.register("Order").contentsLazy(OrderView.class).text(en("Order")).closable(false);
        // DockSystem.register("Global").contentsLazy(GlobalVolumeView.class).text("流動性").closable(false);

        DockSystem.registerLayout(() -> {
            docks.registerIndependentViews();

            MarketServiceProvider.availableMarketServices()
                    .take(MarketService::supportHistoricalTrade)
                    .take(e -> e.exchange == Exchange.Bybit)
                    .take(1)
                    .to(docks::trade);
        });

        List<View> pages = List.of(new SummaryView(), new BackTestView(), new SettingView());

        DockSystem.registerMenu(icon -> {
            icon.text(FontAwesome.Glyph.BARS).behaveLikeButton().context(menus -> {
                menus.menu(en("Open new page"), views -> {

                    for (View page : docks.queryIndependentViews()) {
                        views.menu(page.title()).disableWhen(DockSystem.isOpened(page.id())).action(() -> buildPage(page));
                    }
                });
                menus.separator();
                menus.menu(en("Reboot")).action(Viewtify.application()::reactivate);
                menus.menu(en("Exit")).action(Viewtify.application()::deactivate);
            });
        });
    }

    private void buildPage(View view) {
        DockSystem.register(view.id()).text(view.title()).contentsLazy(tab -> view);
        DockSystem.select(view.id());
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