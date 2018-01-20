/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.bot;

import cointoss.Trading;

/**
 * @version 2018/01/20 22:37:53
 */
public class Spreader extends Trading {

    private boolean whileTrading = false;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        add(market.timeline.to(e -> {
            System.out.println(e);
        }));

        // market.timeline.to(e -> {
        // if (whileTrading == false) {
        // whileTrading = true;
        //
        // Num volume = market.flow.volume();
        // Side entrySide = volume.isPositive() ? Side.BUY : Side.SELL;
        // Side exitSide = entrySide.inverse();
        // Num entrySize = Num.of(0.001);
        //
        // market.request(Order.limit(entrySide, entrySize, entryPrice));
        // }
        // });
    }
}
