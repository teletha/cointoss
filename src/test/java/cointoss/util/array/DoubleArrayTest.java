/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.array;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import cointoss.util.array.DoubleArray;

class DoubleArrayTest {

    private int sizeForTest = 5000;

    @Test
    void size() {
        DoubleArray array = new DoubleArray();
        assert array.size() == 0;

        array.add(0);
        assert array.size() == 1;

        array.add(1);
        assert array.size() == 2;
    }

    @Test
    void get() {
        DoubleArray array = new DoubleArray();
        for (int i = 0; i < sizeForTest; i++) {
            array.add(i);
        }

        for (int i = 0; i < sizeForTest; i++) {
            assert array.get(i) == i;
        }
    }

    @Test
    void getOutOfIndex() {
        DoubleArray array = new DoubleArray();
        for (int i = 0; i < sizeForTest; i++) {
            assert array.get(i) == 0;
            array.set(i, i);
        }
    }

    @Test
    void set() {
        DoubleArray array = new DoubleArray();
        for (int i = 0; i < sizeForTest; i++) {
            array.set(i, i);
        }

        for (int i = 0; i < sizeForTest; i++) {
            assert array.get(i) == i;
        }
    }

    @Test
    void setNegativeIndex() {
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> new DoubleArray().set(-1, 10));
    }

    @Test
    void setLargeStep() {
        DoubleArray array = new DoubleArray();
        for (int i = 0; i < 5; i++) {
            array.set(i * 2000, i);
        }

        for (int i = 0; i < 5; i++) {
            assert array.get(i * 2000) == i;
        }
    }

    @Test
    void increment() {
        DoubleArray array = new DoubleArray();
        for (int i = 0; i < sizeForTest; i++) {
            array.increment(i, i);
        }

        for (int i = 0; i < sizeForTest; i++) {
            assert array.get(i) == i;
        }
    }

    @Test
    void incrementLargeStep() {
        DoubleArray array = new DoubleArray();
        for (int i = 0; i < 5; i++) {
            array.increment(i * 2000, i);
        }

        for (int i = 0; i < 5; i++) {
            assert array.get(i * 2000) == i;
        }
    }

    @Test
    void incrementNegativeIndex() {
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> new DoubleArray().increment(-1, 10));
    }

    @Test
    void decrement() {
        DoubleArray array = new DoubleArray();
        for (int i = 0; i < sizeForTest; i++) {
            array.decrement(i, i);
        }

        for (int i = 0; i < sizeForTest; i++) {
            assert array.get(i) == -i;
        }
    }

    @Test
    void decrementLargeStep() {
        DoubleArray array = new DoubleArray();
        for (int i = 0; i < 5; i++) {
            array.decrement(i * 2000, i);
        }

        for (int i = 0; i < 5; i++) {
            assert array.get(i * 2000) == -i;
        }
    }

    @Test
    void decrementNegativeIndex() {
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> new DoubleArray().decrement(-1, 10));
    }
}
