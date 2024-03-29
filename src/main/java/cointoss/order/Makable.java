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

import java.util.ArrayList;
import java.util.List;

import cointoss.Direction;
import cointoss.Market;
import hypatia.Num;
import kiss.I;
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
     * @param price The price builder.
     * @return
     */
    default Cancellable make(WiseTriFunction<Market, Direction, Num, Num> price) {
        return makeOrder((makert, direction, size) -> I
                .list(Order.with.direction(direction, size).price(price.apply(makert, direction, size))));
    }

    /**
     * Build your limit order by the current market infomation.
     * 
     * @param order The order builder.
     * @return
     */
    Cancellable makeOrder(WiseTriFunction<Market, Direction, Num, List<Order>> order);

    /**
     * Limit order with the best price by referrencing order books.
     * 
     * @return Maker is cancellable.
     */
    default Cancellable makeBestPrice() {
        return make((market, direction, size) -> market.orderBook.by(direction).computeBestPrice(market.service.setting.base.minimumSize));
    }

    /**
     * Limit order with the best price by referrencing order books.
     * 
     * @param direction You can reference orderbook by direction.
     * @return Maker is cancellable.
     */
    default Cancellable makeBestPrice(Direction direction) {
        return make((market, dir, size) -> market.orderBook.by(direction).computeBestPrice(market.service.setting.base.minimumSize));
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
    default Cancellable makeCluster(double from, double to, Division division) {
        return makeCluster(Num.of(from), Num.of(to), division);
    }

    /**
     * Limit order with the best price by referrencing order books.
     * 
     * @return Maker is cancellable.
     */
    default Cancellable makeCluster(Num from, Num to, Division division) {
        return makeOrder((market, direction, size) -> {
            Num start = Num.max(direction, from, to);
            Num end = Num.min(direction, from, to);

            int scale = market.service.setting.base().scale;
            List<Order> orders = new ArrayList();
            Num diff = start.minus(end).divide(Math.max(1, division.size - 1)).scale(scale);
            for (int i = 0; i < division.size; i++) {
                orders.add(Order.with.direction(direction, size.multiply(division.weights[i]))
                        .price(start.minus(diff.multiply(i)).scale(scale)));
            }
            return orders;
        });
    }
}