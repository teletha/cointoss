/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util.feather;

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
import cointoss.ticker.Span;
import cointoss.ticker.data.OpenInterest;
import cointoss.util.Chrono;
import kiss.I;

class FeatherStoreTest {
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

    static class Value implements TemporalData {

        public int value;

        Value(int value) {
            this.value = value;
        }

        Value(long value) {
            this.value = (int) value;
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
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1);
        assert store.isEmpty();

        store.store(value(1));
        assert store.isEmpty() == false;
    }

    @Test
    void isNotEmpty() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1);
        assert store.isNotEmpty() == false;

        store.store(value(1));
        assert store.isNotEmpty();
    }

    @Test
    void add() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1);
        store.store(value(0));
        assert store.at(0).value == 0;
        assert store.at(60) == null;
        assert store.at(120) == null;

        // update
        store.store(value(2));
        assert store.at(0).value == 2;
        assert store.at(60) == null;
        assert store.at(120) == null;

        // add next stamp
        store.store(value(60));
        assert store.at(0).value == 2;
        assert store.at(60).value == 60;
        assert store.at(120) == null;

        // add next stamp
        store.store(value(120));
        assert store.at(0).value == 2;
        assert store.at(60).value == 60;
        assert store.at(120).value == 120;

        // update
        store.store(value(156));
        assert store.at(0).value == 2;
        assert store.at(60).value == 60;
        assert store.at(120).value == 156;
    }

    @Test
    void getByTime() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1);
        store.store(value(0), value(60), value(120));
        assert store.at(0).value == 0;
        assert store.at(30).value == 0;
        assert store.at(60).value == 60;
        assert store.at(70).value == 60;
        assert store.at(120).value == 120;
        assert store.at(140).value == 120;
    }

    @Test
    void getByTimeOverTime() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Day1);
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
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1);
        store.store(value(300));
        assert store.first().value == 300;

        store.store(value(360));
        assert store.first().value == 300;

        store.store(value(180));
        assert store.first().value == 180;

        store.store(value(204));
        assert store.first().value == 204;

        store.store(value(156));
        assert store.first().value == 156;

        store.store(value(120));
        assert store.first().value == 120;
    }

    @Test
    void firstOverDays() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Day1);
        store.store(day(0), day(1), day(2), day(3), day(4));
        assert store.first().value == 0;
    }

    @Test
    void last() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1);
        store.store(value(60));
        assert store.last().value == 60;

        store.store(value(120));
        assert store.last().value == 120;

        store.store(value(144));
        assert store.last().value == 144;

        store.store(value(240));
        assert store.last().value == 240;

        store.store(value(192));
        assert store.last().value == 240;

        store.store(value(360));
        assert store.last().value == 360;
    }

    @Test
    void lastOverDays() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Day1);
        store.store(day(0), day(1), day(2), day(3), day(4));
        assert store.last().value == 4 * days;
    }

    @Test
    void size() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1);
        assert store.size() == 0;

        store.store(value(60));
        assert store.size() == 1;

        store.store(value(120));
        assert store.size() == 2;

        // update will not modify size
        store.store(value(60));
        store.store(value(72));
        store.store(value(84));
        assert store.size() == 2;
    }

    @Test
    void sizeOverDays() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Day1);
        store.store(day(0), day(1), day(2), day(3), day(4));
        assert store.size() == 5;
    }

    @Test
    void each() {
        // padding right
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1);
        store.store(value(0), value(60), value(120), value(180), value(240), value(300), value(360));

        List<Value> list = new ArrayList();
        store.each(list::add);
        assertIterableEquals(values(0, 60, 120, 180, 240, 300, 360), list);

        // padding both sides
        store = FeatherStore.create(Value.class, Span.Minute1);
        store.store(value(180), value(240), value(300), value(360));

        list = new ArrayList();
        store.each(list::add);
        assertIterableEquals(values(180, 240, 300, 360), list);

        // padding left side
        store = FeatherStore.create(Value.class, Span.Hour4);
        store.store(value(3600 * 12), value(3600 * 16), value(3600 * 20));

        list = new ArrayList();
        store.each(list::add);
        assertIterableEquals(values(3600 * 12, 3600 * 16, 3600 * 20), list);
    }

    @Test
    void eachOverDays() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Day1);
        store.store(day(0), day(1), day(2), day(3), day(4));

        List<Value> list = new ArrayList();
        store.each(list::add);
        assertIterableEquals(values(0, days, 2 * days, 3 * days, 4 * days), list);
    }

    @Test
    void eachByTime() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1);
        store.store(I.signal(60, 120, 180, 240, 300, 360, 420).map(this::value));

        List<Value> list = new ArrayList();
        store.each(120, 420, list::add);
        assertIterableEquals(values(120, 180, 240, 300, 360, 420), list);

        list = new ArrayList();
        store.each(120, 419, list::add);
        assertIterableEquals(values(120, 180, 240, 300, 360), list);

        list = new ArrayList();
        store.each(120, 361, list::add);
        assertIterableEquals(values(120, 180, 240, 300, 360), list);

        list = new ArrayList();
        store.each(0, 180, list::add);
        assertIterableEquals(values(60, 120, 180), list);

        list = new ArrayList();
        store.each(1200, 1800, list::add);
        assertIterableEquals(values(), list);
    }

    @Test
    void eachByTimeOverDays() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Day1);
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
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1);
        store.store(values(0, 60, 120, 180, 240, 300, 360));

        List<Value> items = store.each(store.at(0), store.at(180)).toList();
        assert items.size() == 4;
        assert items.get(0).value == 0;
        assert items.get(1).value == 60;
        assert items.get(2).value == 120;
        assert items.get(3).value == 180;
    }

    @Test
    void eachLatest() {
        // padding right
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1);
        store.store(values(0, 60, 120, 180, 240, 300, 360));

        List<Value> list = store.eachLatest().toList();
        assert list.size() == 7;
        assert list.get(0).value == 360;
        assert list.get(1).value == 300;
        assert list.get(2).value == 240;
    }

    @Test
    void eachLatestOverDays() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Day1);
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
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1);

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
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1);
        store.store(values(0, 60, 120, 180));
        assert store.before(0) == null;
        assert store.before(36) == null;
        assert store.before(60).value == 0;
        assert store.before(72).value == 0;
        assert store.before(120).value == 60;
        assert store.before(144).value == 60;
        assert store.before(180).value == 120;
        assert store.before(240).value == 180;
        assert store.before(300) == null;
    }

    @Test
    void beforeOverTime() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Day1);
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
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1);
        store.store(values(0, 60, 120, 180));
        assert store.beforeUntil(0, 1).isEmpty();
        assert store.beforeUntil(36, 2).isEmpty();
        assert store.beforeUntil(60, 1).equals(values(0));
        assert store.beforeUntil(72, 2).equals(values(0));
        assert store.beforeUntil(120, 1).equals(values(60));
        assert store.beforeUntil(144, 2).equals(values(60, 0));
        assert store.beforeUntil(180, 3).equals(values(120, 60, 0));
        assert store.beforeUntil(240, 2).equals(values(180, 120));
        assert store.beforeUntil(300, 5).equals(values(180, 120, 60, 0));
    }

    @Test
    void beforesOverTime() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Day1);
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
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1);
        store.store(values(0, 60, 120, 180));
        assert store.beforeUntilWith(0, 1).equals(values(0));
        assert store.beforeUntilWith(36, 2).equals(values(0));
        assert store.beforeUntilWith(60, 1).equals(values(60));
        assert store.beforeUntilWith(72, 2).equals(values(60, 0));
        assert store.beforeUntilWith(120, 1).equals(values(120));
        assert store.beforeUntilWith(144, 2).equals(values(120, 60));
        assert store.beforeUntilWith(180, 5).equals(values(180, 120, 60, 0));
        assert store.beforeUntilWith(240, 2).equals(values(180, 120));
        assert store.beforeUntilWith(300, 5).equals(values(180, 120, 60, 0));
    }

    @Test
    void beforeWithOverTime() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Day1);
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
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1).enableDiskStore(room.locateRadom());

        int base = (int) Span.Minute1.segmentSeconds;

        for (int i = 1; i <= 20; i++) {
            store.store(value(base * i));
        }

        assert store.existOnHeap(value(base * 14));
        assert store.existOnHeap(value(base * 15));
        assert store.existOnHeap(value(base * 16));
        assert store.existOnHeap(value(base * 17));
        assert store.existOnHeap(value(base * 18));
        assert store.existOnHeap(value(base * 19));
        assert store.existOnHeap(value(base * 20));
        assert store.existOnHeap(value(base * 2)) == false;
        assert store.existOnHeap(value(base * 8)) == false;
        assert store.existOnHeap(value(base * 13)) == false;

        assert store.at(base * 2).value == base * 2;
        assert store.existOnHeap(value(base * 14)) == false;
        assert store.existOnHeap(value(base * 15));
        assert store.existOnHeap(value(base * 16));
        assert store.existOnHeap(value(base * 17));
        assert store.existOnHeap(value(base * 18));
        assert store.existOnHeap(value(base * 19));
        assert store.existOnHeap(value(base * 20));
        assert store.existOnHeap(value(base * 2));
    }

    @Test
    void dataSupply() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1).enableActiveDataSupplier(time -> {
            return I.signal(value((int) time));
        });

        int base = (int) Span.Minute1.segmentSeconds;
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
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1).enableDiskStore(room.locateRadom());

        store.store(value(0));
        assert store.existOnDisk(value(0)) == false;
        store.commit();
        assert store.existOnDisk(value(0)) == true;
    }

    @Test
    void readDataFromDiskCache() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1).enableDiskStore(room.locateRadom());

        store.store(value(0));
        store.commit();
        store.clear();
        assert store.existOnHeap(value(0)) == false;
        assert store.at(0).value == 0;
        assert store.at(60) == null;
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

        FeatherStore<Primitive> store = FeatherStore.create(Primitive.class, Span.Minute1).enableDiskStore(room.locateRadom());
        store.store(primitive);
        store.commit();
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

    private static class Primitive implements TemporalData {
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

        FeatherStore<Enums> store = FeatherStore.create(Enums.class, Span.Minute1).enableDiskStore(room.locateRadom());
        store.store(e);
        store.commit();
        store.clear();

        Enums restored = store.at(e.epochSeconds());
        assert restored != null;
        assert restored.type == e.type;
        assert restored.currency == e.currency;
    }

    private static class Enums implements TemporalData {
        public MarketType type;

        public Currency currency;

        @Override
        public ZonedDateTime date() {
            return Chrono.utc(2020, 1, 1);
        }
    }

    @Test
    void supportZonedDateTime() {
        FeatherStore<OpenInterest> store = FeatherStore.create(OpenInterest.class, Span.Minute1).enableDiskStore(room.locateRadom());
        OpenInterest oi = OpenInterest.with.date(Chrono.utc(2020, 1, 1)).size(10);
        store.store(oi);
        store.commit();
        // store.clear();

        // assert store.at(oi.epochSeconds()).equals(oi);
    }

    @Test
    void storeSparsedDisk() {
        FeatherStore<OpenInterest> store = FeatherStore.create(OpenInterest.class, Span.Day1).enableDiskStore(room.locateRadom());
        OpenInterest oi1 = OpenInterest.with.date(Chrono.utc(2020, 1, 1)).size(10);
        OpenInterest oi2 = OpenInterest.with.date(Chrono.utc(2020, 2, 1)).size(20);
        OpenInterest oi3 = OpenInterest.with.date(Chrono.utc(2020, 3, 1)).size(30);
        store.store(oi1, oi2, oi3);
        store.commit();
        store.clear();

        assert store.at(oi1.epochSeconds()).equals(oi1);
        assert store.at(oi2.epochSeconds()).equals(oi2);
        assert store.at(oi3.epochSeconds()).equals(oi3);
    }
}