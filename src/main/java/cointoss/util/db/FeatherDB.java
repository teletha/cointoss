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
import java.util.List;
import java.util.function.ToLongFunction;

import cointoss.ticker.Span;
import cointoss.util.Chrono;
import kiss.I;
import kiss.Signal;
import psychopath.Directory;
import psychopath.File;
import psychopath.Locator;

/**
 * Featherweight (less-memory) timeserise database.
 */
public class FeatherDB<T> {

    /** The database definition. */
    private final FeatherDefinition define;

    /** The local database file. */
    private final Directory dir;

    /**
     * @param definition
     */
    FeatherDB(String name, FeatherDefinition definition) {
        this.define = definition;
        this.dir = Locator.directory(name).create();
    }

    /**
     * Insert items.
     * 
     * @param items A list of items to insert.
     * @return Chainable API.
     */
    public void insert(T... items) {
        insert(List.of(items));
    }

    /**
     * Insert items.
     * 
     * @param items A list of items to insert.
     * @return Chainable API.
     */
    public void insert(List<T> items) {
        try (FileChannel channel = FileChannel
                .open(dir.file("0" + ".db").asJavaPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            ByteBuffer writer = ByteBuffer.allocate(define.widthTotal * Math.min(define.writerSize, items.size()));

            for (T item : items) {
                long timestamp = define.timestamper.applyAsLong(item);
                long remaining = timestamp % define.duration;
                long index = timestamp - remaining;
                long height = remaining / define.span.seconds;

                channel.position(height * define.widthTotal);

                for (int i = 0; i < define.width.length; i++) {
                    define.writers[i].accept(item, writer);
                }

                if (writer.remaining() == 0) {
                    channel.write(writer.flip());
                    writer.clear();
                }
            }
            channel.write(writer.flip());
            System.out.println("Write all");
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * Insert items.
     * 
     * @param items A list of items to insert.
     * @return Chainable API.
     */
    public void insert(Signal<T> items) {
        insert(items.toList());
    }

    public Signal<T> at(long timestamp) {
        return after(timestamp, 1);
    }

    public Signal<T> after(long timestamp, int size) {
        return new Signal<T>((observer, disposer) -> {
            long remaining = timestamp % define.duration;
            long index = timestamp - remaining;
            long height = remaining / define.span.seconds;

            File file = dir.file(index + ".db");

            try (FileChannel ch = FileChannel.open(file.asJavaPath(), StandardOpenOption.READ)) {
                for (int i = 0; i < size;) {
                    ByteBuffer bytes = ByteBuffer.allocate(define.widthTotal * Math.min(define.readerSize, size - i));
                    int red = ch.read(bytes, (height + i) * define.widthTotal) / define.widthTotal;
                    bytes.flip();
                    i += red;

                    for (int j = 0; j < red; j++) {
                        T item = (T) I.make(define.model.type);
                        for (int k = 0; k < define.width.length; k++) {
                            define.readers[k].accept(item, bytes);
                        }
                        observer.accept(item);
                    }
                }
                observer.complete();
            } catch (IOException e) {
                throw I.quiet(e);
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

    /**
     * Define database.
     * 
     * @param <T>
     * @param span
     * @param modelType
     * @param timestamp
     * @return
     */
    public static <T> FeatherDefinition<T> define(Span span, Class<T> modelType, ToLongFunction<T> timestamp) {
        return new FeatherDefinition(span, modelType, timestamp);
    }
}
