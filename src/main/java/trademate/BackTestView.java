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

import cointoss.MarketService;
import cointoss.market.bitflyer.BitFlyerService;
import cointoss.util.Chrono;
import viewtify.UI;
import viewtify.User;
import viewtify.View;
import viewtify.ui.UIButton;
import viewtify.ui.UIComboBox;
import viewtify.ui.UIDatePicker;

/**
 * @version 2018/06/25 18:26:01
 */
public class BackTestView extends View {

    /** UI */
    private @UI UIComboBox<MarketService> market;

    /** UI */
    private @UI UIDatePicker startDate;

    /** UI */
    private @UI UIDatePicker endDate;

    /** UI */
    private @UI UIButton start;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        market.initial(BitFlyerService.FX_BTC_JPY).values(BitFlyerService.FX_BTC_JPY);
        startDate.initial(Chrono.utcNow().minusDays(10).toLocalDate());
        endDate.initial(Chrono.utcNow().toLocalDate());

        start.when(User.Click).to(e -> {
        });
    }
}
