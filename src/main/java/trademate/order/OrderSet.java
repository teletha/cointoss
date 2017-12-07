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

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import cointoss.Order;
import cointoss.Side;
import cointoss.util.Num;
import viewtify.Calculation;
import viewtify.Viewtify;

/**
 * @version 2017/12/04 22:02:23
 */
public class OrderSet {
    public final ObservableList<Order> sub = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());

    /** Total amount. */
    public final Calculation<Num> amount = Viewtify.calculate(new SimpleObjectProperty(Num.ZERO));

    /** Total price. */
    public final Calculation<Num> totalPrice = Viewtify.calculate(new SimpleObjectProperty(Num.ZERO));

    /** Average price. */
    public final Calculation<Num> averagePrice = Viewtify.calculate(new SimpleObjectProperty(Num.ZERO));

    /** Side */
    public final Calculation<Side> side = Viewtify.calculate(new SimpleObjectProperty(Side.BUY));

    /** The latest date */
    public final Calculation<ZonedDateTime> date = Viewtify.calculate(new SimpleObjectProperty(ZonedDateTime.now()));
    // public final ObservableList<Order> sub =
    // FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
    //
    // /** Total amount. */
    // public final Calculation<Num> amount = Viewtify.calculate(sub).reduce(Num.ZERO, (p, q) ->
    // p.plus(q.size));
    //
    // /** Total price. */
    // public final Calculation<Num> totalPrice = Viewtify.calculate(sub).reduce(Num.ZERO, (p, q) ->
    // p.plus(q.price.multiply(q.size)));
    //
    // /** Average price. */
    // public final Calculation<Num> averagePrice = Viewtify.calculate(totalPrice, amount, (total,
    // amount) -> total.divide(amount).scale(0));
    //
    // /** Side */
    // public final Calculation<Side> side = Viewtify.calculate(sub).item(0).map(o -> o.side);
    //
    // /** The latest date */
    // public final Calculation<ZonedDateTime> date =
    // Viewtify.calculate(sub).item(0).calculateVariable(o -> o.child_order_date);
}
