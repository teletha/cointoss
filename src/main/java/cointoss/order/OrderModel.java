/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.order;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.util.Chrono;
import cointoss.util.Num;
import cointoss.util.ObservableProperty;
import icy.manipulator.Icy;
import kiss.I;
import kiss.Signal;
import kiss.Signaling;

@Icy(grouping = 2)
abstract class OrderModel implements Directional, Comparable<OrderModel> {

    /** The relation holder. */
    private Map<Class, Object> relations;

    /** The size related signal. */
    private final Signaling<Num> remainingSize = new Signaling();

    /** The size related signal. */
    private final Signaling<Num> executedSize = new Signaling();

    /**
     * {@inheritDoc}
     */
    @Icy.Property
    @Override
    public abstract Direction direction();

    /**
     * Specify direction by literal.
     * 
     * @param direction A direction literal.
     * @return A parsed direction.
     */
    @Icy.Overload("direction")
    private Direction direction(String direction) {
        return Direction.parse(direction);
    }

    /**
     * The initial ordered size.
     * 
     * @return
     */
    @Icy.Property
    public abstract Num size();

    /**
     * Set order size by value.
     * 
     * @param size An executed size.
     * @return Chainable API.
     */
    @Icy.Overload("size")
    private Num size(long size) {
        return Num.of(size);
    }

    /**
     * Set order size by value.
     * 
     * @param size An executed size.
     * @return Chainable API.
     */
    @Icy.Overload("size")
    private Num size(double size) {
        return Num.of(size);
    }

    /**
     * Size validation.
     * 
     * @param size
     * @return
     */
    @Icy.Intercept("size")
    private Num validateSize(Num size, Consumer<Num> remainingSize) {
        if (size.isNegativeOrZero()) {
            throw new IllegalArgumentException("Order size must be positive.");
        }
        remainingSize.accept(size);
        return size;
    }

    /**
     * An average price.
     * 
     * @return
     */
    @Icy.Property(setterModifier = "final")
    public Num price() {
        return Num.ZERO;
    }

    /**
     * Set price by value.
     * 
     * @param price A price.
     * @return Chainable API.
     */
    @Icy.Overload("price")
    private Num price(long price) {
        return Num.of(price);
    }

    /**
     * Set price by value.
     * 
     * @param price A price.
     * @return Chainable API.
     */
    @Icy.Overload("price")
    private Num price(double price) {
        return Num.of(price);
    }

    /**
     * Validate order price.
     * 
     * @param price
     * @return
     */
    @Icy.Intercept("price")
    private Num price(Num price, Consumer<OrderType> type) {
        if (price.isNegative()) {
            price = Num.ZERO;
        }

        if (state() == OrderState.INIT) {
            type.accept(price.isZero() ? OrderType.Taker : OrderType.Maker);
        }
        return price;
    }

    /**
     * The order type.
     * 
     * @return
     */
    @Icy.Property
    public OrderType type() {
        return OrderType.Taker;
    }

    /**
     * The quantity conditions enforcement.
     * 
     * @return
     */
    @Icy.Property
    public QuantityCondition quantityCondition() {
        return QuantityCondition.GoodTillCanceled;
    }

    /**
     * Calculate the remaining size of this order.
     * 
     * @return
     */
    @Icy.Property(setterModifier = "final")
    public Num remainingSize() {
        return Num.ZERO;
    }

    /**
     * Expose setter to update size atomically.
     * 
     * @param size
     */
    abstract void setRemainingSize(Num size);

    /**
     * Observe remaining size modification.
     * 
     * @return
     */
    public final Signal<Num> observeRemainingSize() {
        return remainingSize.expose;
    }

    /**
     * Observe remaining size modification.
     * 
     * @return
     */
    public final Signal<Num> observeRemainingSizeNow() {
        return observeRemainingSize().startWith(remainingSize());
    }

    /**
     * Observe remaining size modification.
     * 
     * @return
     */
    public final Signal<Num> observeRemainingSizeDiff() {
        return observeRemainingSizeNow().maps(Num.ZERO, (prev, now) -> now.minus(prev));
    }

    /**
     * Calculate executed size of this order.
     * 
     * @return
     */
    @Icy.Property(setterModifier = "final")
    public Num executedSize() {
        return Num.ZERO;
    }

    /**
     * Expose setter to update size atomically.
     * 
     * @param size
     */
    abstract void setExecutedSize(Num size);

    /**
     * Observe executed size modification.
     * 
     * @return
     */
    public final Signal<Num> observeExecutedSize() {
        return executedSize.expose;
    }

    /**
     * Observe executed size modification.
     * 
     * @return
     */
    public final Signal<Num> observeExecutedSizeNow() {
        return observeExecutedSize().startWith(executedSize());
    }

    /**
     * Observe executed size modification.
     * 
     * @return
     */
    public final Signal<Num> observeExecutedSizeDiff() {
        return observeExecutedSizeNow().maps(Num.ZERO, (prev, now) -> now.minus(prev));
    }

