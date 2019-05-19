/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.order;

import java.time.ZonedDateTime;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import cointoss.Direction;
import cointoss.order.Order;
import cointoss.order.OrderState;
import cointoss.util.Num;
import viewtify.Viewtify;
import viewtify.bind.Calculation;

/**
 * @version 2018/02/25 17:50:47
 */
public class OrderSet {

    public final ObservableList<Order> sub = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());

    /** Total amount. */
    public final Calculation<Num> amount = Viewtify.calculate(sub)
            .observeVariable(o -> o.observeStateNow().to())
            .map(o -> o.state == OrderState.ACTIVE || o.state == OrderState.REQUESTING ? o.size : Num.ZERO)
            .reduce(Num.ZERO, Num::plus);

    /** Total price. */
    public final Calculation<Num> totalPrice = Viewtify.calculate(sub)
            .observeVariable(o -> o.observeStateNow().to())
            .map(o -> o.state == OrderState.ACTIVE || o.state == OrderState.REQUESTING ? o.size.multiply(o.price) : Num.ZERO)
            .reduce(Num.ZERO, Num::plus);

    /** Average price. */
    public final Calculation<Num> averagePrice = Viewtify.calculate(totalPrice, amount, (total, amount) -> total.divide(amount).scale(0));

    /** Side */
    public final Calculation<Direction> side = Viewtify.calculate(sub).item(0).map(o -> o.direction);

    /** The latest date */
    public final Calculation<ZonedDateTime> date = Viewtify.calculate(sub).item(0).flatMap(o -> o.observeCreationTimeNow());

    /**
     * Add order.
     * 
     * @param order
     * @return
     */
    public OrderSet add(Order order) {
        if (order != null) {
            sub.add(order);
        }
        return this;
    }
}
