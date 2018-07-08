/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import cointoss.Directional;
import cointoss.Execution;
import cointoss.Side;
import cointoss.util.Num;
import kiss.I;
import kiss.Signal;
import kiss.Signaling;
import kiss.Variable;

/**
 * @version 2018/07/08 13:10:26
 */
public class Order implements Directional {

    /** The order identifier for the specific market. */
    public final Variable<String> id = Variable.empty();

    /** The order type */
    public final OrderType type;

    /** The order state */
    public final Variable<OrderState> state = Variable.of(OrderState.INIT);

    /** The ordered position. */
    public final Side side;

    /** The ordered price. */
    public final Variable<Num> price;

    /** The ordered size. */
    public final Num size;

    /** The remaining size */
    public final Variable<Num> sizeRemaining;

    /** The executed size */
    public final Variable<Num> sizeExecuted = Variable.of(Num.ZERO);

    /** The canceled size */
    public final Variable<Num> sizeCanceled = Variable.of(Num.ZERO);

    /** The order created date-time */
    public final Variable<ZonedDateTime> created = Variable.of(ZonedDateTime.now());

    /** The attribute holder. */
    private Map<Class, Object> attributes;

    /** The stop trigger price. */
    private Num stopPrice;

    /** The quantity conditions enforcement. */
    private QuantityCondition quantityCondition;

    /** The event listeners. */
    public final Signaling<Execution> listeners = new Signaling();

    /** The execution signal. */
    public final Signal<Execution> execute = listeners.expose;

