/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.market.bitflyer.BitFlyerService;
import cointoss.util.Chrono;
import trademate.chart.ChartView;
import viewtify.UI;
import viewtify.User;
import viewtify.View;
import viewtify.Viewtify;
import viewtify.ui.UIButton;
import viewtify.ui.UIComboBox;
import viewtify.ui.UIDatePicker;

/**
 * @version 2018/06/26 21:28:54
 */
public class BackTestView extends View {

    @UI
    private UIComboBox<MarketService> market;

    @UI
    private UIDatePicker startDate;

    @UI
    private UIDatePicker endDate;

    @UI
    private UIButton start;

    @UI
    private ChartView chart;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        chart.market.set(new Market(BitFlyerService.FX_BTC_JPY));
        Viewtify.Terminator.add(chart.market.get());
        market.initial(BitFlyerService.FX_BTC_JPY).values(BitFlyerService.FX_BTC_JPY);
        startDate.initial(Chrono.utcNow().minusDays(10).toLocalDate());
        endDate.initial(Chrono.utcNow().toLocalDate());

        start.when(User.Click).to(e -> {
            System.out.println("OK");
        });
    }
}
