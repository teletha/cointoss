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

import cointoss.Order;
import cointoss.Side;
import cointoss.util.Num;
import viewtify.Viewtify;
import viewtify.calculation.Calculatable;

/**
 * @version 2017/11/26 13:44:57
 */
public class OrderSet {

    public final ObservableList<Order> sub = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());

    /** Total amount. */
    public final Calculatable<Num> amount = Viewtify.calculate(sub).reduce(Num.ZERO, (p, q) -> p.plus(q.size));

    /** Total price. */
    public final Calculatable<Num> totalPrice = Viewtify.calculate(sub).reduce(Num.ZERO, (p, q) -> p.plus(q.price.multiply(q.size)));

    /** Average price. */
    public final Calculatable<Num> averagePrice = Viewtify.calculate(totalPrice, amount, (total, amount) -> total.divide(amount).scale(0));

    /** Side */
    public final Calculatable<Side> side = Viewtify.calculate(sub).item(0).map(o -> o.side);

    /** The latest date */
    public final Calculatable<ZonedDateTime> date = Viewtify.calculate(sub).item(0).calculateVariable(o -> o.child_order_date);
}