    /**
     * <p>
     * Hide constructor.
     * </p>
     * 
     * @param position
     * @param price
     * @param size
     */
    protected Order(Side position, Num size, Num price, Num priceLimit, QuantityCondition quantity) {
        this.side = Objects.requireNonNull(position);
        this.size = Objects.requireNonNull(size);
        this.price = Variable.of(price == null ? Num.ZERO : price);
        this.type = price == null || price.isZero() ? OrderType.MARKET : OrderType.LIMIT;
        this.sizeRemaining = Variable.of(size);

        stopAt(priceLimit);
        type(quantity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Side side() {
        return side;
    }

    /**
     * Retrieve the {@link QuantityCondition} of this {@link Order}.
     * 
     * @return A {@link QuantityCondition}.
     */
    public final QuantityCondition quantityCondition() {
        return quantityCondition;
    }

    /**
     * Set the {@link QuantityCondition} of this {@link Order}.
     * 
     * @param quantityCondition A {@link QuantityCondition} to set.
     * @return Chainable API.
     */
    public final Order type(QuantityCondition quantityCondition) {
        this.quantityCondition = quantityCondition == null ? QuantityCondition.GoodTillCanceled : quantityCondition;

        return this;
    }

    /**
     * Retrieve the stop trigger price of this {@link Order}.
     * 
     * @return A stop price.
     */
    public final Num stopPrice() {
        return stopPrice;
    }

    /**
     * Set stop trigger price of this {@link Order}.
     * 
     * @param price A price to stop.
     * @return Chainable API.
     */
    public final Order stopAt(long price) {
        return stopAt(Num.of(price));
    }

    /**
     * Set stop trigger price of this {@link Order}.
     * 
     * @param price A price to stop.
     * @return Chainable API.
     */
    public final Order stopAt(double price) {
        return stopAt(Num.of(price));
    }

    /**
     * Set stop trigger price of this {@link Order}.
     * 
     * @param price A price to stop.
     * @return Chainable API.
     */
    public final Order stopAt(Num price) {
        if (price != null) {
            this.stopPrice = price;
        }
        return this;
    }

    /**
     * Observe when this {@link Order} will be canceled or completed.
     * 
     * @return A event {@link Signal}.
     */
    public final Signal<Order> observeTerminating() {
        return state.observe().take(OrderState.CANCELED, OrderState.COMPLETED).take(1).mapTo(this);
    }

    /**
     * Detect order type.
     * 
     * @return
     */
    public final boolean isLimit() {
        return price.v != null;
    }

    /**
     * Check the order {@link OrderState}.
     * 
     * @return The result.
     */
    public final boolean isExpired() {
        return state.is(OrderState.EXPIRED);
    }

    /**
     * Check the order {@link OrderState}.
     * 
     * @return The result.
     */
    public final boolean isNotExpired() {
        return isExpired() == false;
    }

    /**
     * Check the order {@link OrderState}.
     * 
     * @return The result.
     */
    public final boolean isCanceled() {
        return state.is(OrderState.CANCELED);
    }

    /**
     * Check the order {@link OrderState}.
     * 
     * @return The result.
     */
    public final boolean isNotCanceled() {
        return isCanceled() == false;
    }

    /**
     * Check the order {@link OrderState}.
     * 
     * @return The result.
     */
    public final boolean isCompleted() {
        return state.is(OrderState.COMPLETED);
    }

    /**
     * Check the order {@link OrderState}.
     *
     * @return The result.
     */
    public final boolean isNotCompleted() {
        return isCompleted() == false;
    }

    /**
     * Retrieve the attribute by the specified type.
     * 
     * @param type A attribute type.
     */
    public final <T> T attribute(Class<T> type) {
        if (attributes == null) {
            attributes = new ConcurrentHashMap();
        }
        return (T) attributes.computeIfAbsent(type, key -> I.make(type));
    }

    /**
     * Copy all attributes from the specified {@link Order}.
     * 
     * @param base
     */
    public final void copyAttributeFrom(Order base) {
        if (attributes == null) {
            attributes = new ConcurrentHashMap();
        }
        attributes.putAll(base.attributes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Order ? Objects.equals(id, ((Order) obj).id) : false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return side().mark() + size + "@" + price + " 残" + sizeRemaining + " 済" + sizeExecuted + " " + created;
    }

    /**
     * Create market order.
     * 
     * @param side A {@link Side} of order.
     * @param size A size of order.
     * @return A created {@link Order}.
     */
    public static Order market(Side side, long size) {
        return market(side, Num.of(size));
    }

    /**
     * Create market order.
     * 
     * @param side A {@link Side} of order.
     * @param size A size of order.
     * @return A created {@link Order}.
     */
    public static Order market(Side side, double size) {
        return market(side, Num.of(size));
    }

    /**
     * Create market order.
     * 
     * @param side A {@link Side} of order.
     * @param size A size of order.
     * @return A created {@link Order}.
     */
    public static Order market(Side side, Num size) {
        return new Order(side, size, null, null, null);
    }

    /**
     * Create LONG market order.
     * 
     * @param size A size of order.
     * @return A created {@link Order}.
     */
    public static Order marketLong(long size) {
        return marketLong(Num.of(size));
    }

    /**
     * Create LONG market order.
     * 
     * @param size A size of order.
     * @return A created {@link Order}.
     */
    public static Order marketLong(double size) {
        return marketLong(Num.of(size));
    }

    /**
     * Create LONG market order.
     * 
     * @param size A size of order.
     * @return A created {@link Order}.
     */
    public static Order marketLong(Num size) {
        return market(Side.BUY, size);
    }

    /**
     * Create SHORT market order.
     * 
     * @param size A size of order.
     * @return A created {@link Order}.
     */
    public static Order marketShort(long size) {
        return marketShort(Num.of(size));
    }

    /**
     * Create SHORT market order.
     * 
     * @param size A size of order.
     * @return A created {@link Order}.
     */
    public static Order marketShort(double size) {
        return marketShort(Num.of(size));
    }

    /**
     * Create SHORT market order.
     * 
     * @param size A size of order.
     * @return A created {@link Order}.
     */
    public static Order marketShort(Num size) {
        return market(Side.SELL, size);
    }

    /**
     * Create limit order.
     * 
     * @param side A {@link Side} of order.
     * @param size A size of order.
     * @param price A price of order.
     * @return A created {@link Order}.
     */
    public static Order limit(Side side, long size, long price) {
        return limit(side, Num.of(size), Num.of(price));
    }

    /**
     * Create limit order.
     * 
     * @param side A {@link Side} of order.
     * @param size A size of order.
     * @param price A price of order.
     * @return A created {@link Order}.
     */
    public static Order limit(Side side, double size, double price) {
        return limit(side, Num.of(size), Num.of(price));
    }

    /**
     * Create limit order.
     * 
     * @param side A {@link Side} of order.
     * @param size A size of order.
     * @param price A price of order.
     * @return A created {@link Order}.
     */
    public static Order limit(Side side, Num size, Num price) {
        return new Order(side, size, price, null, null);
    }

    /**
     * Create LONG limit order.
     * 
     * @param size A size of order.
     * @param price A price of order.
     * @return A created {@link Order}.
     */
    public static Order limitLong(long size, long price) {
        return limitLong(Num.of(size), Num.of(price));
    }

    /**
     * Create LONG limit order.
     * 
     * @param size A size of order.
     * @param price A price of order.
     * @return A created {@link Order}.
     */
    public static Order limitLong(double size, double price) {
        return limit(Side.BUY, Num.of(size), Num.of(price));
    }

    /**
     * Create LONG limit order.
     * 
     * @param size A size of order.
     * @param price A price of order.
     * @return A created {@link Order}.
     */
    public static Order limitLong(Num size, Num price) {
        return limit(Side.BUY, size, price);
    }

    /**
     * Create SHORT limit order.
     * 
     * @param size A size of order.
     * @param price A price of order.
     * @return A created {@link Order}.
     */
    public static Order limitShort(long size, long price) {
        return limitShort(Num.of(size), Num.of(price));
    }

    /**
     * Create SHORT limit order.
     * 
     * @param size A size of order.
     * @param price A price of order.
     * @return A created {@link Order}.
     */
    public static Order limitShort(double size, double price) {
        return limitShort(Num.of(size), Num.of(price));
    }

    /**
     * Create SHORT limit order.
     * 
     * @param size A size of order.
     * @param price A price of order.
     * @return A created {@link Order}.
     */
    public static Order limitShort(Num size, Num price) {
        return limit(Side.SELL, size, price);
    }
}
