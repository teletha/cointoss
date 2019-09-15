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

import java.util.LinkedList;

import kiss.Disposable;
import kiss.Observer;

public class LinkList<E> extends LinkedList<E> implements Observer<E>, Disposable {

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(E item) {
        add(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void vandalize() {
    }
}
