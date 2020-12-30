/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.ticker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;

class TimeDBTest {

    @RegisterExtension
    CleanRoom room = new CleanRoom();

    @Test
    void write() {
        TimeDB<Data> db = new TimeDB<>(Span.Second5, Data.class, room.root);
        db.write(0, integer(1));
        assert db.read(0).integer == 1;
    }

    @Test
    void writeMultiple() {
        TimeDB<Data> db = new TimeDB<>(Span.Second5, Data.class, room.root);
        db.write(0, integer(1));
        db.write(5, integer(2));
        db.write(10, integer(3));
        assert db.read(0).integer == 1;
        assert db.read(5).integer == 2;
        assert db.read(10).integer == 3;
    }

    Data integer(int value) {
        Data d = new Data();
        d.integer = value;
        return d;
    }

    static class Data {
        public int integer;
    }
}
