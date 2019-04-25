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

import java.util.LinkedList;

import cointoss.util.Num;
import kiss.Signaling;
import kiss.Variable;

public class EntryManager {

    private final LinkedList<Entry> items = new LinkedList();

    private final Signaling<Entry> entries = new Signaling();

    public final Variable<Num> size = entries.expose.flatMap(v -> v.positionSize.diff())
            .scanWith(Num.ZERO, Num::plus)
            .startWith(Num.ZERO)
            .to();

    public void add(Entry entry) {
        entries.accept(entry);
    }
}
