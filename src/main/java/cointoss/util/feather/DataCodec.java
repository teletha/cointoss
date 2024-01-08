/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util.feather;

import java.nio.ByteBuffer;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import cointoss.util.Chrono;
import cointoss.util.arithmetic.Num;
import kiss.Extensible;
import kiss.I;
import kiss.Model;
import kiss.Property;

/**
 * Data serializer and deserializer which can convert data from/to bytes.
 */
public abstract class DataCodec<T> implements Extensible {

    /** The automatic data type collection. */
    private static final Map<Model, DataCodec.AutoDataCodec> autos = new ConcurrentHashMap();

    /**
     * Total bytes size of this data type.
     * 
     * @return The total size.
     */
    public abstract int size();

    /**
     * Convert from bytes to data.
     * 
     * @param reader A byte reader.
     * @return A restored data.
     */
    public abstract T read(long time, ByteBuffer reader);

    /**
     * Convert from data to bytes.
     * 
     * @param item A data to store.
     * @param writer A byte writer.
     */
    public abstract void write(T item, ByteBuffer writer);

    /**
     * Get the {@link Model} based data type for the specified type.
     * 
     * @param <T> A data class.
     * @param type A data class.
     * @return The automatically derived {@link DataCodec}.
     */
    public static <T> DataCodec<T> of(Class<T> type) {
        return of(Model.of(type));
    }

    /**
     * Get the {@link Model} based data type for the specified type.
     * 
     * @param <T> A data class.
     * @param model A data model.
     * @return The automatically derived {@link DataCodec}.
     */
    public static <T> DataCodec<T> of(Model<T> model) {
        DataCodec found = I.find(DataCodec.class, model.type);
        if (found != null) {
            return found;
        } else {
            return autos.computeIfAbsent(model, DataCodec.AutoDataCodec::new);
        }
    }

    /**
     * {@link Model} based automatic data type.
     */
    private static class AutoDataCodec<T> extends DataCodec<T> {

        /** The data type. */
        private final Class<T> type;

        /** The total size. */
        private final int size;

        /** The data readers. */
        private final BiFunction<T, ByteBuffer, T>[] readers;

        /** The data writers. */
        private final BiConsumer<T, ByteBuffer>[] writers;

        /**
         * @param model
         */
        private AutoDataCodec(Model<T> model) {
            this.type = model.type;

            int i = 0;
            int width = 0;
            Collection<Property> properties = model.properties();
            this.readers = new BiFunction[properties.size()];
            this.writers = new BiConsumer[properties.size()];

            for (Property property : properties) {
                Class c = property.model.type;
                if (c == boolean.class) {
                    width += 1;
                    readers[i] = (o, b) -> model.set(o, property, b.get() == 0 ? Boolean.FALSE : Boolean.TRUE);
                    writers[i] = (o, b) -> b.put((byte) (model.get(o, property) == Boolean.FALSE ? 0 : 1));
                } else if (c == byte.class) {
                    width += 1;
                    readers[i] = (o, b) -> model.set(o, property, b.get());
                    writers[i] = (o, b) -> b.put((byte) model.get(o, property));
                } else if (c == short.class) {
                    width += 2;
                    readers[i] = (o, b) -> model.set(o, property, b.getShort());
                    writers[i] = (o, b) -> b.putShort((short) model.get(o, property));
                } else if (c == char.class) {
                    width += 2;
                    readers[i] = (o, b) -> model.set(o, property, b.getChar());
                    writers[i] = (o, b) -> b.putChar((char) model.get(o, property));
                } else if (c == int.class) {
                    width += 4;
                    readers[i] = (o, b) -> model.set(o, property, b.getInt());
                    writers[i] = (o, b) -> b.putInt((int) model.get(o, property));
                } else if (c == float.class) {
                    width += 4;
                    readers[i] = (o, b) -> model.set(o, property, b.getFloat());
                    writers[i] = (o, b) -> b.putFloat((float) model.get(o, property));
                } else if (c == long.class) {
                    width += 8;
                    readers[i] = (o, b) -> model.set(o, property, b.getLong());
                    writers[i] = (o, b) -> b.putLong((long) model.get(o, property));
                } else if (c == double.class) {
                    width += 8;
                    readers[i] = (o, b) -> model.set(o, property, b.getDouble());
                    writers[i] = (o, b) -> b.putDouble((double) model.get(o, property));
                } else if (c.isEnum()) {
                    int size = c.getEnumConstants().length;
                    if (size < 8) {
                        width += 1;
                        readers[i] = (o, b) -> model.set(o, property, property.model.type.getEnumConstants()[b.get()]);
                        writers[i] = (o, b) -> b.put((byte) ((Enum) model.get(o, property)).ordinal());
                    } else if (size < 128) {
                        width += 2;
                        readers[i] = (o, b) -> model.set(o, property, property.model.type.getEnumConstants()[b.getShort()]);
                        writers[i] = (o, b) -> b.putShort((short) ((Enum) model.get(o, property)).ordinal());
                    } else {
                        width += 4;
                        readers[i] = (o, b) -> model.set(o, property, property.model.type.getEnumConstants()[b.getInt()]);
                        writers[i] = (o, b) -> b.putInt(((Enum) model.get(o, property)).ordinal());
                    }
                } else if (Num.class.isAssignableFrom(c)) {
                    width += 4;
                    readers[i] = (o, b) -> model.set(o, property, Num.of(b.getFloat()));
                    writers[i] = (o, b) -> b.putFloat(((Num) model.get(o, property)).floatValue());
                } else if (ZonedDateTime.class.isAssignableFrom(c)) {
                    width += 8;
                    readers[i] = (o, b) -> {
                        long time = b.getLong();
                        return model.set(o, property, time == -1 ? null : Chrono.utcByMills(time));
                    };
                    writers[i] = (o, b) -> {
                        ZonedDateTime time = (ZonedDateTime) model.get(o, property);
                        b.putLong(time == null ? -1 : time.toInstant().toEpochMilli());
                    };
                } else {
                    throw new IllegalArgumentException("Unspported property type [" + c.getName() + "] on " + model.type.getName() + ".");
                }
                i++;
            }
            this.size = width;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            return size;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(T item, ByteBuffer buffer) {
            for (int i = 0; i < writers.length; i++) {
                writers[i].accept(item, buffer);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public T read(long time, ByteBuffer buffer) {
            T item = I.make(type);
            for (int i = 0; i < readers.length; i++) {
                item = readers[i].apply(item, buffer);
            }
            return item;
        }
    }
}