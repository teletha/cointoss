/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.position;

import org.junit.jupiter.api.Test;

import cointoss.order.Order;
import cointoss.util.Num;

class EntryTest {

    @Test
    void entryExecutedSize() {
        Order order1 = Order.buy(10).price(100);
        Order order2 = Order.buy(5).price(100);

        Entry entry = new Entry();
        entry.addEntry(order1);
        entry.addEntry(order2);
        assert entry.entryExecutedSize.v.is(0);

        order1.executedSize.set(Num.of(1));
        assert entry.entryExecutedSize.v.is(1);
        order1.executedSize.set(Num.of(2));
        assert entry.entryExecutedSize.v.is(2);
        order1.executedSize.set(Num.of(3));
        assert entry.entryExecutedSize.v.is(3);

        order2.executedSize.set(Num.of(1));
        assert entry.entryExecutedSize.v.is(4);
    }

    @Test
    void entryRemainingSize() {
        Order order1 = Order.buy(10).price(100);
        Order order2 = Order.buy(5).price(100);

        Entry entry = new Entry();
        entry.addEntry(order1);
        entry.addEntry(order2);
        assert entry.entryRemainingSize.v.is(15);

        order1.remainingSize.set(Num.of(9));
        assert entry.entryRemainingSize.v.is(14);
        order1.remainingSize.set(Num.of(8));
        assert entry.entryRemainingSize.v.is(13);
        order1.remainingSize.set(Num.of(7));
        assert entry.entryRemainingSize.v.is(12);

        order2.remainingSize.set(Num.of(4));
        assert entry.entryRemainingSize.v.is(11);
    }

    @Test
    void positionSize() {
        Order in = Order.buy(10).price(100);
        Order out = Order.buy(5).price(100);

        Entry entry = new Entry();
        entry.addEntry(in);
        entry.addExit(out);
        assert entry.positionSize.v.is(0);

        in.executedSize.set(Num.of(1));
        assert entry.positionSize.v.is(1);
        in.executedSize.set(Num.of(3));
        assert entry.positionSize.v.is(3);
        in.executedSize.set(Num.of(5));
        assert entry.positionSize.v.is(5);

        out.executedSize.set(Num.of(2));
        assert entry.positionSize.v.is(3);
    }

    @Test
    void entyPice() {

    }
}