    /**
     * Update size atomically.
     * 
     * @param remainingSize
     * @param executedSize
     */
    final void updateAtomically(Num remainingSize, Num executedSize) {
        setRemainingSize(remainingSize);
        setExecutedSize(executedSize);

        this.remainingSize.accept(remainingSize);
        this.executedSize.accept(executedSize);
    }

    /**
     * The order identifier for the specific market.
     * 
     * @return
     */
    @Icy.Property(setterModifier = "final")
    public String id() {
        return "";
    }

    /**
     * The requested time of this order.
     * 
     * @return
     */
    @Icy.Property(custom = ObservableProperty.class, setterModifier = "final")
    public ZonedDateTime creationTime() {
        return Chrono.MIN;
    }

    /**
     * The termiated time of this order.
     * 
     * @return
     */
    @Icy.Property(custom = ObservableProperty.class, setterModifier = "final")
    public ZonedDateTime terminationTime() {
        return null;
    }

    /**
     * The termiated time of this order.
     * 
     * @return
     */
    @Icy.Property(custom = ObservableProperty.class, setterModifier = "final")
    public OrderState state() {
        return OrderState.INIT;
    }

    @Icy.Intercept("state")
    private OrderState validateState(OrderState state) {
        OrderState current = state();

        switch (current) {
        case CANCELED:
        case COMPLETED:
            return current;
        default:
            return state;
        }
    }

    public abstract Signal<OrderState> observeState();

    /**
     * Calculate canceled size.
     * 
     * @return
     */
    public final Num canceledSize() {
        if (state() != OrderState.CANCELED) {
            return Num.ZERO;
        } else {
            return size().minus(executedSize());
        }
    }

    /**
     * Observe when this {@link Order} will be active.
     * 
     * @return A event {@link Signal}.
     */
    public final Signal<Order> observeActivating() {
        return observeState().take(OrderState.ACTIVE).take(1).mapTo((Order) this);
    }

    /**
     * Observe when this {@link Order} will be canceled or completed.
     * 
     * @return A event {@link Signal}.
     */
    public final Signal<Order> observeTerminating() {
        return observeState().take(OrderState.CANCELED, OrderState.COMPLETED).take(1).mapTo((Order) this);
    }

    /**
     * Check {@link OrderState}.
     * 
     * @return The result.
     */
    public final boolean isActive() {
        return state() == OrderState.ACTIVE;
    }

    /**
     * Check {@link OrderState}.
     * 
     * @return The result.
     */
    public final boolean isNotActive() {
        return isActive() == false;
    }

    /**
     * Check {@link OrderState}.
     * 
     * @return The result.
     */
    public final boolean isExpired() {
        return state() == OrderState.EXPIRED;
    }

    /**
     * Check {@link OrderState}.
     * 
     * @return The result.
     */
    public final boolean isNotExpired() {
        return isExpired() == false;
    }

    /**
     * Check {@link OrderState}.
     * 
     * @return The result.
     */
    public final boolean isCanceled() {
        return state() == OrderState.CANCELED;
    }

    /**
     * Check {@link OrderState}.
     * 
     * @return The result.
     */
    public final boolean isNotCanceled() {
        return isCanceled() == false;
    }

    /**
     * Check {@link OrderState}.
     * 
     * @return The result.
     */
    public final boolean isTerminated() {
        return isCompleted() || isCanceled();
    }

    /**
     * Check {@link OrderState}.
     *
     * @return The result.
     */
    public final boolean isNotTerminated() {
        return isTerminated() == false;
    }

    /**
     * Check {@link OrderState}.
     * 
     * @return The result.
     */
    public final boolean isCompleted() {
        return state() == OrderState.COMPLETED;
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
     * Retrieve the relation by type.
     * 
     * @param type A relation type.
     */
    public final <T> T relation(Class<T> type) {
        if (relations == null) {
            relations = new ConcurrentHashMap();
        }
        return (T) relations.computeIfAbsent(type, key -> I.make(type));
    }

    /**
     * Write log.
     * 
     * @param comment
     * @return
     */
    public final OrderModel log(String comment) {
        if (comment != null && !comment.isEmpty()) {
            relation(Log.class).items.add(comment);
        }
        return this;
    }

    /**
     * Write log.
     * 
     * @param comment
     * @param params
     */
    public final void log(String comment, Object... params) {
        log(String.format(comment, params));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(OrderModel o) {
        return price().compareTo(o.price());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(id());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Order ? Objects.equals(id(), ((Order) obj).id()) : false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(direction().mark());
        builder.append(executedSize()).append("/").append(size()).append("(").append(canceledSize()).append(")@").append(price());
        builder.append("\t").append(state());
        builder.append("\t").append(creationTime().toLocalDateTime()).append("～").append(terminationTime().toLocalDateTime());
        builder.append("(").append(Chrono.formatAsDuration(creationTime(), terminationTime())).append(")");

        return builder.toString();
    }

    /**
     * Log for {@link OldOrder}.
     */
    private static class Log {

        /** The actual log. */
        private final LinkedList<String> items = new LinkedList();
    }
}
