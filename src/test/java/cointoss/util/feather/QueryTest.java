/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util.feather;

import static cointoss.util.feather.Option.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import cointoss.ticker.Span;
import kiss.Signal;
import primavera.array.IntList;

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

        FeatherStore<Value> store2 = createStore(values(30, 40), values(10, 30));
        assert equality(store2.query(40), 40);
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
    void endTimeOnMemoryAndDisk() {
        FeatherStore<Value> store = createStore(values(30, 50), values(20, 30));
        assert equality(store.query(20, 40), "20~40");
        assert equality(store.query(10, 40), "20~40");
        assert equality(store.query(0, 40), "20~40");
    }

    @Test
    void rangeUnderflowMemory() {
        FeatherStore<Value> store = createStore(values(30, 40), null);
        assert equality(store.query(0, 10), EMPTY);
        assert equality(store.query(0, 20), EMPTY);
        assert equality(store.query(0, 30), 30);
        assert equality(store.query(0, 40), "30~40");
        assert equality(store.query(0, 50), "30~40");
    }

    @Test
    void rangeOverflowMemory() {
        FeatherStore<Value> store = createStore(values(30, 40), null);
        assert equality(store.query(50, 70), EMPTY);
        assert equality(store.query(40, 70), 40);
        assert equality(store.query(30, 70), "30~40");
        assert equality(store.query(20, 70), "30~40");
    }

    @Test
    void rangeUnderflowDisk() {
        FeatherStore<Value> store = createStore(null, values(30, 40));
        assert equality(store.query(0, 10), EMPTY);
        assert equality(store.query(0, 20), EMPTY);
        assert equality(store.query(0, 30), 30);
        assert equality(store.query(0, 40), "30~40");
        assert equality(store.query(0, 50), "30~40");
    }

    @Test
    void rangeOverflowDisk() {
        FeatherStore<Value> store = createStore(null, values(30, 40));
        assert equality(store.query(50, 70), EMPTY);
        assert equality(store.query(40, 70), 40);
        assert equality(store.query(30, 70), "30~40");
        assert equality(store.query(20, 70), "30~40");
    }

    @Test
    void rangeUnderflowMemoryAndDisk() {
        FeatherStore<Value> store = createStore(values(20, 30), values(30, 40));
        assert equality(store.query(0, 10), EMPTY);
        assert equality(store.query(0, 20), 20);
        assert equality(store.query(0, 30), "20~30");
        assert equality(store.query(0, 40), "20~40");
        assert equality(store.query(0, 50), "20~40");
    }

    @Test
    void rangeOverflowMemoryAndDisk() {
        FeatherStore<Value> store = createStore(values(20, 30), values(30, 40));
        assert equality(store.query(50, 70), EMPTY);
        assert equality(store.query(40, 70), 40);
        assert equality(store.query(30, 70), "30~40");
        assert equality(store.query(20, 70), "20~40");
        assert equality(store.query(10, 70), "20~40");
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
    void excludeStart() {
        FeatherStore<Value> store = createStore(values(0, 30), null);
        assert equality(store.query(15, Option::exclude), "16~30");
        assert equality(store.query(25, 30, Option::exclude), "26~30");
        assert equality(store.query(30, 20, Option::exclude), "29~20");
    }

    @Test
    void excludeStartAndReverse() {
        FeatherStore<Value> store = createStore(values(0, 30), null);
        assert equality(store.query(15, o -> o.exclude().reverse()), "14~0");
        assert equality(store.query(25, 30, o -> o.exclude().reverse()), "29~25");
        assert equality(store.query(30, 20, o -> o.exclude().reverse()), "21~30");
        assert equality(store.query(25, 40, o -> o.exclude().reverse()), "30~25");
    }

    @Test
    void sparseSegment() {
        FeatherStore<Value> store = createStore(value(0, 1, 7, 9, 11, 15), null);
        assert equality(store.query(0), 0, 1, 7, 9, 11, 15);
        assert equality(store.query(0, 6), 0, 1);
        assert equality(store.query(3, 13), 7, 9, 11);
        assert equality(store.query(15, o -> o.reverse()), 15, 11, 9, 7, 1, 0);
        assert equality(store.query(0, o -> o.max(5)), 0, 1, 7, 9, 11);
    }

    @Test
    void before() {
        FeatherStore<Value> store = createStore(Span.Minute1, value(0, 60, 120, 180), null);
        assert equality(store.query(0, o -> o.before()), EMPTY);
        assert equality(store.query(36, o -> o.before().max(2)), EMPTY);
        assert equality(store.query(60, o -> o.before()), 0);
        assert equality(store.query(72, o -> o.before().max(2)), 0);
        assert equality(store.query(120, o -> o.before().max(1)), 60);
        assert equality(store.query(144, o -> o.before()), 60, 0);
        assert equality(store.query(180, o -> o.before().max(3)), 120, 60, 0);
        assert equality(store.query(240, o -> o.before().max(2)), 180, 120);
        assert equality(store.query(300, o -> o.before().max(5)), 180, 120, 60, 0);
    }

    @Test
    void beforeOverTime() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Day);
        store.store(day(0), day(1), day(2), day(3), day(4));
        assert equality(store.query(0, o -> o.before().max(1)), EMPTY);
        assert equality(store.query(days - 1, o -> o.before().max(2)), EMPTY);
        assert equality(store.query(days, o -> o.before().max(1)), 0);
        assert equality(store.query(days + 1, o -> o.before().max(2)), 0);
        assert equality(store.query(2 * days - 1, o -> o.before().max(3)), 0);
        assert equality(store.query(2 * days, o -> o.before().max(1)), days);
        assert equality(store.query(2 * days + 1, o -> o.before().max(3)), days, 0);
        assert equality(store.query(4 * days, o -> o.before().max(3)), 3 * days, 2 * days, 1 * days);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 20, 60, 100, 150, 200})
    void loadFromDisk(int offset) {
        FeatherStore<Value> store = createStore(Span.Day, null, List.of(day(offset), day(offset + 1), day(offset + 2)));
        assert equality(store.query(offset), day(offset), day(offset + 1), day(offset + 2));
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
            assert list.get(i).item == ints.get(i);
        }
        return true;
    }

    private boolean equality(Signal<Value> query, int... values) {
        List<Value> list = query.toList();
        assert list.size() == values.length;
        for (int i = 0; i < list.size(); i++) {
            assert list.get(i).item == values[i];
        }
        return true;
    }

    private boolean equality(Signal<Value> query, Value... values) {
        List<Value> list = query.toList();
        assert list.size() == values.length;
        for (int i = 0; i < list.size(); i++) {
            assert list.get(i).equals(values[i]);
        }
        return true;
    }
}