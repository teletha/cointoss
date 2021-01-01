/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.execution;

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.util.Chrono;
import kiss.I;

class FastLogTest {

    private static final ZonedDateTime base = Chrono.utc(2021, 1, 1);

    @Test
    void buyOnly() {
        Execution e1 = Execution.with.buy(1).price(100).date(base);
        Execution e2 = Execution.with.buy(1).price(150).date(base);
        Execution e3 = Execution.with.buy(1).price(120).date(base);
        Execution e4 = Execution.with.buy(1).price(90).date(base);
        Execution e5 = Execution.with.buy(1).price(140).date(base);

        List<Execution> fast = I.signal(e1, e2, e3, e4, e5).plug(new FastLog(2)).toList();
        assert fast.size() == 4;
        assert equals(fast.get(0), Execution.with.buy(1.25).price(100).date(base));
        assert equals(fast.get(1), Execution.with.buy(1.25).price(90).date(base.plusSeconds(1)));
        assert equals(fast.get(2), Execution.with.buy(1.25).price(150).date(base.plusSeconds(2)));
        assert equals(fast.get(3), Execution.with.buy(1.25).price(140).date(base.plusSeconds(3)));
    }

    @Test
    void sellOnly() {
        Execution e1 = Execution.with.sell(1).price(100).date(base);
        Execution e2 = Execution.with.sell(1).price(150).date(base);
        Execution e3 = Execution.with.sell(1).price(120).date(base);
        Execution e4 = Execution.with.sell(1).price(90).date(base);
        Execution e5 = Execution.with.sell(1).price(140).date(base);

        List<Execution> fast = I.signal(e1, e2, e3, e4, e5).plug(new FastLog(2)).toList();
        assert fast.size() == 4;
        assert equals(fast.get(0), Execution.with.sell(1.25).price(100).date(base));
        assert equals(fast.get(1), Execution.with.sell(1.25).price(90).date(base.plusSeconds(1)));
        assert equals(fast.get(2), Execution.with.sell(1.25).price(150).date(base.plusSeconds(2)));
        assert equals(fast.get(3), Execution.with.sell(1.25).price(140).date(base.plusSeconds(3)));
    }

    @Test
    void buyAndSell() {
        Execution e1 = Execution.with.sell(1).price(100).date(base);
        Execution e2 = Execution.with.buy(1).price(150).date(base);
        Execution e3 = Execution.with.sell(1).price(120).date(base);
        Execution e4 = Execution.with.buy(1).price(90).date(base);
        Execution e5 = Execution.with.sell(1).price(140).date(base);

        List<Execution> fast = I.signal(e1, e2, e3, e4, e5).plug(new FastLog(2)).toList();
        assert fast.size() == 4;
        assert equals(fast.get(0), Execution.with.sell(1.5).price(100).date(base));
        assert equals(fast.get(1), Execution.with.buy(1).price(90).date(base.plusSeconds(1)));
        assert equals(fast.get(2), Execution.with.sell(1.5).price(150).date(base.plusSeconds(2)));
        assert equals(fast.get(3), Execution.with.buy(1).price(140).date(base.plusSeconds(3)));
    }

    @Test
    void buy1() {
        Execution e1 = Execution.with.buy(1).price(100);

        List<Execution> fast = I.signal(e1).plug(new FastLog(2)).toList();
        assert fast.size() == 1;
        assert equals(fast.get(0), Execution.with.buy(1).price(100));
    }

    @Test
    void sell1() {
        Execution e1 = Execution.with.sell(1).price(100);

        List<Execution> fast = I.signal(e1).plug(new FastLog(2)).toList();
        assert fast.size() == 1;
        assert equals(fast.get(0), Execution.with.sell(1).price(100));
    }

    @Test
    void none() {
        List<Execution> fast = I.<Execution> signal().plug(new FastLog(2)).toList();
        assert fast.size() == 0;
    }

    @Test
    void multitTicks() {
        Execution e1 = Execution.with.sell(1).price(100).date(base);
        Execution e2 = Execution.with.buy(1).price(150).date(base);
        Execution e3 = Execution.with.sell(1).price(120).date(base.plusSeconds(5));
        Execution e4 = Execution.with.buy(1).price(90).date(base.plusSeconds(5));
        Execution e5 = Execution.with.sell(1).price(140).date(base.plusSeconds(5));

        List<Execution> fast = I.signal(e1, e2, e3, e4, e5).plug(new FastLog(2)).toList();
        assert fast.size() == 8;
        assert equals(fast.get(0), Execution.with.sell(0.5).price(100).date(base));
        assert equals(fast.get(1), Execution.with.buy(0.5).price(100).date(base.plusSeconds(1)));
        assert equals(fast.get(2), Execution.with.sell(0.5).price(150).date(base.plusSeconds(2)));
        assert equals(fast.get(3), Execution.with.buy(0.5).price(150).date(base.plusSeconds(3)));

        assert equals(fast.get(4), Execution.with.sell(1).price(120).date(base.plusSeconds(5)));
        assert equals(fast.get(5), Execution.with.buy(0.5).price(90).date(base.plusSeconds(6)));
        assert equals(fast.get(6), Execution.with.sell(1).price(140).date(base.plusSeconds(7)));
        assert equals(fast.get(7), Execution.with.buy(0.5).price(140).date(base.plusSeconds(8)));
    }

    private boolean equals(Execution one, Execution other) {
        assert one.direction == other.direction;
        assert one.size.equals(other.size);
        assert one.price.equals((other.price));
        assert one.date.equals(other.date);
        return true;
    }
}
