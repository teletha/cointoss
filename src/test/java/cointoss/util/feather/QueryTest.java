/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.feather;

import static cointoss.util.feather.Option.Latest;

import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.util.array.IntList;
import kiss.Signal;

class QueryTest extends FeatherStoreTestBase {

    @Test
    void startTime() {
        FeatherStore<Value> store = createStore(values(0, 30), null);
        assert equality(store.query(0), "0~30");
        assert equality(store.query(1), "1~30");
        assert equality(store.query(2), "2~30");
        assert equality(store.query(10), "10~30");
        assert equality(store.query(20), "20~30");
        assert equality(store.query(30), 30);
        assert equality(store.query(40), EMPTY);
    }

    @Test
    void startTimeOnDisk() {
        FeatherStore<Value> store = createStore(null, values(0, 30));
        assert equality(store.query(0), "0~30");

        store = createStore(null, values(0, 30));
        assert equality(store.query(15), "15~30");

        store = createStore(null, values(0, 30));
        assert equality(store.query(40), EMPTY);
    }

    @Test
    void startTimeOnMemoryAndDisk() {
        FeatherStore<Value> store = createStore(values(30, 40), values(10, 30));
        assert equality(store.query(0), "10~40");

        store = createStore(values(30, 40), values(10, 30));
        assert equality(store.query(40), 40);
    }

    @Test
    void endTime() {
        FeatherStore<Value> store = createStore(values(0, 30), null);
        assert equality(store.query(0, 5), "0~5");
        assert equality(store.query(10, 15), "10~15");
        assert equality(store.query(25, 30), "25~30");
        assert equality(store.query(28, 33), 28, 29, 30);
        assert equality(store.query(35, 40), EMPTY);

        // reverse order
        assert equality(store.query(5, 0), "5~0");
        assert equality(store.query(15, 10), "15~10");
        assert equality(store.query(30, 25), "30~25");
        assert equality(store.query(33, 25), "30~25");
        assert equality(store.query(40, 35), EMPTY);
    }

    @Test
    void endTimeOnDisk() {
        FeatherStore<Value> store = createStore(null, values(0, 30));
        assert equality(store.query(0, 5), "0~5");

        store = createStore(null, values(0, 30));
        assert equality(store.query(15, 30), "15~30");

        store = createStore(null, values(0, 30));
        assert equality(store.query(40, 50), EMPTY);
    }

    @Test
    void latest() {
        FeatherStore<Value> store = createStore(values(0, 20), null);
        assert equality(store.query(10), "10~20");
        assert equality(store.query(10, Option::reverse), "10~0");
        assert equality(store.query(Latest), "20");
        assert equality(store.query(Latest, Option::reverse), "20~0");
        assert equality(store.query(10, Latest), "10~20");
        assert equality(store.query(10, Latest, Option::reverse), "20~10");
        assert equality(store.query(Latest, 10), "20~10");
        assert equality(store.query(Latest, 10, Option::reverse), "10~20");
    }

    @Test
    void reverse() {
        FeatherStore<Value> store = createStore(values(0, 30), null);
        assert equality(store.query(15, Option::reverse), "15~0");
        assert equality(store.query(Latest, Option::reverse), "30~0");

        assert equality(store.query(25, 30, Option::reverse), "30~25");
        assert equality(store.query(30, 25, Option::reverse), "25~30");
        assert equality(store.query(25, Latest, Option::reverse), "30~25");
        assert equality(store.query(Latest, 25, Option::reverse), "25~30");
    }

    @Test
    void max() {
        FeatherStore<Value> store = createStore(values(0, 30), null);
        assert equality(store.query(15, o -> o.max(10)), "15~24");
        assert equality(store.query(25, 30, o -> o.max(10)), "25~30");
        assert equality(store.query(30, 20, o -> o.max(5)), "30~26");
    }

    @Test
    void maxAndReverse() {
        FeatherStore<Value> store = createStore(values(0, 30), null);
        assert equality(store.query(15, o -> o.max(10).reverse()), "15~6");
        assert equality(store.query(25, 30, o -> o.max(10).reverse()), "30~25");
        assert equality(store.query(30, 20, o -> o.max(5).reverse()), "20~24");
    }

    @Test
    void sparseSegment() {
        FeatherStore<Value> store = createStore(value(0, 1, 7, 9, 11, 15), null);
        assert equality(store.query(0), 0, 1, 7, 9, 11, 15);
        assert equality(store.query(0, o -> o.max(5)), 0, 1, 7, 9, 11);
    }

    private static final int[] EMPTY = new int[0];

    private boolean equality(Signal<Value> query, String values) {
        IntList ints = new IntList();

        for (String segment : values.replaceAll("\\s", "").split(",")) {
            int wave = segment.indexOf("~");
            if (wave == -1) {
                ints.add(Integer.parseInt(segment));
            } else {
                int start = Integer.parseInt(segment.substring(0, wave));
                int end = Integer.parseInt(segment.substring(wave + 1));
                if (start < end) {
                    for (int i = start; i <= end; i++) {
                        ints.add(i);
                    }
                } else {
                    for (int i = start; end <= i; i--) {
                        ints.add(i);
                    }
                }
            }
        }

        List<Value> list = query.toList();
        assert list.size() == ints.size();
        for (int i = 0; i < list.size(); i++) {
            assert list.get(i).value() == ints.get(i);
        }
        return true;
    }

    private boolean equality(Signal<Value> query, int... values) {
        List<Value> list = query.toList();
        assert list.size() == values.length;
        for (int i = 0; i < list.size(); i++) {
            assert list.get(i).value() == values[i];
        }
        return true;
    }
}
