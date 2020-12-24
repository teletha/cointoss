/*
 * Copyright (C) 2020 cointoss Development Team
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
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import kiss.I;
import psychopath.Directory;
import psychopath.File;
import psychopath.Locator;

class TimeseriesStoreTest {
    int days = 60 * 60 * 24;

    @Test
    void isEmpty() {
        TimeseriesStore<Integer> store = TimeseriesStore.create(int.class, Integer::longValue, Span.Minute1);
        assert store.isEmpty();

        store.store(1);
        assert store.isEmpty() == false;
    }

    @Test
    void isNotEmpty() {
        TimeseriesStore<Integer> store = TimeseriesStore.create(int.class, Integer::longValue, Span.Minute1);
        assert store.isNotEmpty() == false;

        store.store(1);
        assert store.isNotEmpty();
    }

    @Test
    void add() {
        TimeseriesStore<Integer> store = TimeseriesStore.create(int.class, Integer::longValue, Span.Second5);
        store.store(0);
        assert store.at(0) == 0;
        assert store.at(5) == null;
        assert store.at(10) == null;

        // update
        store.store(2);
        assert store.at(0) == 2;
        assert store.at(5) == null;
        assert store.at(10) == null;

        // add next stamp
        store.store(5);
        assert store.at(0) == 2;
        assert store.at(5) == 5;
        assert store.at(10) == null;

        // add next stamp
        store.store(10);
        assert store.at(0) == 2;
        assert store.at(5) == 5;
        assert store.at(10) == 10;

        // update
        store.store(13);
        assert store.at(0) == 2;
        assert store.at(5) == 5;
        assert store.at(10) == 13;
    }

    @Test
    void getByTime() {
        TimeseriesStore<Integer> store = TimeseriesStore.create(int.class, Integer::longValue, Span.Second5);
        store.store(0, 5, 10);
        assert store.at(0) == 0;
        assert store.at(3) == 0;
        assert store.at(5) == 5;
        assert store.at(7) == 5;
        assert store.at(10) == 10;
        assert store.at(14) == 10;
    }

    @Test
    void getByTimeOverTime() {
        TimeseriesStore<Integer> store = TimeseriesStore.create(int.class, Integer::longValue, Span.Day1);
        store.store(0, days, 2 * days, 3 * days, 4 * days);
        assert store.at(0) == 0;
        assert store.at(days - 1) == 0;
        assert store.at(days) == days;
        assert store.at(days + 1) == days;
        assert store.at(2 * days - 1) == days;
        assert store.at(2 * days) == 2 * days;
        assert store.at(2 * days + 1) == 2 * days;
    }

    @Test
    void first() {
        TimeseriesStore<Integer> store = TimeseriesStore.create(int.class, Integer::longValue, Span.Second5);
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
    void firstOverDays() {
        TimeseriesStore<Integer> store = TimeseriesStore.create(int.class, Integer::longValue, Span.Day1);
        store.store(0, days, 2 * days, 3 * days, 4 * days);
        assert store.first() == 0;
    }

    @Test
    void last() {
        TimeseriesStore<Integer> store = TimeseriesStore.create(int.class, Integer::longValue, Span.Second5);
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
    void lastOverDays() {
        TimeseriesStore<Integer> store = TimeseriesStore.create(int.class, Integer::longValue, Span.Day1);
        store.store(0, days, 2 * days, 3 * days, 4 * days);
        assert store.last() == 4 * days;
    }

    @Test
    void size() {
        TimeseriesStore<Integer> store = TimeseriesStore.create(int.class, Integer::longValue, Span.Second5);
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
    void sizeOverDays() {
        TimeseriesStore<Integer> store = TimeseriesStore.create(int.class, Integer::longValue, Span.Day1);
        store.store(0, days, 2 * days, 3 * days, 4 * days);
        assert store.size() == 5;
    }

    @Test
    void each() {
        // padding right
        TimeseriesStore<Integer> store = TimeseriesStore.create(int.class, Integer::longValue, Span.Second5);
        store.store(0, 5, 10, 15, 20, 25, 30);

        List<Integer> list = new ArrayList();
        store.each(list::add);
        assertIterableEquals(List.of(0, 5, 10, 15, 20, 25, 30), list);

        // padding both sides
        store = TimeseriesStore.create(int.class, Integer::longValue, Span.Second5);
        store.store(15, 20, 25, 30);

        list = new ArrayList();
        store.each(list::add);
        assertIterableEquals(List.of(15, 20, 25, 30), list);

        // padding left side
        store = TimeseriesStore.create(int.class, Integer::longValue, Span.Hour4);
        store.store(3600 * 12, 3600 * 16, 3600 * 20);

        list = new ArrayList();
        store.each(list::add);
        assertIterableEquals(List.of(3600 * 12, 3600 * 16, 3600 * 20), list);
    }

    @Test
    void eachOverDays() {
        TimeseriesStore<Integer> store = TimeseriesStore.create(int.class, Integer::longValue, Span.Day1);
        store.store(0, days, 2 * days, 3 * days, 4 * days);

        List<Integer> list = new ArrayList();
        store.each(list::add);
        assertIterableEquals(List.of(0, days, 2 * days, 3 * days, 4 * days), list);
    }

    @Test
    void eachByTime() {
        TimeseriesStore<Integer> store = TimeseriesStore.create(int.class, Integer::longValue, Span.Second5);
        store.store(5, 10, 15, 20, 25, 30, 35, 40);

        List<Integer> list = new ArrayList();
        store.each(10, 35, list::add);
        assertIterableEquals(List.of(10, 15, 20, 25, 30, 35), list);

        list = new ArrayList();
        store.each(10, 34, list::add);
        assertIterableEquals(List.of(10, 15, 20, 25, 30), list);

        list = new ArrayList();
        store.each(10, 36, list::add);
        assertIterableEquals(List.of(10, 15, 20, 25, 30, 35), list);

        list = new ArrayList();
        store.each(0, 15, list::add);
        assertIterableEquals(List.of(5, 10, 15), list);

        list = new ArrayList();
        store.each(100, 150, list::add);
        assertIterableEquals(List.of(), list);
    }

    @Test
    void eachByTimeOverDays() {
        TimeseriesStore<Integer> store = TimeseriesStore.create(int.class, Integer::longValue, Span.Day1);
        store.store(0, days, 2 * days, 3 * days, 4 * days);

        List<Integer> list = new ArrayList();
        store.each(0, 2 * days, list::add);
        assertIterableEquals(List.of(0, days, 2 * days), list);

        list = new ArrayList();
        store.each(days, 3 * days, list::add);
        assertIterableEquals(List.of(days, 2 * days, 3 * days), list);

        list = new ArrayList();
        store.each(3 * days, 4 * days, list::add);
        assertIterableEquals(List.of(3 * days, 4 * days), list);
    }

    @Test
    void eachByItem() {
        TimeseriesStore<Integer> store = TimeseriesStore.create(int.class, Integer::longValue, Span.Second5);
        store.store(0, 5, 10, 15, 20, 25, 30);

        List<Integer> items = store.each(store.at(0), store.at(15)).toList();
        assert items.size() == 4;
        assert items.get(0) == 0;
        assert items.get(1) == 5;
        assert items.get(2) == 10;
        assert items.get(3) == 15;
    }

    @Test
    void eachLatest() {
        // padding right
        TimeseriesStore<Integer> store = TimeseriesStore.create(int.class, Integer::longValue, Span.Second5);
        store.store(0, 5, 10, 15, 20, 25, 30);

        List<Integer> list = store.eachLatest().toList();
        assert list.size() == 7;
        assert list.get(0) == 30;
        assert list.get(1) == 25;
        assert list.get(2) == 20;
    }

    @Test
    void eachLatestOverDays() {
        TimeseriesStore<Integer> store = TimeseriesStore.create(int.class, Integer::longValue, Span.Day1);
        store.store(0, days, 2 * days, 3 * days, 4 * days);

        List<Integer> list = store.eachLatest().toList();
        assert list.size() == 5;
        assert list.get(0) == 4 * days;
        assert list.get(1) == 3 * days;
        assert list.get(2) == 2 * days;
        assert list.get(3) == 1 * days;
    }

    @Test
    void calculateStartTimeAndRemainderEpochSeconds() {
        TimeseriesStore<Integer> store = TimeseriesStore.create(int.class, Integer::longValue, Span.Minute1);

        // 2019-12-12 02:16:30
        Assertions.assertArrayEquals(new long[] {1576108800, 136}, store.index(1576116990));
        // 2019-12-13 00:00:00
        Assertions.assertArrayEquals(new long[] {1576195200, 0}, store.index(1576195200));
        // 2019-12-13 00:00:59
        Assertions.assertArrayEquals(new long[] {1576195200, 0}, store.index(1576195259));
        // 2019-12-13 00:01:00
        Assertions.assertArrayEquals(new long[] {1576195200, 1}, store.index(1576195260));
    }

    @Test
    void before() {
        TimeseriesStore<Integer> store = TimeseriesStore.create(int.class, Integer::longValue, Span.Second5);
        store.store(0, 5, 10, 15);
        assert store.before(0) == null;
        assert store.before(3) == null;
        assert store.before(5) == 0;
        assert store.before(7) == 0;
        assert store.before(10) == 5;
        assert store.before(14) == 5;
        assert store.before(15) == 10;
        assert store.before(20) == 15;
        assert store.before(25) == null;
    }

    @Test
    void beforeOverTime() {
        TimeseriesStore<Integer> store = TimeseriesStore.create(int.class, Integer::longValue, Span.Day1);
        store.store(0, days, 2 * days, 3 * days, 4 * days);
        assert store.before(0) == null;
        assert store.before(days - 1) == null;
        assert store.before(days) == 0;
        assert store.before(days + 1) == 0;
        assert store.before(2 * days - 1) == 0;
        assert store.before(2 * days) == days;
        assert store.before(2 * days + 1) == days;
    }

    @Test
    void befores() {
        TimeseriesStore<Integer> store = TimeseriesStore.create(int.class, Integer::longValue, Span.Second5);
        store.store(0, 5, 10, 15);
        assert store.beforeUntil(0, 1).isEmpty();
        assert store.beforeUntil(3, 2).isEmpty();
        assert store.beforeUntil(5, 1).equals(List.of(0));
        assert store.beforeUntil(7, 2).equals(List.of(0));
        assert store.beforeUntil(10, 1).equals(List.of(5));
        assert store.beforeUntil(14, 2).equals(List.of(5, 0));
        assert store.beforeUntil(15, 3).equals(List.of(10, 5, 0));
        assert store.beforeUntil(20, 2).equals(List.of(15, 10));
        assert store.beforeUntil(25, 5).equals(List.of(15, 10, 5, 0));
    }

    @Test
    void beforesOverTime() {
        TimeseriesStore<Integer> store = TimeseriesStore.create(int.class, Integer::longValue, Span.Day1);
        store.store(0, days, 2 * days, 3 * days, 4 * days);
        assert store.beforeUntil(0, 1).isEmpty();
        assert store.beforeUntil(days - 1, 2).isEmpty();
        assert store.beforeUntil(days, 1).equals(List.of(0));
        assert store.beforeUntil(days + 1, 2).equals(List.of(0));
        assert store.beforeUntil(2 * days - 1, 3).equals(List.of(0));
        assert store.beforeUntil(2 * days, 1).equals(List.of(days));
        assert store.beforeUntil(2 * days + 1, 3).equals(List.of(days, 0));
        assert store.beforeUntil(4 * days, 3).equals(List.of(3 * days, 2 * days, days));
    }

    @Test
    void beforeWith() {
        TimeseriesStore<Integer> store = TimeseriesStore.create(int.class, Integer::longValue, Span.Second5);
        store.store(0, 5, 10, 15);
        assert store.beforeUntilWith(0, 1).equals(List.of(0));
        assert store.beforeUntilWith(3, 2).equals(List.of(0));
        assert store.beforeUntilWith(5, 1).equals(List.of(5));
        assert store.beforeUntilWith(7, 2).equals(List.of(5, 0));
        assert store.beforeUntilWith(10, 1).equals(List.of(10));
        assert store.beforeUntilWith(14, 2).equals(List.of(10, 5));
        assert store.beforeUntilWith(15, 5).equals(List.of(15, 10, 5, 0));
        assert store.beforeUntilWith(20, 2).equals(List.of(15, 10));
        assert store.beforeUntilWith(25, 5).equals(List.of(15, 10, 5, 0));
    }

    @Test
    void beforeWithOverTime() {
        TimeseriesStore<Integer> store = TimeseriesStore.create(int.class, Integer::longValue, Span.Day1);
        store.store(0, days, 2 * days, 3 * days, 4 * days);
        assert store.beforeUntilWith(0, 1).equals(List.of(0));
        assert store.beforeUntilWith(days - 1, 2).equals(List.of(0));
        assert store.beforeUntilWith(days, 1).equals(List.of(days));
        assert store.beforeUntilWith(days + 1, 2).equals(List.of(days, 0));
        assert store.beforeUntilWith(2 * days - 1, 3).equals(List.of(days, 0));
        assert store.beforeUntilWith(2 * days, 1).equals(List.of(2 * days));
        assert store.beforeUntilWith(2 * days + 1, 5).equals(List.of(2 * days, days, 0));
        assert store.beforeUntilWith(4 * days, 3).equals(List.of(4 * days, 3 * days, 2 * days));
    }

    @RegisterExtension
    CleanRoom room = new CleanRoom();

    @Test
    void diskCache() {
        TimeseriesStore<Long> store = TimeseriesStore.create(long.class, v -> v, Span.Second5).enableDiskStore(room.locateRadom());

        long base = Span.Second5.segmentSeconds;

        for (int i = 1; i <= 20; i++) {
            store.store(base * i);
        }

        assert store.existOnHeap(base * 18);
        assert store.existOnHeap(base * 19);
        assert store.existOnHeap(base * 20);
        assert store.existOnHeap(base * 2) == false;
        assert store.existOnHeap(base * 8) == false;
        assert store.existOnHeap(base * 17) == false;

        assert store.at(base * 2) == base * 2;
        assert store.existOnHeap(base * 18) == false;
        assert store.existOnHeap(base * 19);
        assert store.existOnHeap(base * 20);
        assert store.existOnHeap(base * 2);
    }

    @Test
    void dataSupply() {
        TimeseriesStore<Long> store = TimeseriesStore.create(long.class, v -> v, Span.Second5).enableDataSupplier(time -> {
            return I.signal(time);
        });

        long base = Span.Second5.segmentSeconds;
        assert store.existOnHeap(base * 1) == false;
        assert store.existOnHeap(base * 2) == false;
        assert store.existOnHeap(base * 3) == false;
        assert store.existOnHeap(base * 4) == false;

        // automatic creation
        assert store.at(base * 1) == base * 1;
        assert store.at(base * 2) == base * 2;
        assert store.at(base * 3) == base * 3;
        assert store.at(base * 4) == base * 4;
    }

    @Test
    void persist() {
        Directory dir = Locator.directory(room.locateRadom());
        TimeseriesStore<Long> store = TimeseriesStore.create(long.class, v -> v, Span.Second5).enableDiskStore(dir);
        File cache = dir.file("Second5/1970010100.cache");
        assert cache.isAbsent();

        store.store(0L);
        store.persist();
        assert cache.isPresent();
    }

    @Test
    void persistOnlyModified() {
        Directory dir = Locator.directory(room.locateRadom());
        TimeseriesStore<Long> store = TimeseriesStore.create(long.class, v -> v, Span.Second5).enableDiskStore(dir);
        File cache = dir.file("Second5/1970010100.cache");

        store.store(0L);
        store.persist();
        assert cache.isPresent();

        // no modification
        long modified = cache.lastModified();
        store.persist();
        assert modified == cache.lastModified();

        // modified
        store.store(1L);
        store.persist();
        assert modified != cache.lastModified();
    }

    @Test
    void readDataFromDiskCache() {
        Directory dir = Locator.directory(room.locateRadom());
        TimeseriesStore<Long> store = TimeseriesStore.create(long.class, v -> v, Span.Second5).enableDiskStore(dir);

        store.store(0L);
        store.persist();
        store.clear();
        assert store.existOnHeap(0L) == false;
        assert store.at(0) == 0L;
        assert store.at(5) == null;
    }
}