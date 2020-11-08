/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.db;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

import cointoss.util.Chrono;
import kiss.Disposable;
import kiss.I;
import kiss.Signal;
import psychopath.Directory;
import psychopath.File;
import psychopath.Locator;

public class FileDB<T> {

    /** The database name. */
    private final String name;

    /** The database definition. */
    private final TableDefinition definition;

    /** The local database file. */
    private final Directory dir;

    /** The writing item queue. */
    private ConcurrentSkipListSet<T> writerQueue;

    /** The writing item thread. */
    private Disposable writer;

    /**
     * @param definition
     */
    FileDB(String name, TableDefinition definition) {
        this.name = name;
        this.definition = definition;
        this.dir = Locator.directory(name).create();

        refreshWriterQueue();
    }

    /**
     * Insert items.
     * 
     * @param items A list of items to insert.
     * @return Chainable API.
     */
    public Signal<T> insert(T... items) {
        return insert(I.signal(items));
    }

    /**
     * Insert items.
     * 
     * @param items A list of items to insert.
     * @return Chainable API.
     */
    public Signal<T> insert(Iterable<T> items) {
        return insert(I.signal(items));
    }

    /**
     * Insert items.
     * 
     * @param items A list of items to insert.
     * @return Chainable API.
     */
    public Signal<T> insert(Signal<T> items) {
        return items.effectOnce(this::wakeUpWriter).effect(writerQueue::add);
    }

    /**
     * Refresh writing queue.
     * 
     * @return
     */
    private ConcurrentSkipListSet<T> refreshWriterQueue() {
        ConcurrentSkipListSet<T> now = writerQueue;
        writerQueue = new ConcurrentSkipListSet<>(Comparator.comparingLong(definition.timestampExtractor));
        return now;
    }

    /**
     * 
     */
    private synchronized void wakeUpWriter() {
        if (writer == null) {
            writer = I.schedule(1, TimeUnit.SECONDS).effectOnDispose(() -> writer = null).to(() -> {
                for (T item : refreshWriterQueue()) {
                    write(item);
                }
            });
        }
    }

    private void write(T item) {
        long timestamp = definition.timestampExtractor.applyAsLong(item);
        long remaining = timestamp % definition.duration;
        long index = timestamp - remaining;
        long height = remaining / definition.span.seconds;

        File file = dir.file(index + ".db");
        try (FileChannel channel = FileChannel.open(file.asJavaPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            channel.position(height * definition.widthTotal);

            ByteBuffer buffer = ByteBuffer.allocate(definition.widthTotal);
            for (int i = 0; i < definition.width.length; i++) {
                definition.writers[i].accept(item, buffer);
            }
            channel.write(buffer.flip());
        } catch (IOException e) {
            e.printStackTrace();
            throw I.quiet(e);
        }
    }

    public Signal<T> at(long time) {
        return new Signal<T>((observer, disposer) -> {

            return disposer;
        });
    }

    public T at(ZonedDateTime point) {
        return null;
    }

    public Signal<T> from(ZonedDateTime start) {
        return range(start, Chrono.utcNow());
    }

    public Signal<T> range(ZonedDateTime start, ZonedDateTime end) {
        return null;
    }

    private class Writer {

        /** The writing item queue. */
        private ConcurrentSkipListSet<T> writerQueue = new ConcurrentSkipListSet<>(Comparator.comparingLong(definition.timestampExtractor));

    }
}
