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

import java.util.Collection;

import javax.annotation.processing.Generated;

import cointoss.util.SpecializedCodeGenerator.Primitive;
import cointoss.util.SpecializedCodeGenerator.Wrapper;
import cointoss.util.SpecializedCodeGenerator.WrapperBinaryOperator;
import cointoss.util.SpecializedCodeGenerator.WrapperConsumer;

@Generated("SpecializedCodeGenerator")
public class WrapperRingBuffer<Wrapper1> {

    /** The fixed buffer size. */
    private final int size;

    /** The actual buffer. */
    private final Primitive[] buffer;

    /** The current index. */
    private int index = -1;

    /**
     * Create new buffer.
     * 
     * @param size A fixed buffer size.
     */
    public WrapperRingBuffer(int size) {
        this.size = size;
        this.buffer = Wrapper.newArray(size);
    }

    /**
     * Add an item at tail.
     * 
     * @param item An item to add.
     * @return Removed item.
     */
    public Primitive add(Primitive item) {
        int nextIndex = (index + 1) % size;
        Primitive prev = buffer[nextIndex];
        buffer[nextIndex] = item;
        index = nextIndex;
        return prev;
    }

    /**
     * Get the latest item.
     * 
     * @return A latest item.
     */
    public Primitive latest() {
        return buffer[index];
    }

    /**
     * Take all items.
     * 
     * @param consumer
     */
    public void forEach(WrapperConsumer<Wrapper1> consumer) {
        forEach(size, consumer);
    }

    /**
     * Take all items.
     * 
     * @param consumer
     */
    public void forEach(int size, WrapperConsumer<Wrapper1> consumer) {
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
    public void forEachFromLatest(WrapperConsumer<Wrapper1> consumer) {
        forEachFromLatest(size, consumer);
    }

    /**
     * Take all items from latest.
     * 
     * @param consumer
     */
    public void forEachFromLatest(int size, WrapperConsumer<Wrapper1> consumer) {
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
    public Primitive reduce(WrapperBinaryOperator<Wrapper1> operator) {
        int start = index;
        Primitive result = buffer[start];
        for (int i = 1; i < size; i++) {
            Primitive item = buffer[(start + i) % size];
            if (item != Wrapper.initital()) result = operator.applyAsWrapper(result, item);
        }
        return result;
    }

    /**
     * Recompose to the specified {@link Collection}.
     * 
     * @param <C> A collection type.
     * @param collection A target collection.
     * @return A recomposed collection.
     */
    public <C extends Collection<Wrapper>> C to(C collection) {
        forEach(v -> collection.add(v));
        return collection;
    }
}