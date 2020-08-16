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

import org.junit.jupiter.api.Test;

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
    void setLargeStep() {
        DoubleArray array = new DoubleArray();
        for (int i = 0; i < sizeForTest; i += sizeForTest / 2) {
            array.set(i, i);
        }

        for (int i = 0; i < sizeForTest; i += sizeForTest / 2) {
            assert array.get(i) == i;
        }
    }
}
