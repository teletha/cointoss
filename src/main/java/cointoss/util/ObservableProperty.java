/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

import kiss.Signal;
import kiss.Signaling;

/**
 * 
 */
public abstract class ObservableProperty<T> implements Supplier<T>, Consumer<T> {

    private Signaling<T> signal;

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(T value) {
        if (signal != null) {
            signal.accept(value);
        }
    }

    /**
     * Observe property modification.
     * 
     * @return
     */
    public synchronized Signal<T> observe$() {
        if (signal == null) {
            signal = new Signaling();
        }
        return signal.expose;
    }

    /**
     * Observe property modification with the current value.
     * 
     * @return
     */
    public Signal<T> observe$Now() {
        return observe$().startWith(get());
    }
}