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

/**
 * @version 2017/08/24 23:09:32
 */
public class Order implements Directional {

    /** The ordered position. */
    private Side side;

    /** The ordered size. */
    private Num size;

    /** The ordered price. */
    private Num price;

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

        when(priceLimit);
        type(quantity);
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
     * Get the side property of this {@link Order}.
     * 
     * @return The side property.
     */
    @Override
    public Side side() {
        return side;
    }

    /**
     * Get the side property of this {@link Order}.
     * 
     * @return The side property.
     */
    @SuppressWarnings("unused")
    private Side getSide() {
        return side;
    }

    /**
     * Set the side property of this {@link Order}.
     * 
     * @param side The side value to set.
     */
    @SuppressWarnings("unused")
    private void setSide(Side side) {
        this.side = side;
    }

    /**
     * Get the size property of this {@link Order}.
     * 
     * @return The size property.
     */
    public Num size() {
        return size;
    }

    /**
     * Get the size property of this {@link Order}.
     * 
     * @return The size property.
     */
    @SuppressWarnings("unused")
    private Num getSize() {
        return size;
    }

    /**
     * Set the size property of this {@link Order}.
     * 
     * @param size The size value to set.
     */
    @SuppressWarnings("unused")
    private void setSize(Num size) {
        this.size = size;
    }

    /**
     * Get the price property of this {@link Order}.
     * 
     * @return The price property.
     */
    public Num price() {
        return price;
    }

    /**
     * Get the price property of this {@link Order}.
     * 
     * @return The price property.
     */
    @SuppressWarnings("unused")
    private Num getPrice() {
        return price;
    }

    /**
     * Set the price property of this {@link Order}.
     * 
     * @param price The price value to set.
     */
    @SuppressWarnings("unused")
    private void setPrice(Num price) {
        this.price = price;
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
        this.quantity = quantity == null ? Quantity.GoodTillCnaceled : quantity;

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
        return new Order(position, size, price, null, null);
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
        GoodTillCnaceled, ImmediateOrCancel, FillOrKill;
    }

    /** The server ID */
    public String child_order_acceptance_id;

    /** Order type */
    public OrderType child_order_type;

    /** Order state */
    public OrderState child_order_state;

    /** The order date */
    public ZonedDateTime child_order_date;

    /** The expire date */
    public ZonedDateTime expire_date;

    /** The remaining size */
    public Num outstanding_size;

    /** The executed size */
    public Num executed_size;

    /** The canceled size */
    public Num cancel_size;

    /** The total commited size */
    public Num total_commission;

    /** Order avarage price */
    public Num average_price;

    /** INTERNAL USAGE */
    public Deque<Execution> executions = new ArrayDeque<>();

    /**
     * <p>
     * Utility.
     * </p>
     * 
     * @return
     */
    public final boolean isExpired() {
        return child_order_state == OrderState.EXPIRED;
    }

    /**
     * <p>
     * Utility.
     * </p>
     * 
     * @return
     */
    public boolean isCanceled() {
        return child_order_state == OrderState.CANCELED;
    }

    /**
     * <p>
     * Utility.
     * </p>
     * 
     * @return
     */
    public final boolean isCompleted() {
        return child_order_state == OrderState.COMPLETED;
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
            return average_price.isGreaterThanOrEqual(e.price);
        } else {
            return average_price.isLessThanOrEqual(e.price);
        }
    }

    /**
     * Test whether this order size can trade with the specified {@link Execution}.
     * 
     * @param e A target {@link Execution}.
     * @return A result.
     */
    public final boolean isTradableSizeWith(Execution e) {
        return size().isLessThanOrEqual(e.size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return side.mark() + size + "@" + average_price + " 残" + outstanding_size + " 済" + executed_size + " " + child_order_date;
    }

}
