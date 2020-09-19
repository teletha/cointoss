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
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Iterators;

import cointoss.util.primitive.maps.ConcurrentNavigableLongMap;
import cointoss.util.primitive.maps.LongMap;
import cointoss.util.primitive.maps.LongMap.LongEntry;

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

    private <V> V findFirst(Iterator<V> iterator) {
        return Iterators.get(iterator, 0);
    }

    private <V> V findLast(Iterator<V> iterator) {
        return Iterators.getLast(iterator);
    }

    private void removeFisrt(Iterator iterator) {
        if (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }
}
