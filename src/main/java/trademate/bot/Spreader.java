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

import cointoss.MarketBackend.Health;
import cointoss.order.OrderBookList;
import cointoss.Order;
import cointoss.Side;
import cointoss.Trading;
import cointoss.util.Num;

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

        market.timeline.to(e -> {
            if (whileTrading == false && market.health.is(Health.Normal)) {
                Num volume = market.flow.volume();

                if (volume.abs().isGreaterThanOrEqual(3)) {
                    whileTrading = true;

                    Side entrySide = volume.isPositive() ? Side.BUY : Side.SELL;
                    Side exitSide = entrySide.inverse();
                    Num entrySize = Num.of(0.001);
                    
                    OrderBookList entryBook =  entrySide.isBuy() ? market.orderBook.longs : market.orderBook.shorts;
                    OrderBookList exitBoot = exitSide.isBuy() ? market.orderBook.longs : market.orderBook.shorts;
                    
                    Num entryPrice = entryBook.computeBestPrice()

                    market.request(Order.limit(entrySide, entrySize, entryPrice)).to(entry -> {
                        entry.execute.to(entryExe -> {
                            market.request(Order.limit(exitSide, entryExe.size, ma))
                        });
                    });
                }
            }
        });
    }
}
