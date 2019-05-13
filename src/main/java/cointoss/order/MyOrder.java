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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import cointoss.Direction;
import cointoss.Directional;
import cointoss.util.Num;
import kiss.I;

public class MyOrder implements Directional {

    /** The updater. */
    private static final MethodHandle priceHandler;

    /** The updater. */
    private static final MethodHandle conditionHandler;

    static {
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
            Field field = MyOrder.class.getField(name);
            field.setAccessible(true);

            return MethodHandles.lookup().unreflectSetter(field);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /** The order direction. */
    public final Direction direction;

    /** The order size. */
    public final Num size;

    /** The order price. */
    public final Num price = Num.ZERO;

    /** The execution condition. */
    public final QuantityCondition condition = QuantityCondition.GoodTillCanceled;

    /**
     * {@inheritDoc}
     */
    @Override
    public Direction direction() {
        return direction;
    }

    /**
     * Hide consctuctor.
     * 
     * @param direction The order direction.
     * @param size The order size.
     */
    private MyOrder(Direction direction, Num size) {
        if (direction == null) {
            throw new IllegalArgumentException(MyOrder.class.getSimpleName() + " requires direction.");
        }

        if (size == null || size.isNegativeOrZero()) {
            throw new IllegalArgumentException(MyOrder.class.getSimpleName() + " requires positive size.");
        }

        this.direction = direction;
        this.size = size;
    }

    /**
     * Set your order price.
     * 
     * @param price A price to set.
     * @return Chainable API.
     */
    public final MyOrder price(long price) {
        return price(Num.of(price));
    }

    /**
     * Set your order price.
     * 
     * @param price A price to set.
     * @return Chainable API.
     */
    public final MyOrder price(double price) {
        return price(Num.of(price));
    }

    /**
     * Set your order price.
     * 
     * @param price A price to set.
     * @return Chainable API.
     */
    public final MyOrder price(Num price) {
        if (price == null || price.isNegative()) {
            price = Num.ZERO;
        }

        try {
            priceHandler.invokeExact(this, price);
        } catch (Throwable e) {
            throw I.quiet(e);
        }
        return this;
    }

    /**
     * Set the {@link QuantityCondition} of this {@link MyOrder}.
     * 
     * @param quantityCondition A {@link QuantityCondition} to set.
     * @return Chainable API.
     */
    public final MyOrder type(QuantityCondition quantityCondition) {
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
     * Build the new buying order.
     * 
     * @param size Your order size.
     * @return A new {@link MyOrder}.
     */
    public static MyOrder buy(long size) {
        return buy(Num.of(size));
    }

    /**
     * Build the new buying order.
     * 
     * @param size Your order size.
     * @return A new {@link MyOrder}.
     */
    public static MyOrder buy(double size) {
        return buy(Num.of(size));
    }

    /**
     * Build the new buying order.
     * 
     * @param size Your order size.
     * @return A new {@link MyOrder}.
     */
    public static MyOrder buy(Num size) {
        return of(Direction.BUY, size);
    }

    /**
     * Build the new selling order.
     * 
     * @param size Your order size.
     * @return A new {@link MyOrder}.
     */
    public static MyOrder sell(long size) {
        return sell(Num.of(size));
    }

    /**
     * Build the new selling order.
     * 
     * @param size Your order size.
     * @return A new {@link MyOrder}.
     */
    public static MyOrder sell(double size) {
        return sell(Num.of(size));
    }

    /**
     * Build the new selling order.
     * 
     * @param size Your order size.
     * @return A new {@link MyOrder}.
     */
    public static MyOrder sell(Num size) {
        return of(Direction.SELL, size);
    }

    /**
     * Build the new order.
     * 
     * @param direction A order direction.
     * @param size Your order size.
     * @return A new {@link MyOrder}.
     */
    public static MyOrder of(Direction direction, long size) {
        return of(direction, Num.of(size));
    }

    /**
     * Build the new order.
     * 
     * @param direction A order direction.
     * @param size Your order size.
     * @return A new {@link MyOrder}.
     */
    public static MyOrder of(Direction direction, double size) {
        return of(direction, Num.of(size));
    }

    /**
     * Build the new order.
     * 
     * @param direction A order direction.
     * @param size Your order size.
     * @return A new {@link MyOrder}.
     */
    public static MyOrder of(Direction direction, Num size) {
        return new MyOrder(direction, size);
    }
}
