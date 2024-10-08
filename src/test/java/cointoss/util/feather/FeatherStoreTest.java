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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import cointoss.Currency;
import cointoss.MarketType;
import cointoss.ticker.Span;
import cointoss.ticker.data.OpenInterest;
import cointoss.util.Chrono;
import typewriter.api.model.IdentifiableModel;

class FeatherStoreTest {
    private static final int days = 60 * 60 * 24;

    private Value day(int size) {
        return value(size * days);
    }

    private Value value(int value) {
        return new Value(value);
    }

    private Value value(long value) {
        return new Value(value);
    }

    private List<Value> values(int... values) {
        return IntStream.of(values).mapToObj(this::value).collect(Collectors.toList());
    }

    static class Value extends IdentifiableModel implements Timelinable {

        public long item;

        Value(int value) {
            this.item = value;
        }

        Value(long value) {
            this.item = value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getId() {
            return item;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void setId(long id) {
            item = id;
        }

        @Override
        public ZonedDateTime date() {
            return Chrono.utcBySeconds(item);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return (int) item;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Value) {
                return ((Value) obj).item == item;
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
        assert store.at(0).item == 0;
        assert store.at(60) == null;
        assert store.at(120) == null;

        // update
        store.store(value(2));
        assert store.at(0).item == 2;
        assert store.at(60) == null;
        assert store.at(120) == null;

        // add next stamp
        store.store(value(60));
        assert store.at(0).item == 2;
        assert store.at(60).item == 60;
        assert store.at(120) == null;

        // add next stamp
        store.store(value(120));
        assert store.at(0).item == 2;
        assert store.at(60).item == 60;
        assert store.at(120).item == 120;

        // update
        store.store(value(156));
        assert store.at(0).item == 2;
        assert store.at(60).item == 60;
        assert store.at(120).item == 156;
    }

    @Test
    void merge() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1);
        assert store.size() == 0;

        FeatherStore<Value> additions = FeatherStore.create(Value.class, Span.Minute1);
        additions.store(value(Span.Minute1.segmentSeconds * 0));
        additions.store(value(Span.Minute1.segmentSeconds * 1));
        additions.store(value(Span.Minute1.segmentSeconds * 2));
        additions.store(value(Span.Minute1.segmentSeconds * 3));
        assert additions.size() == 4;

        store.merge(additions);
        assert store.size() == 4;
        assert store.first().item == 0;
        assert store.last().item == Span.Minute1.segmentSeconds * 3;
    }

    @Test
    void mergeNull() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1);

