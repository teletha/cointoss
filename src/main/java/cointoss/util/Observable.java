/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.function.Consumer;

import kiss.I;
import kiss.Signal;
import kiss.Signaling;
import kiss.WiseConsumer;
import kiss.Ⅱ;

public class Observable<V> {

    /**
     * Create special property updater.
     *
     * @param name A target property name.
     * @return A special property updater.
     */
    private static final MethodHandle updater(String name) {
        try {
            Field field = Observable.class.getDeclaredField(name);
            field.setAccessible(true);
            return MethodHandles.lookup().unreflectSetter(field);
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }

    /** The final property updater. */
    private static final MethodHandle set = updater("v");

    /** The current value. */
    public final V v;

    /** Setter interface. */
    private WiseConsumer<V> setter = v -> {
        set.invoke(this, v);
    };

    /** Event manager. */
    private Signaling<V> events;

    /**
     * Initialize.
     * 
     * @param value
     */
    protected Observable(V value) {
        this.v = value;
    }

    /**
     * Observe value modification.
     * 
     * @return
     */
    public synchronized Signal<V> observe() {
        if (events == null) {
            events = new Signaling();
        }
        return events.expose;
    }

    /**
     * Observe value modification.
     * 
     * @return
     */
    public Signal<V> observeNow() {
        return observe().startWith(v);
    }

    /**
     * Specialize for {@link Num} diff.
     * 
     * @return
     */
    public Signal<Num> diff() {
        return observeNow().as(Num.class).maps(Num.ZERO, (prev, next) -> prev.minus(next));
    }

    /**
     * Create {@link Observable} with initial value.
     * 
     * @param <V>
     * @param value
     * @return
     */
    public static <V> Ⅱ<Observable<V>, Consumer<V>> of(V value) {
        Observable view = new Observable(value);
        return I.pair(view, view.setter);
    }
}
