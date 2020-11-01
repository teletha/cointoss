/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.db;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import cointoss.util.arithmetic.Num;
import io.questdb.cairo.CairoConfiguration;
import io.questdb.cairo.CairoEngine;
import io.questdb.cairo.DefaultCairoConfiguration;
import io.questdb.cairo.TableWriter;
import io.questdb.cairo.TableWriter.Row;
import io.questdb.cairo.sql.Record;
import io.questdb.cairo.sql.RecordCursor;
import io.questdb.cairo.sql.RecordCursorFactory;
import io.questdb.griffin.SqlCompiler;
import io.questdb.griffin.SqlException;
import io.questdb.griffin.SqlExecutionContext;
import io.questdb.griffin.SqlExecutionContextImpl;
import kiss.I;
import kiss.model.Model;
import kiss.model.Property;
import psychopath.Directory;
import psychopath.Locator;

public class TimeseriseDatabase<T> {

    /** The database directory. */
    private static final Directory root = Locator.directory(".log/db");

    /** The QuestDB configuration. */
    private static final CairoConfiguration config = new DefaultCairoConfiguration(".log/db");

    /** Singleton */
    private static final CairoEngine engine = new CairoEngine(config);

    /** The datatype and name mapping. */
    private static final Map<Class, String> types = Map
            .of(int.class, "int", long.class, "long", double.class, "double", boolean.class, "boolean", String.class, "string", Num.class, "double");

    static {
        root.create();
    }

    /** The table name. */
    private final String table;

    /** The item model. */
    private final Model<T> model;

    /** The property list. */
    private final List<Property> properties;

    /** The timestamp property. */
    private final Property timestampProperty;

    /** The latest timestamp in database. */
    private long latestTimestamp;

    /**
     * Hide constructor.
     * 
     * @param table
     * @param type
     * @param timestampPropertyName
     */
    private TimeseriseDatabase(String table, Class<T> type, String timestampPropertyName) {
        this.table = table;
        this.model = Model.of(type);
        this.properties = model.properties();
        this.timestampProperty = model.property(timestampPropertyName);

        if (timestampProperty == null) {
            throw new IllegalArgumentException("The property '" + timestampPropertyName + "' is not found in " + type + ".");
        }

        if (!existTable(table)) {
            String columns = properties.stream().map(p -> p.name + " " + typeToName(p)).collect(Collectors.joining(","));
            query("CREATE TABLE " + table + " (" + columns + ") timestamp(" + timestampPropertyName + ")");
        } else {
            latestTimestamp = queryAsLong("SELECT " + timestampPropertyName + " FROM " + table + " LATEST BY " + timestampPropertyName);
        }
        System.out.println(latestTimestamp);
    }

    /**
     * Convert {@link Property} to type name.
     * 
     * @param property A target property.
     * @return A type name.
     */
    private String typeToName(Property property) {
        if (property == timestampProperty) {
            return "timestamp";
        }
        return types.get(property.model.type);
    }

    private final Map<String, RecordCursorFactory> factories = new LinkedHashMap<>(15, 0.75f, true) {

        @Override
        protected boolean removeEldestEntry(Entry<String, RecordCursorFactory> eldest) {
            if (25 < size()) {
                eldest.getValue().close();
                return true;
            } else {
                return false;
            }
        }
    };

    private final RecordCursorFactory factory(String query) {
        return factories.computeIfAbsent(query, key -> {
            try (SqlCompiler compiler = new SqlCompiler(engine)) {
                return compiler.compile(query, new SqlExecutionContextImpl(engine, 1)).getRecordCursorFactory();
            } catch (SqlException e) {
                throw new Error("The query [" + query + "] is failed.", e);
            }
        });
    }

    /**
     * Execute query which returns no value.
     * 
     * @param query Your query which returns no value.
     * @return Result.
     */
    private final void query(String query) {
        executeQuery(void.class, query, null);
    }

    /**
     * Execute query which returns single value.
     * 
     * @param query Your query which returns single value.
     * @return Result.
     */
    public final int queryAsInt(String query) {
        return executeQuery(int.class, query, rec -> rec.getInt(0));
    }

    /**
     * Execute query which returns single value.
     * 
     * @param query Your query which returns single value.
     * @return Result.
     */
    public final long queryAsLong(String query) {
        return executeQuery(long.class, query, rec -> rec.getLong(0));
    }

