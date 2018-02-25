/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.order;

import java.time.ZonedDateTime;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import cointoss.Side;
import cointoss.order.Order;
import cointoss.order.Order.State;
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
            .observe(o -> o.state)
            .map(o -> o.state.is(State.ACTIVE) || o.state.is(State.REQUESTING) ? o.size : Num.ZERO)
            .reduce(Num.ZERO, Num::plus);

    /** Total price. */
    public final Calculation<Num> totalPrice = Viewtify.calculate(sub)
            .observe(o -> o.state)
            .map(o -> o.state.is(State.ACTIVE) || o.state.is(State.REQUESTING) ? o.size.multiply(o.price) : Num.ZERO)
            .reduce(Num.ZERO, Num::plus);

    /** Average price. */
    public final Calculation<Num> averagePrice = Viewtify.calculate(totalPrice, amount, (total, amount) -> total.divide(amount).scale(0));

    /** Side */
    public final Calculation<Side> side = Viewtify.calculate(sub).item(0).map(o -> o.side);

    /** The latest date */
    public final Calculation<ZonedDateTime> date = Viewtify.calculate(sub).item(0).flatVariable(o -> o.child_order_date);
}
