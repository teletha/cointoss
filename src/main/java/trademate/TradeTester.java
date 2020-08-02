/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.market.bitmex.BitMex;
import kiss.I;
import kiss.Managed;
import kiss.Singleton;
import trademate.verify.BackTestView;
import viewtify.Theme;
import viewtify.Viewtify;
import viewtify.ui.UITab;
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
        DockSystem.register("BackTest").contents(BackTestView.class).closable(false);

        List<MarketService> services = List.of(BitMex.ETH_USD);

        // ========================================================
        // Create Tab for each Markets
        // ========================================================
        for (MarketService service : services) {
            UITab tab = DockSystem.register(service.marketIdentity())
                    .closable(false)
                    .text(service.marketReadableName())
                    .contents(ui -> new TradingView(ui, service));
        }
    }

    /**
     * Entry point.
     */
    public static void main(String[] args) {
        I.load(Market.class);

        // activate application
        Viewtify.application()
                .logging(LogManager.getLogger()::error)
                .use(Theme.Dark)
                .icon("icon/tester.png")
                .language(I.env("language", Locale.getDefault().getLanguage()))
                .activate(TradeTester.class);
    }
}