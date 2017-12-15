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

import kiss.Observer;

/**
 * Listener support.
 * 
 * @version 2017/12/13 9:18:41
 */
@SuppressWarnings("serial")
public final class Listeners<E> extends CopyOnWriteArrayList<Observer<? super E>> {

    /**
     * Omit your event.
     * 
     * @param event
     */
    public void omit(E event) {
        for (Observer<? super E> observer : this) {
            observer.accept(event);
        }
    }
}