/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.execution.Execution;
import cointoss.util.Chrono;
import cointoss.util.Num;
import kiss.I;
import kiss.Signal;
import kiss.Variable;

public class Order implements Directional {

    /** The updater. */
    private static final MethodHandle priceHandler;

    /** The updater. */
    private static final MethodHandle typeHandler;

    /** The updater. */
    private static final MethodHandle conditionHandler;

    static {
        typeHandler = find("type");
        priceHandler = find("price");
        conditionHandler = find("condition");
    }

    /**
     * Find field updater.
     * 
     * @param name A field name.
     * @return An updater.
     */
    private static MethodHandle find(String name) {
        try {
            Field field = Order.class.getField(name);
            field.setAccessible(true);

            return MethodHandles.lookup().unreflectSetter(field);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /** The ordered position. */
    public final Direction direction;

    /** The ordered size. */
    public final Num size;

    /** The ordered price. */
    public final Num price = Num.ZERO;

    /** The order type */
    public final OrderType type = OrderType.MARKET;

    /** The quantity conditions enforcement. */
    public final QuantityCondition condition = QuantityCondition.GoodTillCanceled;

    /** The order identifier for the specific market. */
    public final Variable<String> id = Variable.empty();

    /** The order state */
    public final Variable<OrderState> state = Variable.of(OrderState.INIT);

    /** The requested time of this {@link Order}. */
    public final Variable<ZonedDateTime> creationTime = Variable.of(Chrono.utcNow());

    /** The terminated time of this {@link Order}. */
    public final Variable<ZonedDateTime> terminationTime = Variable.empty();

    /** The executed size */
    public final Variable<Num> executedSize = Variable.of(Num.ZERO);

    /** The remaining size */
    public final Variable<Num> remainingSize = Variable.empty();

    /** The relation holder. */
    private Map<Class, Object> relations;

    /** The entry holder. */
    private final LinkedList<Execution> entries = new LinkedList();

    /** The exit holder. */
    private final LinkedList<Order> exits = new LinkedList();

    /**
     * Hide constructor.
     * 
     * @param direction A order direction.
     * @param size A order size.
     */
    protected Order(Direction direction, Num size) {
        if (direction == null) {
            throw new IllegalArgumentException(Order.class.getSimpleName() + " requires direction.");
        }

        if (size == null || size.isNegativeOrZero()) {
            throw new IllegalArgumentException(Order.class.getSimpleName() + " requires positive size.");
        }

        this.direction = direction;
        this.size = size;
        this.remainingSize.set(size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Direction direction() {
        return direction;
    }

    /**
     * Set your order price.
     * 
     * @param price A price to set.
     * @return Chainable API.
     */
    public final Order price(long price) {
        return price(Num.of(price));
    }

    /**
     * Set your order price.
     * 
     * @param price A price to set.
     * @return Chainable API.
     */
    public final Order price(double price) {
        return price(Num.of(price));
    }

    /**
     * Set your order price.
     * 
     * @param price A price to set.
     * @return Chainable API.
     */
    public final Order price(Num price) {
        if (price == null || price.isNegative()) {
            price = Num.ZERO;
        }

        try {
            priceHandler.invokeExact(this, price);

            if (state.is(OrderState.INIT)) {
                typeHandler.invoke(this, price.isZero() ? OrderType.MARKET : OrderType.LIMIT);
            }
        } catch (Throwable e) {
            throw I.quiet(e);
        }
        return this;
    }

    /**
     * For method reference.
     * 
     * @return
     */
    public final Variable<OrderState> state() {
        return state;
    }

    /**
     * Set the {@link QuantityCondition} of this {@link Order}.
     * 
     * @param quantityCondition A {@link QuantityCondition} to set.
     * @return Chainable API.
     */
    public final Order type(QuantityCondition quantityCondition) {
        if (quantityCondition == null) {
            quantityCondition = QuantityCondition.GoodTillCanceled;
        }

        try {
            conditionHandler.invoke(this, quantityCondition == null ? QuantityCondition.GoodTillCanceled : quantityCondition);
        } catch (Throwable e) {
            throw I.quiet(e);
        }
        return this;
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
    public final Order log(String comment) {
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
    public void log(String comment, Object... params) {
        log(String.format(comment, params));
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
     * Register entry or exit execution.
     */
    final void execute(Execution execution) {
        entries.add(execution);
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
        return direction().mark() + size + "@" + price + " 残" + remainingSize + " 済" + executedSize + " " + creationTime;
    }

    /**
     * Build the new buying order.
     * 
     * @param size Your order size.
     * @return A new {@link Order}.
     */
    public static Order buy(long size) {
        return buy(Num.of(size));
    }

    /**
     * Build the new buying order.
     * 
     * @param size Your order size.
     * @return A new {@link Order}.
     */
    public static Order buy(double size) {
        return buy(Num.of(size));
    }

    /**
     * Build the new buying order.
     * 
     * @param size Your order size.
     * @return A new {@link Order}.
     */
    public static Order buy(Num size) {
        return of(Direction.BUY, size);
    }

    /**
     * Build the new selling order.
     * 
     * @param size Your order size.
     * @return A new {@link Order}.
     */
    public static Order sell(long size) {
        return sell(Num.of(size));
    }

    /**
     * Build the new selling order.
     * 
     * @param size Your order size.
     * @return A new {@link Order}.
     */
    public static Order sell(double size) {
        return sell(Num.of(size));
    }

    /**
     * Build the new selling order.
     * 
     * @param size Your order size.
     * @return A new {@link Order}.
     */
    public static Order sell(Num size) {
        return of(Direction.SELL, size);
    }

    /**
     * Build the new order.
     * 
     * @param direction A order direction.
     * @param size Your order size.
     * @return A new {@link Order}.
     */
    public static Order of(Direction direction, long size) {
        return of(direction, Num.of(size));
    }

    /**
     * Build the new order.
     * 
     * @param direction A order direction.
     * @param size Your order size.
     * @return A new {@link Order}.
     */
    public static Order of(Direction direction, double size) {
        return of(direction, Num.of(size));
    }

    /**
     * Build the new order.
     * 
     * @param direction A order direction.
     * @param size Your order size.
     * @return A new {@link Order}.
     */
    public static Order of(Direction direction, Num size) {
        return new Order(direction, size);
    }

    /**
     * Log for {@link Order}.
     */
    private static class Log {

        /** The actual log. */
        private final LinkedList<String> items = new LinkedList();
    }
}
