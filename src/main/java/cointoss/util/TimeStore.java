/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import org.h2.mvstore.MVStore;

import cointoss.ticker.Span;
import kiss.model.Model;
import psychopath.Directory;

public class TimeStore<E extends TimeStorable> {

    public static <T extends TimeStorable> TimeStore<T> create(Class<T> type, Span span) {
        return create(type, span, null);
    }

    public static <T extends TimeStorable> TimeStore<T> create(Class<T> type, Span span, Directory disk) {
        return new TimeStore(type, span, disk);
    }

    private final Model<E> model;

    private final MVStore store;

    private TimeStore(Class<E> type, Span spam, Directory disk) {
        this.model = Model.of(type);
        this.store = MVStore.open(disk == null ? null : disk.file(model.name.concat(".db")).toString());
    }
}
