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

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.ToLongFunction;
import java.util.stream.IntStream;

import cointoss.ticker.Span;
import cointoss.util.arithmetic.Num;
import kiss.model.Model;
import kiss.model.Property;

public class FeatherDefinition<T> {

    final Span span;

    final long duration;

    final Model<T> model;

    final ToLongFunction<T> timestamper;

    final int[] width;

    final int widthTotal;

    /** The best size while item reading. */
    int readerSize;

    /** The best size while item writing. */
    int writerSize;

    /** The best size while item writing. */
    int fileSize;

    final BiConsumer<T, ByteBuffer>[] readers;

    final BiConsumer<T, ByteBuffer>[] writers;

    /**
     * Define database.
     * 
     * @param type
     * @param timestamper
     */
    FeatherDefinition(Span span, Class<T> type, ToLongFunction<T> timestamper) {
        this.span = span;
        this.duration = span.seconds * 100000;
        this.model = Model.of(type);
        this.timestamper = Objects.requireNonNull(timestamper);

        List<Property> properties = model.properties();
        this.width = new int[properties.size()];
        this.readers = new BiConsumer[properties.size()];
        this.writers = new BiConsumer[properties.size()];

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
            }
        }
        this.widthTotal = IntStream.of(width).sum();

        maxDataFileSize(512);
        maxReaderMemorySize(8);
        maxWriterMemorySize(32);
    }

    /**
     * Configure time span.
     * 
     * @param span
     * @return
     */
    public FeatherDefinition<T> maxDataFileSize(int mb) {
        if (0 < mb) {
            this.fileSize = mb * 1024 * 1024 / widthTotal;
        }
        return this;
    }

    /**
     * Configure time span.
     * 
     * @param span
     * @return
     */
    public FeatherDefinition<T> maxReaderMemorySize(int mb) {
        if (0 < mb) {
            this.readerSize = mb * 1024 * 1024 / widthTotal;
        }
        return this;
    }

    /**
     * Configure time span.
     * 
     * @param span
     * @return
     */
    public FeatherDefinition<T> maxWriterMemorySize(int mb) {
        if (0 < mb) {
            this.writerSize = mb * 1024 * 1024 / widthTotal;
        }
        return this;
    }

    /**
     * Build database.
     * 
     * @param name
     * @return
     */
    public FeatherDB<T> createTable(String name) {
        return new FeatherDB(name, this);
    }
}
