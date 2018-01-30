/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import kiss.Observer;

/**
 * Listener support.
 * 
 * @version 2018/01/30 15:40:58
 */
@SuppressWarnings("serial")
public final class Listeners<E> extends CopyOnWriteArrayList<Observer<? super E>> implements Consumer<E> {

    /**
     * Omit your event.
     * 
     * @param event
     */
    @Override
    public void accept(E event) {
        for (Observer<? super E> observer : this) {
            observer.accept(event);
        }
    }

}