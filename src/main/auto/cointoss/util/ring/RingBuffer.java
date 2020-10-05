/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.ring;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;

import javax.annotation.processing.Generated;

@Generated("SpecializedCodeGenerator")
public class RingBuffer<E> {

    /** The fixed buffer size. */
    private final int size;

    /** The actual buffer. */
    private final E[] buffer;

    /** The current index. */
    private int index = -1;

    /**
     * Create new buffer.
     * 
     * @param size A fixed buffer size.
     */
    public RingBuffer(int size) {
        this.size = size;
        this.buffer = (E[]) java.lang.reflect.Array.newInstance(Object.class, size);
    }

    /**
     * Add an item at tail.
     * 
     * @param item An item to add.
     * @return Removed item.
     */
    public E add(E item) {
        int nextIndex = (index + 1) % size;
        E prev = buffer[nextIndex];
        buffer[nextIndex] = item;
        index = nextIndex;
        return prev;
    }

    /**
     * Get the latest item.
     * 
     * @return A latest item.
     */
    public E latest() {
        return buffer[index];
    }

    /**
     * Take all items.
     * 
     * @param consumer
     */
    public void forEach(Consumer<E> consumer) {
        forEach(size, consumer);
    }

    /**
     * Take all items.
     * 
     * @param consumer
     */
    public void forEach(int size, Consumer<E> consumer) {
        int start = index + 1;
        for (int i = 0; i < size; i++) {
            consumer.accept(buffer[(start + i) % this.size]);
        }
    }

    /**
     * Take all items from latest.
     * 
     * @param consumer
     */
    public void forEachFromLatest(Consumer<E> consumer) {
        forEachFromLatest(size, consumer);
    }

    /**
     * Take all items from latest.
     * 
     * @param consumer
     */
    public void forEachFromLatest(int size, Consumer<E> consumer) {
        int start = index + this.size;
        for (int i = 0; i < size; i++) {
            consumer.accept(buffer[(start - i) % this.size]);
        }
    }

    /**
     * Reduce items.
     * 
     * @param operator The calculation.
     * @return The reduced result.
     */
    public E reduce(BinaryOperator<E> operator) {
        int start = index;
        E result = buffer[start];
        for (int i = 1; i < size; i++) {
            E item = buffer[(start + i) % size];
            if (item != null) result = operator.apply(result, item);
        }
        return result;
    }

    /**
     * Recompose to {@link List}.
     * 
     * @return
     */
    public List<E> toList() {
        List list = new ArrayList();
        int start = index + 1;
        for (int i = 0; i < size; i++) {
            list.add(buffer[(start + i) % this.size]);
        }
        return list;
    }
}