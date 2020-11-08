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
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

import cointoss.util.Chrono;
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

    /**
     * @param definition
     */
    FileDB(String name, TableDefinition definition) {
        this.name = name;
        this.definition = definition;
        this.dir = Locator.directory(name).create();

        // refreshWriterQueue();
    }

    /**
     * Insert items.
     * 
     * @param items A list of items to insert.
     * @return Chainable API.
     */
    public void insert(T... items) {
        insert(I.signal(items));
    }

    /**
     * Insert items.
     * 
     * @param items A list of items to insert.
     * @return Chainable API.
     */
    public void insert(Iterable<T> items) {
        insert(I.signal(items));
    }

    /**
     * Insert items.
     * 
     * @param items A list of items to insert.
     * @return Chainable API.
     */
    public void insert(Signal<T> items) {
        items.buffer(1000).effect(e -> System.out.println(e)).effect(this::write).to(e -> {
            System.out.println(e);
        });
    }

    // /**
    // * Refresh writing queue.
    // *
    // * @return
    // */
    // private ConcurrentSkipListSet<T> refreshWriterQueue() {
    // ConcurrentSkipListSet<T> now = writerQueue;
    // writerQueue = new
    // ConcurrentSkipListSet<>(Comparator.comparingLong(definition.timestampExtractor));
    // return now;
    // }
    //
    // /**
    // *
    // */
    // private synchronized void wakeUpWriter() {
    // if (writer == null) {
    // writer = I.schedule(1, TimeUnit.SECONDS).effectOnDispose(() -> writer = null).to(() -> {
    // for (T item : refreshWriterQueue()) {
    // write(item);
    // }
    // });
    // }
    // }

    private void write(List<T> items) {
        File file = dir.file("0" + ".db");
        try (FileChannel channel = FileChannel.open(file.asJavaPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {

            for (T item : items) {
                long timestamp = definition.timestampExtractor.applyAsLong(item);
                long remaining = timestamp % definition.duration;
                long index = timestamp - remaining;
                long height = remaining / definition.span.seconds;

                channel.position(height * definition.widthTotal);

                ByteBuffer buffer = ByteBuffer.allocate(definition.widthTotal);
                for (int i = 0; i < definition.width.length; i++) {
                    definition.writers[i].accept(item, buffer);
                }
                channel.write(buffer.flip());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw I.quiet(e);
        }
    }

    public Signal<T> at(long timestamp) {
        return range(timestamp, 1);
    }

    public Signal<T> range(long timestamp, long size) {
        return new Signal<T>((observer, disposer) -> {
            long remaining = timestamp % definition.duration;
            long index = timestamp - remaining;
            long height = remaining / definition.span.seconds;

            File file = dir.file(index + ".db");
            try (FileChannel channel = FileChannel.open(file.asJavaPath(), StandardOpenOption.READ)) {
                for (int j = 0; j < size; j++) {
                    channel.position((height + j) * definition.widthTotal);

                    T item = (T) I.make(definition.model.type);
                    ByteBuffer buffer = ByteBuffer.allocate(definition.widthTotal);
                    channel.read(buffer);
                    buffer.flip();
                    for (int i = 0; i < definition.width.length; i++) {
                        definition.readers[i].accept(item, buffer);
                    }

                    observer.accept(item);
                }
                observer.complete();
            } catch (Exception e) {
                observer.error(e);
            }
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
}
