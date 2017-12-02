/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.time.ZonedDateTime;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.fxmisc.easybind.EasyBind;

import cointoss.util.Num;
import viewtify.Viewtify;
import viewtify.bind.MonadicBinding;

/**
 * @version 2017/11/26 13:44:57
 */
public class OrderSet {

    public final ObservableList<Order> sub = FXCollections.observableArrayList();

    /** Total amount calculation. */
    final ObjectBinding<Num> amount = Bindings
            .createObjectBinding(() -> sub.stream().reduce(Num.ZERO, (o, n) -> o.plus(n.size), Num::plus), sub);

    /** Total price calculation. */
    final ObjectBinding<Num> totalPrice = Bindings
            .createObjectBinding(() -> sub.stream().reduce(Num.ZERO, (o, n) -> o.plus(n.price.multiply(n.size)), (p, q) -> p.plus(q)), sub);

    /** Average price calculation. */
    final org.fxmisc.easybind.monadic.MonadicBinding<Num> averagePrice = EasyBind
            .combine(totalPrice, amount, (total, amount) -> total.divide(amount).scale(0));

    /** Average price calculation. */
    final MonadicBinding<Side> side = Viewtify.bind(sub).item(0).map(o -> o.side);

    final MonadicBinding<ZonedDateTime> date = Viewtify.bind(sub).item(0).flatVariable(o -> o.child_order_date);

    /**
     * Get the amount property of this {@link OrderSet}.
     * 
     * @return The amount property.
     */
    public ObjectBinding<Num> amount() {
        Binding<Num> bind = Viewtify.bind(sub, signal -> signal.scan(Num.ZERO, (o, n) -> o.plus(n.size)).take);
        return amount;
    }

    /**
     * Get the totalPrice property of this {@link OrderSet}.
     * 
     * @return The totalPrice property.
     */
    public ObjectBinding<Num> totalPrice() {
        return totalPrice;
    }

    /**
     * Get the averagePrice property of this {@link OrderSet}.
     * 
     * @return The averagePrice property.
     */
    public org.fxmisc.easybind.monadic.MonadicBinding<Num> averagePrice() {
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
