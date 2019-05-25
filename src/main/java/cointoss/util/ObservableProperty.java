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

import java.util.function.Consumer;
import java.util.function.Supplier;

import kiss.Signal;
import kiss.Signaling;

/**
 * 
 */
public abstract class ObservableProperty<T> implements Supplier<T>, Consumer<T> {

    private final Signaling<T> signal = new Signaling();

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(T value) {
        signal.accept(value);
    }

    /**
     * Observe property modification.
     * 
     * @return
     */
    public Signal<T> observe$() {
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