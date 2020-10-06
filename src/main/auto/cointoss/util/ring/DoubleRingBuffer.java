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

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;

@Generated("SpecializedCodeGenerator")
public class DoubleRingBuffer {

    /** The fixed buffer size. */
    private final int size;

    /** The actual buffer. */
    private final double[] buffer;

    /** The current index. */
    private int index = -1;

    /**
     * Create new buffer.
     * 
     * @param size A fixed buffer size.
     */
    public DoubleRingBuffer(int size) {
        this.size = size;
        this.buffer = new double[size];
    }

    /**
     * Add an item at tail.
     * 
     * @param item An item to add.
     * @return Removed item.
     */
    public double add(double item) {
        int nextIndex = (index + 1) % size;
        double prev = buffer[nextIndex];
        buffer[nextIndex] = item;
        index = nextIndex;
        return prev;
    }

    /**
     * Get the latest item.
     * 
     * @return A latest item.
     */
    public double latest() {
        return buffer[index];
    }

    /**
     * Take all items.
     * 
     * @param consumer
     */
    public void forEach(DoubleConsumer consumer) {
        forEach(size, consumer);
    }

    /**
     * Take all items.
     * 
     * @param consumer
     */
    public void forEach(int size, DoubleConsumer consumer) {
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
    public void forEachFromLatest(DoubleConsumer consumer) {
        forEachFromLatest(size, consumer);
    }

    /**
     * Take all items from latest.
     * 
     * @param consumer
     */
    public void forEachFromLatest(int size, DoubleConsumer consumer) {
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
    public double reduce(DoubleBinaryOperator operator) {
        int start = index;
        double result = buffer[start];
        for (int i = 1; i < size; i++) {
            double item = buffer[(start + i) % size];
            if (item != 0d) result = operator.applyAsDouble(result, item);
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
    public <C extends Collection<Double>> C to(C collection) {
        forEach(v -> collection.add(v));
        return collection;
    }
}