/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.feather;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import psychopath.Locator;

public class DiskStorageTest {

    @RegisterExtension
    CleanRoom room = new CleanRoom();

    /**
     * Helper to create new storage.
     * 
     * @param <T>
     * @param type
     * @param duration
     * @return
     */
    private <T> DiskStorage<T> createStorage(Class<T> type, long duration) {
        return new DiskStorage(Locator.file(room.locateRadom()), DataCodec.of(type), duration);
    }

    @Test
    void read() {
        DiskStorage<IntValue> storage = createStorage(IntValue.class, 1);
        storage.write(0, new IntValue(0), new IntValue(1), new IntValue(2));

        IntValue[] items = new IntValue[1];
        assert storage.read(0, items) == 1;
        assert items[0].value == 0;

        items = new IntValue[2];
        assert storage.read(0, items) == 2;
        assert items[0].value == 0;
        assert items[1].value == 1;

        items = new IntValue[3];
        assert storage.read(0, items) == 3;
        assert items[0].value == 0;
        assert items[1].value == 1;
        assert items[2].value == 2;
    }

    @Test
    void readOverflow() {
        DiskStorage<IntValue> storage = createStorage(IntValue.class, 1);
        storage.write(0, new IntValue(0), new IntValue(1), new IntValue(2));

        IntValue[] items = new IntValue[4];
        assert storage.read(0, items) == 3;
        assert items[0].value == 0;
        assert items[1].value == 1;
        assert items[2].value == 2;
        assert items[3] == null;

        items = new IntValue[5];
        assert storage.read(0, items) == 3;
        assert items[0].value == 0;
        assert items[1].value == 1;
        assert items[2].value == 2;
        assert items[3] == null;
        assert items[4] == null;
    }

    @Test
    void readUnderflow() {
        DiskStorage<IntValue> storage = createStorage(IntValue.class, 1);
        storage.write(0, new IntValue(0), new IntValue(1), new IntValue(2));

        IntValue[] items = new IntValue[1];
        assert storage.read(2, items) == 1;
        assert items[0].value == 2;

        items = new IntValue[3];
        assert storage.read(2, items) == 1;
        assert items[0].value == 2;
        assert items[1] == null;
        assert items[2] == null;

        items = new IntValue[3];
        assert storage.read(5, items) == 0;
        assert items[0] == null;
        assert items[1] == null;
        assert items[2] == null;
    }

    @Test
    void readNoItem() {
        DiskStorage<IntValue> storage = createStorage(IntValue.class, 1);

        assert storage.read(0, new IntValue[1]) == 0;
        assert storage.read(0, new IntValue[2]) == 0;
        assert storage.read(5, new IntValue[3]) == 0;
    }

    @Test
    void readSparse() {
        DiskStorage<IntValue> storage = createStorage(IntValue.class, 1);
        storage.write(0, new IntValue(0), null, new IntValue(2));

        IntValue[] items = new IntValue[3];
        assert storage.read(0, items) == 2;
        assert items[0].value == 0;
        assert items[1] == null;
        assert items[2].value == 2;
    }

    @Test
    void readSequentialSparse() {
        DiskStorage<IntValue> storage = createStorage(IntValue.class, 1);
        storage.write(0, new IntValue(0), null, null, new IntValue(3));

        IntValue[] items = new IntValue[4];
        assert storage.read(0, items) == 2;
        assert items[0].value == 0;
        assert items[1] == null;
        assert items[2] == null;
        assert items[3].value == 3;
    }

    @Test
    void readDiscreteSparse() {
        DiskStorage<IntValue> storage = createStorage(IntValue.class, 1);
        storage.write(0, null, new IntValue(1), null, new IntValue(3), null);

        IntValue[] items = new IntValue[5];
        assert storage.read(0, items) == 2;
        assert items[0] == null;
        assert items[1].value == 1;
        assert items[2] == null;
        assert items[3].value == 3;
        assert items[4] == null;
    }

    @Test
    void startTime() {
        DiskStorage<IntValue> storage = createStorage(IntValue.class, 1);
        assert storage.startTime() == Long.MAX_VALUE;

        // insert item
        storage.write(10, new IntValue(10));
        assert storage.startTime() == 10;

        // insert the post item
        storage.write(15, new IntValue(15));
        assert storage.startTime() == 10;

        // insert the pre item
        storage.write(5, new IntValue(5));
        assert storage.startTime() == 5;
    }

    @Test
    void endTime() {
        DiskStorage<IntValue> storage = createStorage(IntValue.class, 1);
        assert storage.endTime() == -1;

        // insert item
        storage.write(10, new IntValue(10));
        assert storage.endTime() == 10;

        // insert the post item
        storage.write(15, new IntValue(15));
        assert storage.endTime() == 15;

        // insert the pre item
        storage.write(5, new IntValue(5));
        assert storage.endTime() == 15;
    }

    @Test
    void rebuildAutomatically() {
        DiskStorage<IntValue> storage = createStorage(IntValue.class, 1);
        int base = 60 * 60 * 24 * 35;
        storage.write(base, new IntValue(base), null, new IntValue(base + 2));
        assert storage.offsetTime() == 60 * 60 * 24 * 31;

        IntValue[] items = new IntValue[3];
        assert storage.read(base, items) == 2;
        assert items[0].value == base;
        assert items[1] == null;
        assert items[2].value == base + 2;

        storage.write(0, new IntValue(0));
        assert storage.offsetTime() == 0;

        items = new IntValue[3];
        assert storage.read(0, items) == 1;
        assert items[0].value == 0;
        assert items[1] == null;
        assert items[2] == null;

        items = new IntValue[3];
        assert storage.read(base, items) == 2;
        assert items[0].value == base;
        assert items[1] == null;
        assert items[2].value == base + 2;
    }

    public record IntValue(int value) {
    }
}
