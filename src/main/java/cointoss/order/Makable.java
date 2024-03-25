/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.order;

import cointoss.Direction;
import cointoss.Market;
import cointoss.util.arithmetic.Num;
import kiss.Variable;
import kiss.WiseTriFunction;

/**
 * Maker order strategy.
 */
public interface Makable {

    /**
     * Limit order with the specified price.
     * 
     * @param price A limit price.
     * @return Maker is cancellable.
     */
    default Cancellable make(long price) {
        return make(Num.of(price));
    }

    /**
     * Limit order with the specified price.
     * 
     * @param price A limit price.
     * @return Maker is cancellable.
     */
    default Cancellable make(double price) {
        return make(Num.of(price));
    }

    /**
     * Limit order with the specified price.
     * 
     * @param price A limit price.
     * @return Maker is cancellable.
     */
    default Cancellable make(Num price) {
        return make((market, direction, size) -> price);
    }

    /**
     * Limit order with the specified price.
     * 
     * @param price A limit price.
     * @return Maker is cancellable.
     */
    default Cancellable make(Variable<Num> price) {
        return make(price.v);
    }

    /**
     * Build your limit order by the current market infomation.
     * 
     * @param price
     * @return
     */
    Cancellable make(WiseTriFunction<Market, Direction, Num, Num> price);

    /**
     * Limit order with the best price by referrencing order books.
     * 
     * @return Maker is cancellable.
     */
    default Cancellable makeBestPrice() {
        return make((market, direction, price) -> market.orderBook.by(direction).computeBestPrice(market.service.setting.base.minimumSize));
    }

    /**
     * Limit order with the best price by referrencing order books.
     * 
     * @param direction You can reference orderbook by direction.
     * @return Maker is cancellable.
     */
    default Cancellable makeBestPrice(Direction direction) {
        return make((market, d, price) -> market.orderBook.by(direction).computeBestPrice(market.service.setting.base.minimumSize));
    }

    /**
     * Limit order with the best price by referrencing order books.
     * 
     * @return Maker is cancellable.
     */
    default Cancellable makeBestSellPrice() {
        return makeBestPrice(Direction.SELL);
    }

    /**
     * Limit order with the best price by referrencing order books.
     * 
     * @return Maker is cancellable.
     */
    default Cancellable makeBestBuyPrice() {
        return makeBestPrice(Direction.BUY);
    }

    /**
     * Limit order with the best price by referrencing order books.
     * 
     * @return Maker is cancellable.
     */
    default Cancellable makeClaster(Num from, Num to) {
        return makeBestPrice(Direction.BUY);
    }
}