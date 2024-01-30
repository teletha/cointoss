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
import trademate.GlobalVolumeView;
import trademate.SummaryView;
import trademate.TradingView;
import trademate.TradingViewCoordinator;
import trademate.setting.SettingView;
import viewtify.ui.UITab;
import viewtify.ui.dock.DockRegister;
import viewtify.ui.dock.DockSystem;

public class TradeMateDockRegister extends DockRegister {

    public void volumes() {
        register(new GlobalVolumeView());
    }

    public void summary() {
        register(new SummaryView());
    }

    public void setting() {
        register(new SettingView());
    }

    public void trade(MarketService service) {
        UITab tab = DockSystem.register(service.id).text(service.id).contentsLazy(ui -> new TradingView(ui, service));

        TradingViewCoordinator.requestLoading(service, tab);
    }
}
