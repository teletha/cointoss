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

import static java.nio.file.StandardOpenOption.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import cointoss.util.arithmetic.Num;
import kiss.I;
import kiss.Signal;
import kiss.model.Model;
import kiss.model.Property;

/**
 * Featherweight (less-memory) timeserise database.
 */
public class TimeDB<T> {

    /** The time interval. */
    private final Span span;

    /** The local database file. */
    private final Path dir;

    private final long duration;

    private final Model<T> model;

    private final int[] width;

    private final int widthTotal;

    private final BiConsumer<T, ByteBuffer>[] readers;

    private final BiConsumer<T, ByteBuffer>[] writers;

    /**
     * @param definition
     */
    TimeDB(Span span, Class<T> type, Path root) {
        this.span = span;
        this.dir = root;
        this.duration = span.segmentSeconds;
        this.model = Model.of(type);

        List<Property> properties = model.properties();
        this.width = new int[properties.size()];
        this.readers = new BiConsumer[width.length];
        this.writers = new BiConsumer[width.length];

        for (int i = 0; i < width.length; i++) {
            Property property = properties.get(i);
            Class c = property.model.type;
            if (c == boolean.class) {
                width[i] = 1;
                readers[i] = (o, b) -> model.set(o, property, b.get() == 0 ? Boolean.FALSE : Boolean.TRUE);
                writers[i] = (o, b) -> b.put((byte) (model.get(o, property) == Boolean.FALSE ? 0 : 1));
            } else if (c == byte.class) {
                width[i] = 1;
                readers[i] = (o, b) -> model.set(o, property, b.get());
                writers[i] = (o, b) -> b.put((byte) model.get(o, property));
            } else if (c == short.class) {
                width[i] = 2;
                readers[i] = (o, b) -> model.set(o, property, b.getShort());
                writers[i] = (o, b) -> b.putShort((short) model.get(o, property));
            } else if (c == char.class) {
                width[i] = 2;
                readers[i] = (o, b) -> model.set(o, property, b.getChar());
                writers[i] = (o, b) -> b.putChar((char) model.get(o, property));
            } else if (c == int.class) {
                width[i] = 4;
                readers[i] = (o, b) -> model.set(o, property, b.getInt());
                writers[i] = (o, b) -> b.putInt((int) model.get(o, property));
            } else if (c == float.class) {
                width[i] = 4;
                readers[i] = (o, b) -> model.set(o, property, b.getFloat());
                writers[i] = (o, b) -> b.putFloat((float) model.get(o, property));
            } else if (c == long.class) {
                width[i] = 8;
                readers[i] = (o, b) -> model.set(o, property, b.getLong());
                writers[i] = (o, b) -> b.putLong((long) model.get(o, property));
            } else if (c == double.class) {
                width[i] = 8;
                readers[i] = (o, b) -> model.set(o, property, b.getDouble());
                writers[i] = (o, b) -> b.putDouble((double) model.get(o, property));
            } else if (c.isEnum()) {
                int size = c.getEnumConstants().length;
                if (size < 8) {
                    width[i] = 1;
                    readers[i] = (o, b) -> model.set(o, property, property.model.type.getEnumConstants()[b.get()]);
                    writers[i] = (o, b) -> b.put((byte) ((Enum) model.get(o, property)).ordinal());
                } else if (size < 128) {
                    width[i] = 2;
                    readers[i] = (o, b) -> model.set(o, property, property.model.type.getEnumConstants()[b.getShort()]);
                    writers[i] = (o, b) -> b.putShort((short) ((Enum) model.get(o, property)).ordinal());
                } else {
                    width[i] = 4;
                    readers[i] = (o, b) -> model.set(o, property, property.model.type.getEnumConstants()[b.getInt()]);
                    writers[i] = (o, b) -> b.putInt(((Enum) model.get(o, property)).ordinal());
                }
            } else if (Number.class.isAssignableFrom(c)) {
                width[i] = 4;
                readers[i] = (o, b) -> model.set(o, property, Num.of(b.getFloat()));
                writers[i] = (o, b) -> b.putFloat(((Num) model.get(o, property)).floatValue());
            } else {
                throw new IllegalArgumentException("Unspported property type [" + c.getName() + "] on " + type.getName() + ".");
            }
        }
        this.widthTotal = IntStream.of(width).sum();
    }

    /**
     * Insert items.
     * 
     * @param items A list of items to insert.
     * @return Chainable API.
     */
    public void write(long timestamp, T item) {
        long remaining = timestamp % duration;
        long index = timestamp - remaining;
        long height = remaining / span.seconds;

        ByteBuffer writer = ByteBuffer.allocate(widthTotal);
        for (int i = 0; i < width.length; i++) {
            writers[i].accept(item, writer);
        }

        try (FileChannel channel = FileChannel.open(dir.resolve(index + ".db"), WRITE, CREATE)) {
            channel.position(height * widthTotal);
            channel.write(writer.flip());
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    public T read(long timestamp) {
        return read(timestamp, 1).to().exact();
    }

    public Signal<T> read(long timestamp, int size) {
        return new Signal<T>((observer, disposer) -> {
            long remaining = timestamp % duration;
            long index = timestamp - remaining;
            long height = remaining / span.seconds;

            try (FileChannel ch = FileChannel.open(dir.resolve(index + ".db"), READ)) {
                ByteBuffer buffer = ByteBuffer.allocate(widthTotal);

                for (int i = 0; i < size; i++) {
                    ch.position((height + i) * widthTotal);
                    ch.read(buffer);
                    buffer.flip();

                    T item = I.make(model.type);
                    for (int k = 0; k < width.length; k++) {
                        readers[k].accept(item, buffer);
                    }
                    observer.accept(item);
                    buffer.flip();
                }
                observer.complete();
            } catch (IOException e) {
                observer.error(e);
            }
            return disposer;
        });
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
    public static <T> TimeDB<T> define(Span span, Class<T> modelType, Path root) {
        return new TimeDB(span, modelType, root);
    }
}