        Assertions.assertThrows(NullPointerException.class, () -> store.merge(null));
    }

    @Test
    void mergeDifferentSpan() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1);
        FeatherStore<Value> additions = FeatherStore.create(Value.class, Span.Minute15);

        Assertions.assertThrows(IllegalArgumentException.class, () -> store.merge(additions));
    }

    @Test
    void getByTime() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1);
        store.store(value(0), value(60), value(120));
        assert store.at(0).item == 0;
        assert store.at(30).item == 0;
        assert store.at(60).item == 60;
        assert store.at(70).item == 60;
        assert store.at(120).item == 120;
        assert store.at(140).item == 120;
    }

    @Test
    void getByTimeOverTime() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Day);
        store.store(value(0), value(days), value(2 * days), value(3 * days), value(4 * days));
        assert store.at(0).item == 0;
        assert store.at(days - 1).item == 0;
        assert store.at(days).item == days;
        assert store.at(days + 1).item == days;
        assert store.at(2 * days - 1).item == days;
        assert store.at(2 * days).item == 2 * days;
        assert store.at(2 * days + 1).item == 2 * days;
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
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Day);
        store.store(day(0), day(1), day(2), day(3), day(4));
        assert store.size() == 5;
    }

    @Test
    void calculateStartTimeAndRemainderEpochSeconds() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1);

        // 2019-12-12 02:16:30
        Assertions.assertArrayEquals(new long[] {1576116000, 16}, store.index(1576116990));
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
        assert store.before(60).item == 0;
        assert store.before(72).item == 0;
        assert store.before(120).item == 60;
        assert store.before(144).item == 60;
        assert store.before(180).item == 120;
        assert store.before(240).item == 180;
        assert store.before(300) == null;
    }

    @Test
    void beforeOverTime() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Day);
        store.store(day(0), day(1), day(2), day(3), day(4));
        assert store.before(0) == null;
        assert store.before(days - 1) == null;
        assert store.before(days).item == 0;
        assert store.before(days + 1).item == 0;
        assert store.before(2 * days - 1).item == 0;
        assert store.before(2 * days).item == days;
        assert store.before(2 * days + 1).item == days;
    }

    @Test
    void diskCache() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1).enablePersistence("diskCache");

        int base = (int) Span.Minute1.segmentSeconds;

        for (int i = 1; i <= 20; i++) {
            store.store(value(base * i));
        }

        assert store.existOnHeap(value(base * 2)) == false;
        assert store.existOnHeap(value(base * 8)) == false;
        assert store.existOnHeap(value(base * 14)) == false;
        assert store.existOnHeap(value(base * 15));
        assert store.existOnHeap(value(base * 16));
        assert store.existOnHeap(value(base * 17));
        assert store.existOnHeap(value(base * 18));
        assert store.existOnHeap(value(base * 19));
        assert store.existOnHeap(value(base * 20));

        store.store(value(base * 2));
        assert store.existOnHeap(value(base * 2)) == false;
        assert store.existOnHeap(value(base * 14)) == false;
        assert store.existOnHeap(value(base * 15));
        assert store.existOnHeap(value(base * 16));
        assert store.existOnHeap(value(base * 17));
        assert store.existOnHeap(value(base * 18));
        assert store.existOnHeap(value(base * 19));
        assert store.existOnHeap(value(base * 20));
    }

    @Test
    void persist() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1).enablePersistence(randomName());

        store.store(value(0));
        assert store.existOnDisk(value(0)) == false;
        store.commit();
        assert store.existOnDisk(value(0)) == true;
    }

    @Test
    void readDataFromDiskCache() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1).enablePersistence(randomName());

        store.store(value(0));
        store.commit();
        store.clear();
        assert store.existOnHeap(value(0)) == false;
        assert store.at(0).item == 0;
        assert store.at(60) == null;
    }

    @Test
    void queryDataFromDiskCache() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1).enablePersistence(randomName());

        store.store(value(0));
        store.commit();
        store.clear();
        assert store.existOnHeap(value(0)) == false;
        assert store.existOnDisk(value(0)) == true;
        assert store.query(0).toList().getFirst().item == 0;
    }

    @Test
    void supportEnum() {
        Enums e = new Enums();
        e.type = MarketType.DERIVATIVE;
        e.currency = Currency.BTC;

        FeatherStore<Enums> store = FeatherStore.create(Enums.class, Span.Minute1).enablePersistence(randomName());
        store.store(e);
        store.commit();
        store.clear();

        Enums restored = store.at(e.seconds());
        assert restored != null;
        assert restored.type == e.type;
        assert restored.currency == e.currency;
    }

    private static class Enums extends IdentifiableModel implements Timelinable {
        public MarketType type;

        public Currency currency;

        @Override
        public long getId() {
            return seconds();
        }

        @Override
        protected void setId(long id) {
        }

        @Override
        public ZonedDateTime date() {
            return Chrono.utc(2020, 1, 1);
        }
    }

    @Test
    void supportZonedDateTime() {
        FeatherStore<OpenInterest> store = FeatherStore.create(OpenInterest.class, Span.Minute1).enablePersistence(randomName());
        OpenInterest oi = OpenInterest.with.date(Chrono.utc(2020, 1, 1)).size(10);
        store.store(oi);
        store.commit();
        store.clear();
        assert store.at(oi.seconds()).equals(oi);
    }

    @Test
    void storeSparsedDisk() {
        FeatherStore<OpenInterest> store = FeatherStore.create(OpenInterest.class, Span.Day).enablePersistence(randomName());
        OpenInterest oi1 = OpenInterest.with.date(Chrono.utc(2020, 1, 1)).size(10);
        OpenInterest oi2 = OpenInterest.with.date(Chrono.utc(2020, 2, 1)).size(20);
        OpenInterest oi3 = OpenInterest.with.date(Chrono.utc(2020, 3, 1)).size(30);
        store.store(oi1, oi2, oi3);
        store.commit();
        store.clear();

        assert store.at(oi1.seconds()).equals(oi1);
        assert store.at(oi2.seconds()).equals(oi2);
        assert store.at(oi3.seconds()).equals(oi3);
    }

    @Test
    void endTimeWithDisk() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1).enablePersistence(randomName());
        assert store.lastTime() == -1;

        store.store(new Value(60));
        assert store.lastTime() == 60;

        store.store(new Value(600));
        assert store.lastTime() == 600;

        store.commit();
        store.clear();
        assert store.lastTime() == 600;
    }

    @Test
    void dontCreateEmptySegmentWhenReadModeAt() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1);
        store.at(1); // try to create empty segment
        assert store.isEmpty();
    }

    @Test
    void dontCreateEmptySegmentWhenReadModeQuery() {
        FeatherStore<Value> store = FeatherStore.create(Value.class, Span.Minute1);
        store.query(1, 20); // try to create empty segment
        assert store.isEmpty();
    }

    String randomName() {
        return RandomStringUtils.insecure().nextAlphabetic(20);
    }
}