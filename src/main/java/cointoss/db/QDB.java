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

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Function;

import cointoss.util.arithmetic.Num;
import io.questdb.cairo.CairoConfiguration;
import io.questdb.cairo.CairoEngine;
import io.questdb.cairo.DefaultCairoConfiguration;
import io.questdb.cairo.TableUtils;
import io.questdb.cairo.TableWriter;
import io.questdb.cairo.TableWriter.Row;
import io.questdb.cairo.sql.Record;
import io.questdb.cairo.sql.RecordCursor;
import io.questdb.cairo.sql.RecordCursorFactory;
import io.questdb.griffin.SqlCompiler;
import io.questdb.griffin.SqlException;
import io.questdb.griffin.SqlExecutionContext;
import io.questdb.griffin.SqlExecutionContextImpl;
import io.questdb.std.str.Path;
import kiss.I;
import kiss.model.Model;
import kiss.model.Property;
import psychopath.Directory;
import psychopath.Locator;

public class QDB<T> {

    private static final Directory root = Locator.directory(".log/db");

    static {
        root.create();
    }

    private static final CairoConfiguration config = new DefaultCairoConfiguration(".log/db");

    private static final CairoEngine engine = new CairoEngine(config);

    private final String table;

    private final Model<T> model;

    private final List<Property> properties;

    private final Property timestampProperty;

    private final SqlExecutionContext context = new SqlExecutionContextImpl(engine, 1);

    public QDB(String tableName, Class<T> type, String timestampPropertyName) {
        this.table = Objects.requireNonNull(tableName);
        this.model = Model.of(type);
        this.properties = model.properties();
        this.timestampProperty = Objects.requireNonNull(model.property(timestampPropertyName));

        if (TableUtils.exists(engine.getConfiguration().getFilesFacade(), new Path(), engine.getConfiguration()
                .getRoot(), tableName) == TableUtils.TABLE_DOES_NOT_EXIST) {
            StringJoiner joiner = new StringJoiner(", ", "create table " + tableName + " (", ")");
            for (Property property : properties) {
                Class t = property.model.type;
                String propertyType = "";
                if (property == timestampProperty) {
                    propertyType = "timestamp";
                } else if (t == int.class) {
                    propertyType = "int";
                } else if (t == long.class) {
                    propertyType = "long";
                } else if (t == double.class || t == Num.class) {
                    propertyType = "double";
                }

                joiner.add(property.name + " " + propertyType);
            }
            executeQuery(joiner.toString(), null);
        }
    }

    private <R> R executeQuery(String query, Function<Record, R> result) {
        try (SqlCompiler compiler = new SqlCompiler(engine)) {
            try (RecordCursorFactory factory = compiler.compile(query, context).getRecordCursorFactory()) {
                if (factory != null) {
                    try (RecordCursor cursor = factory.getCursor(context)) {
                        if (cursor.hasNext()) {
                            Record record = cursor.getRecord();
                            if (record != null) {
                                return result.apply(record);
                            }
                        }
                    }
                }
                return null;
            }
        } catch (SqlException e) {
            throw I.quiet(e);
        }
    }

    public void insert(T item) {

    }

    public void insert(Iterable<T> items) {
        try (TableWriter writer = engine.getWriter(context.getCairoSecurityContext(), table)) {
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
        return executeQuery("select avg(" + propertyName + ") from " + table, record -> record.getDouble(0));
    }

    public long count() {
        return executeQuery("select count() from " + table, record -> record.getLong(0));
    }

    public double sum(String propertyName) {
        return executeQuery("select sum(" + propertyName + ") from " + table, record -> record.getDouble(0));
    }

    public double max(String propertyName) {
        return executeQuery("select max(" + propertyName + ") from " + table, record -> record.getDouble(0));
    }

    public double min(String propertyName, String... where) {
        if (where.length != 1) {
            return executeQuery("select min(" + propertyName + ") from " + table, record -> record.getDouble(0));
        } else {
            return executeQuery("select min(" + propertyName + ") from " + table + " where " + where[0], record -> record.getDouble(0));
        }
    }

    public void selectAll(Consumer<T> items) {
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

    public static class Bean {
        public int id;

        public long time;

        /**
         * @param id
         * @param value
         */
        public Bean(int id, long time) {
            this.id = id;
            this.time = time;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "Bean [id=" + id + ", time=" + time + "]";
        }
    }

    public static void main(String[] args) throws SqlException {
        // List<Bean> beans = new ArrayList();
        // for (int i = 0; i < 100000000; i++) {
        // beans.add(new Bean(i, i));
        // }

        QDB<Bean> db = new QDB<>("bean", Bean.class, "time");
        // db.insert(beans);
        System.out.println(db.max("time") + "   " + db.min("time", "20 < time") + "   " + db.avg("time") + "   " + db.sum("time"));
    }
}
