/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.ticker;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import cointoss.Currency;
import cointoss.MarketType;
import cointoss.ticker.data.OpenInterest;
import cointoss.ticker.data.TimeseriesData;
import cointoss.util.Chrono;
import kiss.I;
import psychopath.Directory;
import psychopath.File;
import psychopath.Locator;

class TimeseriesStoreTest {
    private static final int days = 60 * 60 * 24;

    private Value day(int size) {
        return value(size * days);
    }

    private Value value(int value) {
        return new Value(value);
    }

    private List<Value> values(int... values) {
        return IntStream.of(values).mapToObj(this::value).collect(Collectors.toList());
    }

    public static class Value implements TimeseriesData {

        public int value;

        private Value(int value) {
            this.value = value;
        }

        @Override
        public ZonedDateTime date() {
            return Chrono.utcBySeconds(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Value) {
                return ((Value) obj).value == value;
            } else {
                return false;
            }
        }
    }

    @Test
    void isEmpty() {
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Minute1);
        assert store.isEmpty();

        store.store(value(1));
        assert store.isEmpty() == false;
    }

    @Test
    void isNotEmpty() {
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Minute1);
        assert store.isNotEmpty() == false;

        store.store(value(1));
        assert store.isNotEmpty();
    }

    @Test
    void add() {
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Second5);
        store.store(value(0));
        assert store.at(0).value == 0;
        assert store.at(5) == null;
        assert store.at(10) == null;

        // update
        store.store(value(2));
        assert store.at(0).value == 2;
        assert store.at(5) == null;
        assert store.at(10) == null;

        // add next stamp
        store.store(value(5));
        assert store.at(0).value == 2;
        assert store.at(5).value == 5;
        assert store.at(10) == null;

        // add next stamp
        store.store(value(10));
        assert store.at(0).value == 2;
        assert store.at(5).value == 5;
        assert store.at(10).value == 10;

        // update
        store.store(value(13));
        assert store.at(0).value == 2;
        assert store.at(5).value == 5;
        assert store.at(10).value == 13;
    }

    @Test
    void getByTime() {
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Second5);
        store.store(value(0), value(5), value(10));
        assert store.at(0).value == 0;
        assert store.at(3).value == 0;
        assert store.at(5).value == 5;
        assert store.at(7).value == 5;
        assert store.at(10).value == 10;
        assert store.at(14).value == 10;
    }

    @Test
    void getByTimeOverTime() {
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Day1);
        store.store(value(0), value(days), value(2 * days), value(3 * days), value(4 * days));
        assert store.at(0).value == 0;
        assert store.at(days - 1).value == 0;
        assert store.at(days).value == days;
        assert store.at(days + 1).value == days;
        assert store.at(2 * days - 1).value == days;
        assert store.at(2 * days).value == 2 * days;
        assert store.at(2 * days + 1).value == 2 * days;
    }

    @Test
    void first() {
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Second5);
        store.store(value(25));
        assert store.first().value == 25;

        store.store(value(30));
        assert store.first().value == 25;

        store.store(value(15));
        assert store.first().value == 15;

        store.store(value(17));
        assert store.first().value == 17;

        store.store(value(13));
        assert store.first().value == 13;

        store.store(value(10));
        assert store.first().value == 10;
    }

    @Test
    void firstOverDays() {
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Day1);
        store.store(day(0), day(1), day(2), day(3), day(4));
        assert store.first().value == 0;
    }

    @Test
    void last() {
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Second5);
        store.store(value(5));
        assert store.last().value == 5;

        store.store(value(10));
        assert store.last().value == 10;

        store.store(value(12));
        assert store.last().value == 12;

        store.store(value(20));
        assert store.last().value == 20;

        store.store(value(16));
        assert store.last().value == 20;

        store.store(value(30));
        assert store.last().value == 30;
    }

    @Test
    void lastOverDays() {
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Day1);
        store.store(day(0), day(1), day(2), day(3), day(4));
        assert store.last().value == 4 * days;
    }

    @Test
    void size() {
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Second5);
        assert store.size() == 0;

        store.store(value(5));
        assert store.size() == 1;

        store.store(value(10));
        assert store.size() == 2;

        // update will not modify size
        store.store(value(5));
        store.store(value(6));
        store.store(value(7));
        assert store.size() == 2;
    }

    @Test
    void sizeOverDays() {
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Day1);
        store.store(day(0), day(1), day(2), day(3), day(4));
        assert store.size() == 5;
    }

    @Test
    void each() {
        // padding right
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Second5);
        store.store(value(0), value(5), value(10), value(15), value(20), value(25), value(30));

        List<Value> list = new ArrayList();
        store.each(list::add);
        assertIterableEquals(values(0, 5, 10, 15, 20, 25, 30), list);

        // padding both sides
        store = TimeseriesStore.create(Value.class, Span.Second5);
        store.store(value(15), value(20), value(25), value(30));

        list = new ArrayList();
        store.each(list::add);
        assertIterableEquals(values(15, 20, 25, 30), list);

        // padding left side
        store = TimeseriesStore.create(Value.class, Span.Hour4);
        store.store(value(3600 * 12), value(3600 * 16), value(3600 * 20));

        list = new ArrayList();
        store.each(list::add);
        assertIterableEquals(values(3600 * 12, 3600 * 16, 3600 * 20), list);
    }

    @Test
    void eachOverDays() {
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Day1);
        store.store(day(0), day(1), day(2), day(3), day(4));

        List<Value> list = new ArrayList();
        store.each(list::add);
        assertIterableEquals(values(0, days, 2 * days, 3 * days, 4 * days), list);
    }

    @Test
    void eachByTime() {
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Second5);
        store.store(I.signal(5, 10, 15, 20, 25, 30, 35).map(this::value));

        List<Value> list = new ArrayList();
        store.each(10, 35, list::add);
        assertIterableEquals(values(10, 15, 20, 25, 30, 35), list);

        list = new ArrayList();
        store.each(10, 34, list::add);
        assertIterableEquals(values(10, 15, 20, 25, 30), list);

        list = new ArrayList();
        store.each(10, 36, list::add);
        assertIterableEquals(values(10, 15, 20, 25, 30, 35), list);

        list = new ArrayList();
        store.each(0, 15, list::add);
        assertIterableEquals(values(5, 10, 15), list);

        list = new ArrayList();
        store.each(100, 150, list::add);
        assertIterableEquals(values(), list);
    }

    @Test
    void eachByTimeOverDays() {
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Day1);
        store.store(day(0), day(1), day(2), day(3), day(4));

        List<Value> list = new ArrayList();
        store.each(0, 2 * days, list::add);
        assertIterableEquals(values(0, days, 2 * days), list);

        list = new ArrayList();
        store.each(days, 3 * days, list::add);
        assertIterableEquals(values(days, 2 * days, 3 * days), list);

        list = new ArrayList();
        store.each(3 * days, 4 * days, list::add);
        assertIterableEquals(values(3 * days, 4 * days), list);
    }

    @Test
    void eachByItem() {
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Second5);
        store.store(values(0, 5, 10, 15, 20, 25, 30));

        List<Value> items = store.each(store.at(0), store.at(15)).toList();
        assert items.size() == 4;
        assert items.get(0).value == 0;
        assert items.get(1).value == 5;
        assert items.get(2).value == 10;
        assert items.get(3).value == 15;
    }

    @Test
    void eachLatest() {
        // padding right
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Second5);
        store.store(values(0, 5, 10, 15, 20, 25, 30));

        List<Value> list = store.eachLatest().toList();
        assert list.size() == 7;
        assert list.get(0).value == 30;
        assert list.get(1).value == 25;
        assert list.get(2).value == 20;
    }

    @Test
    void eachLatestOverDays() {
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Day1);
        store.store(day(0), day(1), day(2), day(3), day(4));

        List<Value> list = store.eachLatest().toList();
        assert list.size() == 5;
        assert list.get(0).value == 4 * days;
        assert list.get(1).value == 3 * days;
        assert list.get(2).value == 2 * days;
        assert list.get(3).value == 1 * days;
    }

    @Test
    void calculateStartTimeAndRemainderEpochSeconds() {
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Minute1);

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
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Second5);
        store.store(values(0, 5, 10, 15));
        assert store.before(0) == null;
        assert store.before(3) == null;
        assert store.before(5).value == 0;
        assert store.before(7).value == 0;
        assert store.before(10).value == 5;
        assert store.before(14).value == 5;
        assert store.before(15).value == 10;
        assert store.before(20).value == 15;
        assert store.before(25) == null;
    }

    @Test
    void beforeOverTime() {
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Day1);
        store.store(day(0), day(1), day(2), day(3), day(4));
        assert store.before(0) == null;
        assert store.before(days - 1) == null;
        assert store.before(days).value == 0;
        assert store.before(days + 1).value == 0;
        assert store.before(2 * days - 1).value == 0;
        assert store.before(2 * days).value == days;
        assert store.before(2 * days + 1).value == days;
    }

    @Test
    void befores() {
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Second5);
        store.store(values(0, 5, 10, 15));
        assert store.beforeUntil(0, 1).isEmpty();
        assert store.beforeUntil(3, 2).isEmpty();
        assert store.beforeUntil(5, 1).equals(values(0));
        assert store.beforeUntil(7, 2).equals(values(0));
        assert store.beforeUntil(10, 1).equals(values(5));
        assert store.beforeUntil(14, 2).equals(values(5, 0));
        assert store.beforeUntil(15, 3).equals(values(10, 5, 0));
        assert store.beforeUntil(20, 2).equals(values(15, 10));
        assert store.beforeUntil(25, 5).equals(values(15, 10, 5, 0));
    }

    @Test
    void beforesOverTime() {
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Day1);
        store.store(day(0), day(1), day(2), day(3), day(4));
        assert store.beforeUntil(0, 1).isEmpty();
        assert store.beforeUntil(days - 1, 2).isEmpty();
        assert store.beforeUntil(days, 1).equals(values(0));
        assert store.beforeUntil(days + 1, 2).equals(values(0));
        assert store.beforeUntil(2 * days - 1, 3).equals(values(0));
        assert store.beforeUntil(2 * days, 1).equals(values(days));
        assert store.beforeUntil(2 * days + 1, 3).equals(values(days, 0));
        assert store.beforeUntil(4 * days, 3).equals(values(3 * days, 2 * days, days));
    }

    @Test
    void beforeWith() {
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Second5);
        store.store(values(0, 5, 10, 15));
        assert store.beforeUntilWith(0, 1).equals(values(0));
        assert store.beforeUntilWith(3, 2).equals(values(0));
        assert store.beforeUntilWith(5, 1).equals(values(5));
        assert store.beforeUntilWith(7, 2).equals(values(5, 0));
        assert store.beforeUntilWith(10, 1).equals(values(10));
        assert store.beforeUntilWith(14, 2).equals(values(10, 5));
        assert store.beforeUntilWith(15, 5).equals(values(15, 10, 5, 0));
        assert store.beforeUntilWith(20, 2).equals(values(15, 10));
        assert store.beforeUntilWith(25, 5).equals(values(15, 10, 5, 0));
    }

    @Test
    void beforeWithOverTime() {
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Day1);
        store.store(day(0), day(1), day(2), day(3), day(4));
        assert store.beforeUntilWith(0, 1).equals(values(0));
        assert store.beforeUntilWith(days - 1, 2).equals(values(0));
        assert store.beforeUntilWith(days, 1).equals(values(days));
        assert store.beforeUntilWith(days + 1, 2).equals(values(days, 0));
        assert store.beforeUntilWith(2 * days - 1, 3).equals(values(days, 0));
        assert store.beforeUntilWith(2 * days, 1).equals(values(2 * days));
        assert store.beforeUntilWith(2 * days + 1, 5).equals(values(2 * days, days, 0));
        assert store.beforeUntilWith(4 * days, 3).equals(values(4 * days, 3 * days, 2 * days));
    }

    @RegisterExtension
    CleanRoom room = new CleanRoom();

    @Test
    void diskCache() {
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Second5).enableDiskStore(room.locateRadom());

        int base = (int) Span.Second5.segmentSeconds;

        for (int i = 1; i <= 20; i++) {
            store.store(value(base * i));
        }

        assert store.existOnHeap(value(base * 18));
        assert store.existOnHeap(value(base * 19));
        assert store.existOnHeap(value(base * 20));
        assert store.existOnHeap(value(base * 2)) == false;
        assert store.existOnHeap(value(base * 8)) == false;
        assert store.existOnHeap(value(base * 17)) == false;

        assert store.at(base * 2).value == base * 2;
        assert store.existOnHeap(value(base * 18)) == false;
        assert store.existOnHeap(value(base * 19));
        assert store.existOnHeap(value(base * 20));
        assert store.existOnHeap(value(base * 2));
    }

    @Test
    void dataSupply() {
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Second5).enableActiveDataSupplier(time -> {
            return I.signal(value((int) time));
        });

        int base = (int) Span.Second5.segmentSeconds;
        assert store.existOnHeap(value(base * 1)) == false;
        assert store.existOnHeap(value(base * 2)) == false;
        assert store.existOnHeap(value(base * 3)) == false;
        assert store.existOnHeap(value(base * 4)) == false;

        // automatic creation
        assert store.at(base * 1).value == base * 1;
        assert store.at(base * 2).value == base * 2;
        assert store.at(base * 3).value == base * 3;
        assert store.at(base * 4).value == base * 4;
    }

    @Test
    void persist() {
        Directory dir = Locator.directory(room.locateRadom());
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Second5).enableDiskStore(dir);
        File cache = dir.file("1970-01-01 00.db");

        store.store(value(0));
        assert cache.isAbsent();
        store.persist();
        assert cache.isPresent();
    }

    @Test
    void persistAutoSync() {
        Directory dir = Locator.directory(room.locateRadom());
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Second5).enableDiskStore(dir, true);
        File cache = dir.file("1970-01-01 00.db");
        assert cache.isAbsent();

        store.store(value(0));
        assert cache.isPresent();
    }

    @Test
    void persistOnlyModified() {
        Directory dir = Locator.directory(room.locateRadom());
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Second5).enableDiskStore(dir);
        File cache = dir.file("1970-01-01 00.db");

        store.store(value(0));
        store.persist();
        assert cache.isPresent();

        // no modification
        long modified = cache.lastModifiedMilli();
        store.persist();
        assert modified == cache.lastModifiedMilli();

        // modified
        store.store(value(1));
        store.persist();
        assert modified != cache.lastModifiedMilli();
    }

    @Test
    void readDataFromDiskCache() {
        Directory dir = Locator.directory(room.locateRadom());
        TimeseriesStore<Value> store = TimeseriesStore.create(Value.class, Span.Second5).enableDiskStore(dir, true);

        store.store(value(0));
        store.clear();
        assert store.existOnHeap(value(0)) == false;
        assert store.at(0).value == 0;
        assert store.at(5) == null;
    }

    @Test
    void supportPrimitiveTypes() {
        Primitive primitive = new Primitive();
        primitive.intValue = 1;
        primitive.longValue = -2;
        primitive.floatValue = 3.3f;
        primitive.doubleValue = -0.4;
        primitive.booleanValue = true;
        primitive.charValue = 'c';
        primitive.byteValue = 2;
        primitive.shortValue = 3;

        TimeseriesStore<Primitive> store = TimeseriesStore.create(Primitive.class, Span.Second5).enableDiskStore(room.locateRadom(), true);
        store.store(primitive);
        store.clear();

        Primitive restored = store.at(primitive.intValue);
        assert restored != null;
        assert restored.intValue == primitive.intValue;
        assert restored.longValue == primitive.longValue;
        assert restored.floatValue == primitive.floatValue;
        assert restored.doubleValue == primitive.doubleValue;
        assert restored.booleanValue == primitive.booleanValue;
        assert restored.charValue == primitive.charValue;
        assert restored.byteValue == primitive.byteValue;
        assert restored.shortValue == primitive.shortValue;
    }

    private static class Primitive implements TimeseriesData {
        public int intValue;

        public long longValue;

        public float floatValue;

        public double doubleValue;

        public boolean booleanValue;

        public char charValue;

        public short shortValue;

        public byte byteValue;

        @Override
        public ZonedDateTime date() {
            return Chrono.utcBySeconds(intValue);
        }
    }

    @Test
    void supportEnum() {
        Enums e = new Enums();
        e.type = MarketType.DERIVATIVE;
        e.currency = Currency.BTC;

        TimeseriesStore<Enums> store = TimeseriesStore.create(Enums.class, Span.Second5).enableDiskStore(room.locateRadom(), true);
        store.store(e);
        store.clear();

        Enums restored = store.at(e.epochSeconds());
        assert restored != null;
        assert restored.type == e.type;
        assert restored.currency == e.currency;
    }

    private static class Enums implements TimeseriesData {
        public MarketType type;

        public Currency currency;

        @Override
        public ZonedDateTime date() {
            return Chrono.utc(2020, 1, 1);
        }
    }

    @Test
    void supportZonedDateTime() {
        Directory dir = Locator.directory(room.locateRadom());
        TimeseriesStore<OpenInterest> store = TimeseriesStore.create(OpenInterest.class, Span.Second5).enableDiskStore(dir, true);
        OpenInterest oi = OpenInterest.with.date(Chrono.utc(2020, 1, 1)).size(10);
        store.store(oi);
        store.clear();

        assert store.at(oi.epochSeconds()).equals(oi);
    }

    @Test
    void storeSparsedDisk() {
        Directory dir = Locator.directory(room.locateRadom());
        TimeseriesStore<OpenInterest> store = TimeseriesStore.create(OpenInterest.class, Span.Day1).enableDiskStore(dir, true);
        OpenInterest oi1 = OpenInterest.with.date(Chrono.utc(2020, 1, 1)).size(10);
        OpenInterest oi2 = OpenInterest.with.date(Chrono.utc(2020, 2, 1)).size(20);
        OpenInterest oi3 = OpenInterest.with.date(Chrono.utc(2020, 3, 1)).size(30);
        store.store(oi1, oi2, oi3);
        store.clear();

        assert store.at(oi1.epochSeconds()).equals(oi1);
        assert store.at(oi2.epochSeconds()).equals(oi2);
        assert store.at(oi3.epochSeconds()).equals(oi3);
    }
}