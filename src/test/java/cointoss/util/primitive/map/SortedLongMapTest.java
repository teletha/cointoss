/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.primitive.map;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.common.base.Objects;
import com.google.common.collect.Iterators;

import cointoss.util.primitive.map.ConcurrentNavigableLongMap;
import cointoss.util.primitive.map.LongMap;
import cointoss.util.primitive.map.LongMap.LongEntry;

class SortedLongMapTest {

    @Test
    void putAndGet() {
        ConcurrentNavigableLongMap<String> map = LongMap.createSortedMap();
        for (int i = 0; i < 10; i++) {
            String value = String.valueOf(i);

            map.put(i, value);
            assert map.get(i).equals(value);
        }
    }

    @Test
    void size() {
        ConcurrentNavigableLongMap<Integer> map = LongMap.createSortedMap();
        for (int i = 1; i < 10; i++) {
            map.put(i, i);
            assert map.size() == i;
        }
    }

    @Test
    void remove() {
        ConcurrentNavigableLongMap<Integer> map = LongMap.createSortedMap();
        for (int i = 1; i < 10; i++) {
            map.put(i, i);
            assert map.remove(i) == i;
            assert map.get(i) == null;
        }
    }

    @Test
    void longEntrySet() {
        ConcurrentNavigableLongMap<Integer> map = LongMap.createSortedMap();
        Set<LongEntry<Integer>> liveView = map.longEntrySet();

        for (int i = 0; i < 10; i++) {
            map.put(i, i);

            // map
            assert liveView.size() == i + 1;
            assert liveView.contains(LongEntry.immutable(i, i));

            // iterator
            assert Iterators.size(liveView.iterator()) == i + 1;
            assert findLast(liveView.iterator()).getLongKey() == i;
        }

        for (int i = 0; i < 9; i++) {
            // remove from iterator
            removeFisrt(liveView.iterator());
            assert findFirst(liveView.iterator()).getLongKey() == i + 1;
            assert map.firstLongKey() == i + 1;
        }
    }

    @Test
    void keySet() {
        ConcurrentNavigableLongMap<Integer> map = LongMap.createSortedMap();
        Set<Long> liveView = map.keySet();

        for (int i = 0; i < 10; i++) {
            map.put(i, i);

            // map
            assert liveView.size() == i + 1;
            assert liveView.contains((long) i);

            // iterator
            assert Iterators.size(liveView.iterator()) == i + 1;
            assert findLast(liveView.iterator()) == i;
        }

        for (int i = 0; i < 9; i++) {
            // remove from iterator
            removeFisrt(liveView.iterator());
            assert findFirst(liveView.iterator()) == i + 1;
            assert map.firstLongKey() == i + 1;
        }
    }

    @Test
    void values() {
        ConcurrentNavigableLongMap<Integer> map = LongMap.createSortedMap();
        Collection<Integer> liveView = map.values();

        for (int i = 0; i < 10; i++) {
            map.put(i, i);

            // map
            assert liveView.size() == i + 1;
            assert liveView.contains(i);

            // iterator
            assert Iterators.size(liveView.iterator()) == i + 1;
            assert findLast(liveView.iterator()) == i;
        }

        for (int i = 0; i < 9; i++) {
            // remove from iterator
            removeFisrt(liveView.iterator());
            assert findFirst(liveView.iterator()) == i + 1;
            assert map.firstLongKey() == i + 1;
        }
    }

    @Test
    void subMapSize() {
        ConcurrentNavigableLongMap<Integer> map = LongMap.createSortedMap();
        ConcurrentNavigableLongMap<Integer> sub = map.subMap(5, 15);

        for (int i = 0; i < 10; i++) {
            map.put(i, i);
            assert sub.size() == Math.max(0, i - 4);
        }

        // add from sub
        sub.put(10, 10);
        assert map.size() == 11;
    }

    @Test
    void subMapContainsKey() {
        ConcurrentNavigableLongMap<Integer> map = LongMap.createSortedMap();
        ConcurrentNavigableLongMap<Integer> sub = map.subMap(5, 15);

        for (int i = 0; i < 10; i++) {
            map.put(i, i);
            assert sub.containsKey(i) == 5 <= i;
        }

        // add from sub
        sub.put(10, 10);
        assert map.containsKey(10);
    }

