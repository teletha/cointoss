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

public class TableDefinition<T> {

    final Span span;

    final long duration;

    final Model<T> model;

    final ToLongFunction<T> timestampExtractor;

    final int[] width;

    final int widthTotal;

    final BiConsumer<T, ByteBuffer>[] writers;

    public TableDefinition(Span span, Class<T> type, ToLongFunction<T> timestampExtractor) {
        this.span = Objects.requireNonNull(span);
        this.duration = span.seconds * 1024 * 1024;
        this.model = Model.of(type);
        this.timestampExtractor = Objects.requireNonNull(timestampExtractor);

        List<Property> properties = model.properties();
        this.width = new int[properties.size()];
        this.writers = new BiConsumer[properties.size()];

        for (int i = 0; i < width.length; i++) {
            Property property = properties.get(i);
            Class c = property.model.type;
            if (c == byte.class) {
                width[i] = 1;
                writers[i] = (o, b) -> b.put((byte) model.get(o, property));
            } else if (c == short.class) {
                width[i] = 2;
                writers[i] = (o, b) -> b.putShort((short) model.get(o, property));
            } else if (c == char.class) {
                width[i] = 2;
                writers[i] = (o, b) -> b.putChar((char) model.get(o, property));
            } else if (c == int.class) {
                width[i] = 4;
                writers[i] = (o, b) -> b.putInt((int) model.get(o, property));
            } else if (c == float.class) {
                width[i] = 4;
                writers[i] = (o, b) -> b.putFloat((float) model.get(o, property));
            } else if (c == long.class) {
                width[i] = 8;
                writers[i] = (o, b) -> b.putLong((long) model.get(o, property));
            } else if (c == double.class) {
                width[i] = 8;
                writers[i] = (o, b) -> b.putDouble((double) model.get(o, property));
            } else if (c.isEnum()) {
                int size = c.getEnumConstants().length;
                if (size < 8) {
                    width[i] = 1;
                    writers[i] = (o, b) -> b.put((byte) ((Enum) model.get(o, property)).ordinal());
                } else if (size < 128) {
                    width[i] = 2;
                    writers[i] = (o, b) -> b.putShort((short) ((Enum) model.get(o, property)).ordinal());
                } else {
                    width[i] = 4;
                    writers[i] = (o, b) -> b.putInt(((Enum) model.get(o, property)).ordinal());
                }
            } else if (Number.class.isAssignableFrom(c)) {
                width[i] = 8;
                writers[i] = (o, b) -> b.putDouble(((Num) model.get(o, property)).doubleValue());
            }
        }
        this.widthTotal = IntStream.of(width).sum();
    }

    public FileDB<T> build(String name) {
        return new FileDB(name, this);
    }
}
