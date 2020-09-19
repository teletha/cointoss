/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.primitive;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;

public class CollectionSupport {

    /**
     * Build {@link Iterator} from {@link Spliterator} with supporting remove operation.
     * 
     * @param <V>
     * @param spliterator
     * @param remover
     * @return
     */
    public static <V> Iterator<V> iteratorFrom(Spliterator<V> spliterator, Consumer<V> remover) {
        class Adapter implements Iterator<V>, Consumer<V> {

            /** Iteration state. */
            private boolean ready = false;

            /** The next element. */
            private V next;

            /**
             * {@inheritDoc}
             */
            @Override
            public void accept(V item) {
                ready = true;
                next = item;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean hasNext() {
                if (!ready) {
                    spliterator.tryAdvance(this);
                }
                return ready;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public V next() {
                if (!ready && !hasNext()) {
                    throw new NoSuchElementException();
                } else {
                    ready = false;
                    return next;
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void remove() {
                if (remover == null) {
                    throw new UnsupportedOperationException("Remove operation is not supported.");
                } else {
                    remover.accept(next);
                }
            }
        }

        return new Adapter();
    }
}
