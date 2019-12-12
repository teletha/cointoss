/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import cointoss.ticker.TimeSpan;
import cointoss.ticker.TimeseriesStore;

class TimeseriesStoreTest {

    @Test
    void isEmpty() {
        TimeseriesStore<Integer> store = new TimeseriesStore<>(TimeSpan.Minute1, Integer::longValue);
        assert store.isEmpty();

        store.store(1);
        assert store.isEmpty() == false;
    }

    @Test
    void isNotEmpty() {
        TimeseriesStore<Integer> store = new TimeseriesStore<>(TimeSpan.Minute1, Integer::longValue);
        assert store.isNotEmpty() == false;

        store.store(1);
        assert store.isNotEmpty();
    }

    @Test
    void add() {
        TimeseriesStore<Integer> store = new TimeseriesStore<>(TimeSpan.Second5, Integer::longValue);
        store.store(0);
        assert store.getByTime(0) == 0;
        assert store.getByTime(5) == null;
        assert store.getByTime(10) == null;

        // update
        store.store(2);
        assert store.getByTime(0) == 2;
        assert store.getByTime(5) == null;
        assert store.getByTime(10) == null;

        // add next stamp
        store.store(5);
        assert store.getByTime(0) == 2;
        assert store.getByTime(5) == 5;
        assert store.getByTime(10) == null;

        // add next stamp
        store.store(10);
        assert store.getByTime(0) == 2;
        assert store.getByTime(5) == 5;
        assert store.getByTime(10) == 10;

        // update
        store.store(13);
        assert store.getByTime(0) == 2;
        assert store.getByTime(5) == 5;
        assert store.getByTime(10) == 13;
    }

    @Test
    void getByIndex() {
        TimeseriesStore<Integer> store = new TimeseriesStore<>(TimeSpan.Second5, Integer::longValue);
        store.store(0);
        assert store.getByIndex(0) == 0;
        assert store.getByIndex(5) == null;
        assert store.getByIndex(10) == null;
    }

    @Test
    void first() {
        TimeseriesStore<Integer> store = new TimeseriesStore<>(TimeSpan.Second5, Integer::longValue);
        store.store(25);
        assert store.first() == 25;

        store.store(30);
        assert store.first() == 25;

        store.store(15);
        assert store.first() == 15;

        store.store(17);
        assert store.first() == 17;

        store.store(13);
        assert store.first() == 13;

        store.store(10);
        assert store.first() == 10;
    }

    @Test
    void last() {
        TimeseriesStore<Integer> store = new TimeseriesStore<>(TimeSpan.Second5, Integer::longValue);
        store.store(5);
        assert store.last() == 5;

        store.store(10);
        assert store.last() == 10;

        store.store(12);
        assert store.last() == 12;

        store.store(20);
        assert store.last() == 20;

        store.store(16);
        assert store.last() == 20;

        store.store(30);
        assert store.last() == 30;
    }

    @Test
    void size() {
        TimeseriesStore<Integer> store = new TimeseriesStore<>(TimeSpan.Second5, Integer::longValue);
        assert store.size() == 0;

        store.store(5);
        assert store.size() == 1;

        store.store(10);
        assert store.size() == 2;

        // update will not modify size
        store.store(5);
        store.store(6);
        store.store(7);
        assert store.size() == 2;
    }

    @Test
    void eachAt() {
        TimeseriesStore<Integer> store = new TimeseriesStore<>(TimeSpan.Second5, Integer::longValue);
        store.store(5, 10, 15, 20, 25, 30, 35, 40);

        List<Integer> list = new ArrayList();
        store.eachByTime(10, 35, list::add);
        assertIterableEquals(List.of(10, 15, 20, 25, 30, 35), list);

        list = new ArrayList();
        store.eachByTime(10, 34, list::add);
        assertIterableEquals(List.of(10, 15, 20, 25, 30), list);

        list = new ArrayList();
        store.eachByTime(10, 36, list::add);
        assertIterableEquals(List.of(10, 15, 20, 25, 30, 35), list);

        list = new ArrayList();
        store.eachByTime(0, 15, list::add);
        assertIterableEquals(List.of(5, 10, 15), list);

        list = new ArrayList();
        store.eachByTime(100, 150, list::add);
        assertIterableEquals(List.of(), list);
    }

    @Test
    void calculateStartTimeAndRemainderEpochSeconds() {
        TimeseriesStore<Integer> store = new TimeseriesStore<>(TimeSpan.Minute1, Integer::longValue);

        // 2019-12-12 02:16:30
        Assertions.assertArrayEquals(new long[] {1576108800, 136}, store.index(1576116990));
        // 2019-12-13 00:00:00
        Assertions.assertArrayEquals(new long[] {1576195200, 0}, store.index(1576195200));
        // 2019-12-13 00:00:59
        Assertions.assertArrayEquals(new long[] {1576195200, 0}, store.index(1576195259));
        // 2019-12-13 00:01:00
        Assertions.assertArrayEquals(new long[] {1576195200, 1}, store.index(1576195260));
    }
}
