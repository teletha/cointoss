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

import kiss.Variable;

@SuppressWarnings("serial")
public class LinkedQueue<E> extends LinkedList<E> {

    /**
     * Peek first item.
     * 
     * @return
     */
    public Variable<E> first() {
        return Variable.of(peekFirst());
    }

    /**
     * Peek first item.
     * 
     * @return
     */
    public Variable<E> last() {
        return Variable.of(peekLast());
    }
}
