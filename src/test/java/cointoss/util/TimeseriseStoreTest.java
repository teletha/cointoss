/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.ticker.Span;

class TimeseriseStoreTest {

    @Test
    void isEmpty() {
        TimeseriseStore<Integer> buffer = new TimeseriseStore<>(Span.Minute1, Integer::longValue);
        assert buffer.isEmpty();

        buffer.store(1);
        assert buffer.isEmpty() == false;
    }

    @Test
    void isNotEmpty() {
        TimeseriseStore<Integer> buffer = new TimeseriseStore<>(Span.Minute1, Integer::longValue);
        assert buffer.isNotEmpty() == false;

        buffer.store(1);
        assert buffer.isNotEmpty();
    }

    @Test
    void add() {
        TimeseriseStore<Integer> buffer = new TimeseriseStore<>(Span.Second5, Integer::longValue);
        buffer.store(0);
        assert buffer.getByTime(0) == 0;
        assert buffer.getByTime(5) == null;
        assert buffer.getByTime(10) == null;

        // update
        buffer.store(2);
        assert buffer.getByTime(0) == 2;
        assert buffer.getByTime(5) == null;
        assert buffer.getByTime(10) == null;

        // add next stamp
        buffer.store(5);
        assert buffer.getByTime(0) == 2;
        assert buffer.getByTime(5) == 5;
        assert buffer.getByTime(10) == null;

        // add next stamp
        buffer.store(10);
        assert buffer.getByTime(0) == 2;
        assert buffer.getByTime(5) == 5;
        assert buffer.getByTime(10) == 10;

        // update
        buffer.store(13);
        assert buffer.getByTime(0) == 2;
        assert buffer.getByTime(5) == 5;
        assert buffer.getByTime(10) == 13;
    }

    @Test
    void get() {
        TimeseriseStore<Integer> buffer = new TimeseriseStore<>(Span.Second5, Integer::longValue);
        buffer.store(0);
        assert buffer.getByIndex(0) == 0;
        assert buffer.getByIndex(5) == null;
        assert buffer.getByIndex(10) == null;
    }

    @Test
    void first() {
        TimeseriseStore<Integer> buffer = new TimeseriseStore<>(Span.Second5, Integer::longValue);
        buffer.store(25);
        assert buffer.first() == 25;

        buffer.store(30);
        assert buffer.first() == 25;

        buffer.store(15);
        assert buffer.first() == 15;

        buffer.store(17);
        assert buffer.first() == 17;

        buffer.store(13);
        assert buffer.first() == 13;

        buffer.store(10);
        assert buffer.first() == 10;
    }

    @Test
    void last() {
        TimeseriseStore<Integer> buffer = new TimeseriseStore<>(Span.Second5, Integer::longValue);
        buffer.store(5);
        assert buffer.last() == 5;

        buffer.store(10);
        assert buffer.last() == 10;

        buffer.store(12);
        assert buffer.last() == 12;

        buffer.store(20);
        assert buffer.last() == 20;

        buffer.store(16);
        assert buffer.last() == 20;

        buffer.store(30);
        assert buffer.last() == 30;
    }

    @Test
    void size() {
        TimeseriseStore<Integer> buffer = new TimeseriseStore<>(Span.Second5, Integer::longValue);
        assert buffer.size() == 0;

        buffer.store(5);
        assert buffer.size() == 1;

        buffer.store(10);
        assert buffer.size() == 2;

        // update will not modify size
        buffer.store(5);
        buffer.store(6);
        buffer.store(7);
        assert buffer.size() == 2;
    }

    @Test
    void eachAt() {
        TimeseriseStore<Integer> buffer = new TimeseriseStore<>(Span.Second5, Integer::longValue);
        buffer.store(5, 10, 15, 20, 25, 30, 35, 40);

        List<Integer> list = new ArrayList();
        buffer.eachAt(10, 35, list::add);
        assertIterableEquals(List.of(10, 15, 20, 25, 30, 35), list);

        list = new ArrayList();
        buffer.eachAt(10, 34, list::add);
        assertIterableEquals(List.of(10, 15, 20, 25, 30), list);

        list = new ArrayList();
        buffer.eachAt(10, 36, list::add);
        assertIterableEquals(List.of(10, 15, 20, 25, 30, 35), list);

        list = new ArrayList();
        buffer.eachAt(0, 15, list::add);
        assertIterableEquals(List.of(5, 10, 15), list);

        list = new ArrayList();
        buffer.eachAt(100, 150, list::add);
        assertIterableEquals(List.of(), list);
    }
}