    /**
     * Execute query which returns single value.
     * 
     * @param query Your query which returns single value.
     * @return Result.
     */
    public final double queryAsDouble(String query) {
        return executeQuery(double.class, query, rec -> rec.getDouble(0));
    }

    /**
     * Execute query.
     * 
     * @param <R>
     * @param type
     * @param query
     * @param decoder
     * @return
     */
    private <R> R executeQuery(Class<R> type, String query, Function<Record, R> decoder) {
        RecordCursorFactory factory = factory(query);

        if (factory == null) {
            return null;
        }

        try (RecordCursor cursor = factory.getCursor(new SqlExecutionContextImpl(engine, 1))) {
            while (cursor.hasNext()) {
                return decoder.apply(cursor.getRecord());
            }
        } catch (Exception e) {
            throw I.quiet(e);
        }
        throw new Error("This query [" + query + "] doesn't return " + type.getSimpleName() + " value.");
    }

    public void insert(T item) {

    }

    public void insert(Iterable<T> items) {
        try (TableWriter writer = engine.getWriter(new SqlExecutionContextImpl(engine, 1).getCairoSecurityContext(), table)) {
            for (T item : items) {
                Row row = writer.newRow((long) model.get(item, timestampProperty));
                for (int i = 0; i < properties.size(); i++) {
                    Property property = properties.get(i);
                    Object value = model.get(item, property);
                    Class type = property.model.type;
                    if (type == int.class) {
                        row.putInt(i, (int) value);
                    } else if (type == long.class) {
                        row.putLong(i, (long) value);
                    } else if (type == double.class) {
                        row.putDouble(i, (double) value);
                    } else if (type == Num.class) {
                        row.putDouble(i, ((Num) value).doubleValue());
                    }
                }
                row.append();
            }
            writer.commit();
        }
    }

    public double avg(String propertyName) {
        return queryAsDouble("SELECT avg(" + propertyName + ") FROM " + table);
    }

    public long count() {
        return queryAsLong("SELECT count() FROM " + table);
    }

    public double sum(String propertyName) {
        return queryAsDouble("SELECT sum(" + propertyName + ") FROM " + table);
    }

    public double max(String propertyName) {
        return queryAsDouble("SELECT max(" + propertyName + ") FROM " + table);
    }

    public double min(String propertyName, String... where) {
        if (where.length != 1) {
            return queryAsDouble("SELECT min(" + propertyName + ") FROM " + table);
        } else {
            return queryAsDouble("SELECT min(" + propertyName + ") FROM " + table + " WHERE " + where[0]);
        }
    }

    public void selectAll(Consumer<T> items) {
        SqlExecutionContext context = new SqlExecutionContextImpl(engine, 1);

        try (SqlCompiler compiler = new SqlCompiler(engine)) {
            try (RecordCursorFactory factory = compiler.compile(table, context).getRecordCursorFactory()) {
                try (RecordCursor cursor = factory.getCursor(context)) {
                    final io.questdb.cairo.sql.Record record = cursor.getRecord();
                    while (cursor.hasNext()) {
                        T o = I.make(model.type);
                        for (int i = 0; i < properties.size(); i++) {
                            Property property = properties.get(i);
                            Object value = null;
                            Class t = property.model.type;
                            if (t == int.class) {
                                value = record.getInt(i);
                            } else if (t == long.class) {
                                value = record.getLong(i);
                            } else if (t == double.class) {
                                value = record.getDouble(i);
                            } else if (t == Num.class) {
                                value = Num.of(record.getDouble(i));
                            }
                            model.set(o, property, value);
                        }
                        items.accept(o);
                    }
                }
            } catch (SqlException e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * Create database accessor.
     * 
     * @param <T>
     * @param name
     * @param type
     * @param timestampPropertyName
     * @return
     */
    public static <T> TimeseriseDatabase<T> create(String name, Class<T> type, String timestampPropertyName) {
        return new TimeseriseDatabase(name, type, timestampPropertyName);
    }

    /**
     * Helper to check whether the target table exists or not.
     * 
     * @param table A target table name.
     * @return Result.
     */
    public static boolean existTable(String table) {
        return root.directory(table).isPresent();
    }

    /**
     * Helper to clear the target table completely.
     * 
     * @param table A target table name.
     */
    public static void clearTable(String table) {
        root.directory(table).delete();
    }
}
