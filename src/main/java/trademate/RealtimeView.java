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

import java.util.HashMap;
import java.util.Map;

import javafx.scene.layout.Region;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.ticker.Span;
import cointoss.ticker.Tick;
import viewtify.ui.View;
import viewtify.ui.canvas.EnhancedCanvas;

public class RealtimeView extends View {

    private final EnhancedCanvas volumes = new EnhancedCanvas().bindSizeTo((Region) ui());

    private final Map<MarketService, Tick> latestTicks = new HashMap();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
    }

    public void register(Market market) {
        market.tickers.on(Span.Second5).open.to(tick -> latestTicks.put(market.service, tick));
    }
}