    @Test
    void subMapGet() {
        ConcurrentNavigableLongMap<Integer> map = LongMap.createSortedMap();
        ConcurrentNavigableLongMap<Integer> sub = map.subMap(5, 15);

        for (int i = 0; i < 10; i++) {
            map.put(i, i);
            assert sub.get(i) == (i < 5 ? null : i);
        }

        // add from sub
        sub.put(10, 10);
        assert map.get(10) == 10;
    }

    @Test
    void subMapRemove() {
        ConcurrentNavigableLongMap<Integer> map = LongMap.createSortedMap();
        ConcurrentNavigableLongMap<Integer> sub = map.subMap(5, 15);

        for (int i = 0; i < 10; i++) {
            map.put(i, i);
            assert sub.remove(i) == (i < 5 ? null : i);
            assert map.get(i) == (i < 5 ? i : null);
        }
    }

    @Test
    void subMapKeySet() {
        ConcurrentNavigableLongMap<Integer> map = LongMap.createSortedMap();
        ConcurrentNavigableLongMap<Integer> sub = map.subMap(5, 15);

        Set<Long> liveView = sub.keySet();
        for (int i = 0; i < 10; i++) {
            map.put(i, i);

            // map
            assert liveView.size() == (i < 5 ? 0 : i - 4);
            assert liveView.contains((long) i) == 5 <= i;

            // iterator
            assert Iterators.size(liveView.iterator()) == (i < 5 ? 0 : i - 4);
            assert findLast(liveView.iterator()) == (i < 5 ? null : (long) i);
        }

        for (int i = 0; i < 4; i++) {
            // remove from iterator
            removeFisrt(liveView.iterator());
            assert findFirst(liveView.iterator()) == i + 6;
            assert map.firstLongKey() == 0;
            assert map.size() == 9 - i;
        }
    }

    @Test
    void subMapValues() {
        ConcurrentNavigableLongMap<Integer> map = LongMap.createSortedMap();
        ConcurrentNavigableLongMap<Integer> sub = map.subMap(5, 15);

        Collection<Integer> liveView = sub.values();
        for (int i = 0; i < 10; i++) {
            map.put(i, i);

            // map
            assert liveView.size() == (i < 5 ? 0 : i - 4);
            assert liveView.contains(i) == 5 <= i;

            // iterator
            assert Iterators.size(liveView.iterator()) == (i < 5 ? 0 : i - 4);
            assert findLast(liveView.iterator()) == (i < 5 ? null : i);
        }

        for (int i = 0; i < 4; i++) {
            // remove from iterator
            removeFisrt(liveView.iterator());
            assert findFirst(liveView.iterator()) == i + 6;
            assert map.firstLongKey() == 0;
            assert map.size() == 9 - i;
        }
    }

    @Test
    void subMapLongEntrySet() {
        ConcurrentNavigableLongMap<Integer> map = LongMap.createSortedMap();
        ConcurrentNavigableLongMap<Integer> sub = map.subMap(5, 15);

        Set<LongEntry<Integer>> liveView = sub.longEntrySet();
        for (int i = 0; i < 10; i++) {
            map.put(i, i);

            // map
            assert liveView.size() == (i < 5 ? 0 : i - 4);
            assert liveView.contains(LongEntry.immutable(i, i)) == 5 <= i;

            // iterator
            assert Iterators.size(liveView.iterator()) == (i < 5 ? 0 : i - 4);
            assert Objects.equal(findLast(liveView.iterator()), i < 5 ? null : LongEntry.immutable(i, i));
        }

        for (int i = 0; i < 4; i++) {
            // remove from iterator
            removeFisrt(liveView.iterator());
            assert findFirst(liveView.iterator()).getLongKey() == i + 6;
            assert map.firstLongKey() == 0;
            assert map.size() == 9 - i;
        }
    }

    private <V> V findFirst(Iterator<V> iterator) {
        try {
            return Iterators.get(iterator, 0);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private <V> V findLast(Iterator<V> iterator) {
        try {
            return Iterators.getLast(iterator);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    private void removeFisrt(Iterator iterator) {
        if (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }
}
