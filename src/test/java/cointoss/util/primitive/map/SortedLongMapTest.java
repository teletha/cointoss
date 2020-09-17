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

import org.junit.jupiter.api.Test;

import cointoss.util.primitive.maps.ConcurrentNavigableLongMap;
import cointoss.util.primitive.maps.LongMap;

class SortedLongMapTest {

    @Test
    void get() {
        ConcurrentNavigableLongMap<String> map = LongMap.createSortedMap();
        for (int i = 0; i < 10; i++) {
            String value = String.valueOf(i);

            map.put(i, value);
            assert map.get(i).equals(value);
        }
    }
}
