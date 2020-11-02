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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
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
import io.questdb.griffin.engine.functions.bind.BindVariableService;
import kiss.I;
import kiss.Signal;
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

    /** Pre Thread */
    private static final ThreadLocal<SqlExecutionContext> contexts = ThreadLocal.withInitial(() -> new SqlExecutionContextImpl(engine, 1));

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

    /** The unit of timestamp. */
    private final TimeUnit timestampUnit;

    /** The latest timestamp in database. */
    private long latestTimestamp;

    @SuppressWarnings("serial")
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

    /**
     * Hide constructor.
     * 
     * @param table
     * @param type
     */
    private TimeseriseDatabase(String table, Class<T> type) {
        this.table = table;
        this.model = Model.of(type);
        this.properties = model.properties();

        // search timestamp property
        Field field = I.signal(type.getDeclaredFields()).take(f -> f.isAnnotationPresent(Timestamp.class)).first().to().v;
        if (field == null) {
            throw new IllegalArgumentException("The timestamp property is not found in " + type + ", you must annotate the property by " + Timestamp.class);
        }

        String timestampPropertyName = field.getName();
        this.timestampProperty = model.property(timestampPropertyName);
        this.timestampUnit = field.getAnnotation(Timestamp.class).value();

        if (!existTable(table)) {
            String columns = properties.stream().map(p -> p.name + " " + typeToName(p)).collect(Collectors.joining(","));
            query("CREATE TABLE " + table + " (" + columns + ") timestamp(" + timestampPropertyName + ")");
        } else {
            latestTimestamp = queryAsLong("SELECT " + timestampPropertyName + " FROM " + table + " ORDER BY " + timestampPropertyName + " DESC LIMIT 1");
        }
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

    /**
     * Execute query which returns no value.
     * 
     * @param query Your query which returns no value.
     * @return Result.
     */
    private void query(String query) {
        executeQuery(void.class, query, null, null);
    }

    /**
     * Execute query which returns single value.
     * 
     * @param query Your query which returns single value.
     * @return Result.
     */
    public final int queryAsInt(String query) {
        return queryAsInt(query, null);
    }

    /**
     * Execute query which returns single value.
     * 
     * @param query Your query which returns single value.
     * @return Result.
     */
    public final int queryAsInt(String query, Consumer<BindVariableService> variables) {
        return executeQuery(int.class, query, variables, rec -> rec.getInt(0));
    }

    /**
     * Execute query which returns single value.
     * 
     * @param query Your query which returns single value.
     * @return Result.
     */
    public final long queryAsLong(String query) {
        return queryAsLong(query, null);
    }

    /**
     * Execute query which returns single value.
     * 
     * @param query Your query which returns single value.
     * @return Result.
     */
    public final long queryAsLong(String query, Consumer<BindVariableService> variables) {
        return executeQuery(long.class, query, variables, rec -> rec.getLong(0));
    }

    /**
     * Execute query which returns single value.
     * 
     * @param query Your query which returns single value.
     * @return Result.
     */
    public final double queryAsDouble(String query) {
        return queryAsDouble(query, null);
    }

    /**
     * Execute query which returns single value.
     * 
     * @param query Your query which returns single value.
     * @return Result.
     */
    public final double queryAsDouble(String query, Consumer<BindVariableService> variables) {
        return executeQuery(double.class, query, variables, rec -> rec.getDouble(0));
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
    private <R> R executeQuery(Class<R> type, String query, Consumer<BindVariableService> variables, Function<Record, R> decoder) {
        SqlExecutionContext context = contexts.get();
        if (variables != null) variables.accept(context.getBindVariableService());

        RecordCursorFactory factory = factory(query);
        if (factory == null) {
            return null;
        }

        try (RecordCursor cursor = factory.getCursor(context)) {
            while (cursor.hasNext()) {
                return decoder.apply(cursor.getRecord());
            }
        }

        throw new Error("This query [" + query + "] doesn't return " + type.getSimpleName() + " value.");
    }

    private RecordCursorFactory factory(String query) {
        return factories.computeIfAbsent(query, key -> {
            try (SqlCompiler compiler = new SqlCompiler(engine)) {
                return compiler.compile(query, contexts.get()).getRecordCursorFactory();
            } catch (SqlException e) {
                throw new Error("The query [" + query + "] is failed.", e);
            }
        });
    }

    /**
     * Format query.
     * 
     * @param base A base query.
     * @param additions A list of additional queries.
     * @return A merged query.
     */
    private String format(String base, String... additions) {
        String joined = String.join(" ", additions);
        if (joined.isBlank()) {
            return base;
        } else {
            return base + " " + joined;
        }
    }

    /**
     * Add itmes.
     * 
     * @param items A list of items to add.
     */
    public final void insert(T... items) {
        insert(I.signal(items));
    }

    /**
     * Add items.
     * 
     * @param items A list of items to add.
     */
    public final void insert(Iterable<T> items) {
        insert(I.signal(items));
    }

    /**
     * Add items.
     * 
     * @param items A list of items to add.
     */
    public final void insert(Signal<T> items) {
        try (TableWriter writer = engine.getWriter(contexts.get().getCairoSecurityContext(), table)) {
            items.to(item -> {
                long timestamp = timestampUnit.toMicros((long) model.get(item, timestampProperty));
                if (latestTimestamp < timestamp) {
                    latestTimestamp = timestamp;

                    Row row = writer.newRow(timestamp);
                    for (int i = 0; i < properties.size(); i++) {
                        Property property = properties.get(i);
                        if (property == timestampProperty) {
                            continue;
                        }

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
            });
            writer.commit();
        }
    }

    /**
     * Calculate the average of the target property.
     * 
     * @param propertyName A target property name.
     * @param additionalQuery A list of additional conditions.
     * @return
     */
    public final double avg(String propertyName, String... additionalQuery) {
        return queryAsDouble(format("SELECT avg(" + propertyName + ") FROM " + table, additionalQuery));
    }

    /**
     * Get the item size.
     * 
     * @param additionalQuery A list of additional conditions.
     * @return
     */
    public final long count(String... additionalQuery) {
        return queryAsLong(format("SELECT count() FROM " + table, additionalQuery));
    }

    /**
     * Calculate the maximum value of the target property.
     * 
     * @param propertyName A target property name.
     * @param additionalQuery A list of additional conditions.
     * @return
     */
    public final double max(String propertyName, String... additionalQuery) {
        return queryAsDouble(format("SELECT max(" + propertyName + ") FROM " + table, additionalQuery));
    }

    /**
     * Calculate the minimum value of the target property.
     * 
     * @param propertyName A target property name.
     * @param additionalQuery A list of additional conditions.
     * @return
     */
    public final double min(String propertyName, String... additionalQuery) {
        return queryAsDouble(format("SELECT min(" + propertyName + ") FROM " + table, additionalQuery));
    }

    /**
     * Calculate the sum value of the target property.
     * 
     * @param propertyName A target property name.
     * @param additionalQuery A list of additional conditions.
     * @return
     */
    public final double sum(String propertyName, String... additionalQuery) {
        return queryAsDouble(format("SELECT sum(" + propertyName + ") FROM " + table, additionalQuery));
    }

    /**
     * Select items by your condition.
     * 
     * @param additinalQuery Condition expression.
     * @return The matched items.
     */
    public final Signal<T> select(String additinalQuery) {
        String query = additinalQuery == null || additinalQuery.isBlank() ? table : "SELECT * FROM " + table + " " + additinalQuery;

        return new Signal<>((observer, disposer) -> {
            RecordCursorFactory factory = factory(query);
            try (RecordCursor cursor = factory.getCursor(contexts.get())) {
                while (cursor.hasNext() && !disposer.isDisposed()) {
                    Record record = cursor.getRecord();

                    T o = I.make(model.type);
                    for (int i = 0; i < properties.size(); i++) {
                        Property property = properties.get(i);
                        Object value = null;

                        if (property == timestampProperty) {
                            value = timestampUnit.convert(record.getLong(i), TimeUnit.MICROSECONDS);
                        } else {
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
                        }

                        model.set(o, property, value);
                    }
                    observer.accept(o);
                }
                observer.complete();
            } catch (Throwable e) {
                observer.error(e);
            }
            return disposer;
        });
    }

    /**
     * Select all items.
     * 
     * @return All items.
     */
    public final Signal<T> selectAll() {
        return select(null);
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
    public static <T> TimeseriseDatabase<T> create(String name, Class<T> type) {
        return new TimeseriseDatabase(name, type);
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

    /**
     * Property marker.
     */
    @Documented
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Timestamp {
        TimeUnit value() default TimeUnit.MILLISECONDS;
    }
}
