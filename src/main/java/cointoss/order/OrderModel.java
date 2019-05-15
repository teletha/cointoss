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
import cointoss.execution.Execution;
import cointoss.util.Num;
import cointoss.util.NumVar;
import icy.manipulator.Icy;
import kiss.I;
import kiss.Signal;
import kiss.Variable;

@Icy(grouping = 2)
public abstract class OrderModel implements Directional {

    /** The requested time of this order. */
    public final Variable<ZonedDateTime> creationTime = Variable.empty();

    /** The termiated time of this order. */
    public final Variable<ZonedDateTime> terminationTime = Variable.empty();

    /** The executed size. */
    public final NumVar executedSize = NumVar.zero();

    /** The remaining size. */
    public final NumVar remainingSize = NumVar.zero();

    /** The order state. */
    public final Variable<OrderState> state = Variable.of(OrderState.INIT);

    /** The order identifier for the specific market. */
    public final Variable<String> id = Variable.empty();

    /** The total cost. */
    public final NumVar cost = NumVar.zero();

    /** The relation holder. */
    private Map<Class, Object> relations;

    /** The entry holder. */
    private final LinkedList<Execution> entries = new LinkedList();

    /**
     * {@inheritDoc}
     */
    @Icy.Property
    @Override
    public abstract Direction direction();

    @Icy.Intercept("direction")
    private Direction validate(Direction direction) {
        if (direction == null) {
            throw new IllegalArgumentException("Required");
        }
        return direction;
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
    private Num size(int size) {
        return Num.of(size);
    }

    /**
     * Set order size by value.
     * 
     * @param size An executed size.
     * @return Chainable API.
     */
    @Icy.Overload("size")
    private Num size(float size) {
        return Num.of(size);
    }

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
     * Size valication.
     * 
     * @param size
     * @return
     */
    @Icy.Intercept("size")
    private Num validateSize(Num size) {
        if (size.isNegativeOrZero()) {
            throw new IllegalArgumentException("Order size must be positive.");
        }
        remainingSize.set(size);
        return size;
    }

    /**
     * The initial ordered price.
     * 
     * @return
     */
    @Icy.Property(mutable = true)
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
    private Num price(int price) {
        return Num.of(price);
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
    private Num price(float price) {
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

        if (state.is(OrderState.INIT)) {
            type.accept(price.isZero() ? OrderType.MARKET : OrderType.LIMIT);
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
        return OrderType.MARKET;
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
     * Observe when this {@link OldOrder} will be canceled or completed.
     * 
     * @return A event {@link Signal}.
     */
    public final Signal<Order> observeTerminating() {
        return state.observe().take(OrderState.CANCELED, OrderState.COMPLETED).take(1).mapTo((Order) this);
    }

    /**
     * Register entry or exit execution.
     */
    final void execute(Execution execution) {
        entries.add(execution);
        cost.set(v -> v.plus(execution.size.multiply(execution.price)));
    }

    /**
     * Retrieve first {@link Execution}.
     * 
     * @return
     */
    @Deprecated
    public Variable<Execution> first() {
        return Variable.of(entries.peekFirst());
    }

    /**
     * Retrieve last {@link Execution}.
     * 
     * @return
     */
    @Deprecated
    public Variable<Execution> last() {
        return Variable.of(entries.peekLast());
    }

    /**
     * Retrieve all {@link Execution}s.
     * 
     * @return
     */
    @Deprecated
    public Signal<Execution> all() {
        return I.signal(entries);
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
        return direction().mark() + size() + "@" + price() + " 残" + remainingSize + " 済" + executedSize + " " + creationTime + " " + state;
    }

    /**
     * Log for {@link OldOrder}.
     */
    private static class Log {

        /** The actual log. */
        private final LinkedList<String> items = new LinkedList();
    }
}
