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

import javafx.beans.binding.ObjectBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import cointoss.Order;
import cointoss.Side;
import cointoss.util.Num;
import viewtify.Viewtify;
import viewtify.bind.Bind;
import viewtify.bind.MonadicBinding;

/**
 * @version 2017/11/26 13:44:57
 */
public class OrderSet {

    public final ObservableList<Order> sub = FXCollections.observableArrayList();

    /** Total amount calculation. */
    final Bind<Num> amount = Viewtify.bind(sub).reduce(Num.ZERO, (p, q) -> p.plus(q.size));

    /** Total price calculation. */
    final Bind<Num> totalPrice = Viewtify.bind(sub).reduce(Num.ZERO, (p, q) -> p.plus(q.price.multiply(q.size)));

    /** Average price calculation. */
    final ObjectBinding<Num> averagePrice = Viewtify.bind(totalPrice, amount, (total, amount) -> total.divide(amount).scale(0));

    /** Average price calculation. */
    final MonadicBinding<Side> side = Viewtify.bind(sub).item(0).map(o -> o.side);

    final MonadicBinding<ZonedDateTime> date = Viewtify.bind(sub).item(0).flatVariable(o -> o.child_order_date);

    /**
     * Get the amount property of this {@link OrderSet}.
     * 
     * @return The amount property.
     */
    public Bind<Num> amount() {
        return amount;
    }

    /**
     * Get the totalPrice property of this {@link OrderSet}.
     * 
     * @return The totalPrice property.
     */
    public Bind<Num> totalPrice() {
        return totalPrice;
    }

    /**
     * Get the averagePrice property of this {@link OrderSet}.
     * 
     * @return The averagePrice property.
     */
    public ObjectBinding<Num> averagePrice() {
        return averagePrice;
    }

    /**
     * Get the side property of this {@link OrderSet}.
     * 
     * @return The side property.
     */
    public MonadicBinding<Side> side() {
        return side;
    }

    /**
     * Get the date property of this {@link OrderSet}.
     * 
     * @return The side property.
     */
    public MonadicBinding<ZonedDateTime> date() {
        return date;
    }
}
