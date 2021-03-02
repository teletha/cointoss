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

class QueryTest {

    private FeatherStore<Int> createFilledStore(int start, int end) {
        FeatherStore<Int> store = FeatherStore.create(Int.class, 1, 10, 10);
        for (int i = start; i <= end; i++) {
            store.store(new Int(i));
        }
        return store;
    }

    @Test
    void startTime() {
        FeatherStore<Int> store = createFilledStore(0, 30);
        assert equality(store.query(0), "0~30");
        assert equality(store.query(1), "1~30");
        assert equality(store.query(2), "2~30");
        assert equality(store.query(10), "10~30");
        assert equality(store.query(20), "20~30");
        assert equality(store.query(30), 30);
        assert equality(store.query(40), EMPTY);

        // reverse order
        assert equality(store.query(Latest, 0), "30~0");
        assert equality(store.query(Latest, 20), "30~20");
    }

    @Test
    void endTime() {
        FeatherStore<Int> store = createFilledStore(0, 30);
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
    void reverse() {
        FeatherStore<Int> store = createFilledStore(0, 30);
        assert equality(store.query(15), "15~30");
        assert equality(store.query(15), "15~0");
        assert equality(store.query(Latest), 30);

        assert equality(store.query(25, Latest), "25~30");
        assert equality(store.query(30, 25), "30~25");
        assert equality(store.query(25, 30), "30~25");
        assert equality(store.query(30, 25), "25~35");
    }

    private static final int[] EMPTY = new int[0];

    private boolean equality(Signal<Int> query, String values) {
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

        List<Int> list = query.toList();
        assert list.size() == ints.size();
        for (int i = 0; i < list.size(); i++) {
            assert list.get(i).value == ints.get(i);
        }
        return true;
    }

    private boolean equality(Signal<Int> query, int... values) {
        List<Int> list = query.toList();
        assert list.size() == values.length;
        for (int i = 0; i < list.size(); i++) {
            assert list.get(i).value == values[i];
        }
        return true;
    }

    @SuppressWarnings("preview")
    record Int(int value) implements TemporalData {

        @Override
        public long seconds() {
            return value;
        }
    }
}
