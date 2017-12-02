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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import cointoss.util.Num;
import kiss.Observer;
import kiss.Signal;
import kiss.Variable;

/**
 * @version 2017/08/24 23:09:32
 */
public class Order implements Directional {

    /** The ordered position. */
    public final Side side;

    /** The ordered size. */
    public final Num size;

    /** The ordered price. */
    public final Num price;

    /** The order state */
    public final Variable<State> state = Variable.of(State.INIT);

    private Num triggerPrice;

    private Quantity quantity;

    /** The event listeners. */
    final CopyOnWriteArrayList<Observer<? super Execution>> executeListeners = new CopyOnWriteArrayList<>();

    /** The execution signal. */
    public final Signal<Execution> execute = new Signal(executeListeners);

    /** The event listeners. */
    final CopyOnWriteArrayList<Observer<? super Order>> cancelListeners = new CopyOnWriteArrayList<>();

    /** The execution signal. */
    public final Signal<Order> cancel = new Signal(cancelListeners);

    /**
     * <p>
     * Hide constructor.
     * </p>
     * 
     * @param position
     * @param price
     * @param size
     */
    protected Order(Side position, Num size, Num price, Num priceLimit, Quantity quantity) {
        this.side = Objects.requireNonNull(position);
        this.size = Objects.requireNonNull(size);
        this.price = price == null ? Num.ZERO : price;
        this.child_order_type = price == null ? OrderType.MARKET : OrderType.LIMIT;
        this.outstanding_size = Variable.of(size);

        when(priceLimit);
        type(quantity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Side side() {
        return side;
    }

    /**
     * Aliase for {@link #child_order_acceptance_id}.
     * 
     * @return
     */
    public String id() {
        return child_order_acceptance_id;
    }

    /**
     * Get the triggerPrice property of this {@link Order}.
     * 
     * @return The triggerPrice property.
     */
    public Num triggerPrice() {
        return triggerPrice;
    }

    /**
     * Get the triggerPrice property of this {@link Order}.
     * 
     * @return The triggerPrice property.
     */
    @SuppressWarnings("unused")
    private Num getTriggerPrice() {
        return triggerPrice;
    }

    /**
     * Set the triggerPrice property of this {@link Order}.
     * 
     * @param triggerPrice The triggerPrice value to set.
     */
    @SuppressWarnings("unused")
    private void setTriggerPrice(Num triggerPrice) {
        this.triggerPrice = triggerPrice;
    }

    /**
     * Get the quantity property of this {@link Order}.
     * 
     * @return The quantity property.
     */
    public Quantity quantity() {
        return quantity;
    }

    /**
     * Get the quantity property of this {@link Order}.
     * 
     * @return The quantity property.
     */
    @SuppressWarnings("unused")
    private Quantity getQuantity() {
        return quantity;
    }

    /**
     * Set the quantity property of this {@link Order}.
     * 
     * @param quantity The quantity value to set.
     */
    @SuppressWarnings("unused")
    private void setQuantity(Quantity quantity) {
        this.quantity = quantity;
    }

    /**
     * ストップ注文の値段を指定
     * 
     * @param priceLimit
     * @return
     */
    public Order when(int priceLimit) {
        return when(Num.of(priceLimit));
    }

    /**
     * ストップ注文の値段を指定
     * 
     * @param triggerPrice
     * @return
     */
    public Order when(Num triggerPrice) {
        if (triggerPrice != null) {
            this.triggerPrice = triggerPrice;
        }
        return this;
    }

    /**
     * 執行数量条件を指定
     * 
     * @param quantity
     * @return
     */
    public Order type(Quantity quantity) {
        this.quantity = quantity == null ? Quantity.GoodTillCanceled : quantity;

        return this;
    }

    /**
     * 成り行き注文
     * 
     * @param position
     * @param size
     * @return
     */
    public static Order market(Side position, Num size) {
        return new Order(position, size, null, null, null);
    }

    /**
     * 成り行き注文
     * 
     * @param size
     * @return
     */
    public static Order marketLong(int size) {
        return marketLong(Num.of(size));
    }

    /**
     * 成り行き注文
     * 
     * @param size
     * @return
     */
    public static Order marketLong(Num size) {
        return market(Side.BUY, size);
    }

    /**
     * 成り行き注文
     * 
     * @param size
     * @return
     */
    public static Order marketShort(int size) {
        return marketShort(Num.of(size));
    }

    /**
     * 成り行き注文
     * 
     * @param size
     * @return
     */
    public static Order marketShort(Num size) {
        return market(Side.SELL, size);
    }

    /**
     * 指値注文
     * 
     * @param position
     * @param size
     * @return
     */
    public static Order limit(Side position, Num size, Num price) {
        Order order = new Order(position, size, price, null, null);
        order.average_price.set(price);

        return order;
    }

    /**
     * 指値注文
     * 
     * @param size
     * @return
     */
    public static Order limitLong(double size, double price) {
        return limit(Side.BUY, Num.of(size), Num.of(price));
    }

    /**
     * 指値注文
     * 
     * @param size
     * @return
     */
    public static Order limitLong(Num size, Num price) {
        return limit(Side.BUY, size, price);
    }

    /**
     * 指値注文
     * 
     * @param size
     * @return
     */
    public static Order limitLong(String size, String price) {
        return limitLong(Num.of(size), Num.of(price));
    }

    /**
     * 指値注文
     * 
     * @param size
     * @return
     */
    public static Order limitLong(int size, int price) {
        return limitLong(Num.of(size), Num.of(price));
    }

    /**
     * 指値注文
     * 
     * @param size
     * @return
     */
    public static Order limitShort(Num size, Num price) {
        return limit(Side.SELL, size, price);
    }

    /**
     * 指値注文
     * 
     * @param size
     * @return
     */
    public static Order limitShort(String size, String price) {
        return limitShort(Num.of(size), Num.of(price));
    }

    /**
     * 指値注文
     * 
     * @param size
     * @return
     */
    public static Order limitShort(int size, int price) {
        return limitShort(Num.of(size), Num.of(price));
    }

    /**
     * @version 2017/07/22 18:02:37
     */
    public static enum Quantity {
        GoodTillCanceled, ImmediateOrCancel, FillOrKill;
    }

    /** The server ID */
    public String child_order_acceptance_id;

    /** Order type */
    public OrderType child_order_type;

    /** The order date */
    public Variable<ZonedDateTime> child_order_date = Variable.of(ZonedDateTime.now());

    /** The expire date */
    public ZonedDateTime expire_date;

    /** The remaining size */
    public final Variable<Num> outstanding_size;

    /** The executed size */
    public Num executed_size = Num.ZERO;

    /** The canceled size */
    public Num cancel_size;

    /** The total commited size */
    public Num total_commission;

    /** Order avarage price */
    public final Variable<Num> average_price = Variable.of(Num.ZERO);

    /** INTERNAL USAGE */
    public Deque<Execution> executions = new ArrayDeque<>();

    /**
     * Detect order type.
     * 
     * @return
     */
    public final boolean isLimit() {
        return price != null;
    }

    /**
     * <p>
     * Utility.
     * </p>
     * 
     * @return
     */
    public final boolean isExpired() {
        return state.is(State.EXPIRED);
    }

    /**
     * <p>
     * Utility.
     * </p>
     * 
     * @return
     */
    public boolean isCanceled() {
        return state.is(State.CANCELED);
    }

    /**
     * <p>
     * Utility.
     * </p>
     * 
     * @return
     */
    public final boolean isCompleted() {
        return state.is(State.COMPLETED);
    }

    /**
     * @return
     */
    public final boolean isNotCompleted() {
        return isCompleted() == false;
    }

    /**
     * Test whether this order can trade with the specified {@link Execution}.
     * 
     * @param e A target {@link Execution}.
     * @return A result.
     */
    public final boolean isTradableWith(Execution e) {
        return isTradableSizeWith(e) && isTradablePriceWith(e);
    }

    /**
     * Test whether this order price can trade with the specified {@link Execution}.
     * 
     * @param e A target {@link Execution}.
     * @return A result.
     */
    public final boolean isTradablePriceWith(Execution e) {
        if (child_order_type == OrderType.MARKET) {
            return true;
        }

        if (isBuy()) {
            return average_price.get().isGreaterThanOrEqual(e.price);
        } else {
            return average_price.get().isLessThanOrEqual(e.price);
        }
    }

    /**
     * Test whether this order size can trade with the specified {@link Execution}.
     * 
     * @param e A target {@link Execution}.
     * @return A result.
     */
    public final boolean isTradableSizeWith(Execution e) {
        return size.isLessThanOrEqual(e.size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return side().mark() + size + "@" + average_price + " 残" + outstanding_size + " 済" + executed_size + " " + child_order_date;
    }

    /**
     * @version 2017/12/02 14:54:40
     */
    public enum State {
        INIT, REQUESTING, ACTIVE, COMPLETED, CANCELED, EXPIRED, REJECTED;
    }
}
