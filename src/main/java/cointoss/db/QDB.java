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

import io.questdb.cairo.CairoConfiguration;
import io.questdb.cairo.CairoEngine;
import io.questdb.cairo.DefaultCairoConfiguration;
import io.questdb.cairo.TableWriter;
import io.questdb.cairo.sql.RecordCursor;
import io.questdb.cairo.sql.RecordCursorFactory;
import io.questdb.griffin.SqlCompiler;
import io.questdb.griffin.SqlException;
import io.questdb.griffin.SqlExecutionContextImpl;
import io.questdb.std.Os;
import kiss.I;
import psychopath.Locator;

public class QDB {

    static {
        Locator.directory(".log/db").create();
    }

    private static final CairoConfiguration config = new DefaultCairoConfiguration(".log/db");

    private static final CairoEngine engine = new CairoEngine(config);

    public static void main(String[] args) {
        final SqlExecutionContextImpl ctx = new SqlExecutionContextImpl(engine, 1);
        try (SqlCompiler compiler = new SqlCompiler(engine)) {
            compiler.compile("create table abc (a int, b byte, c short, d long, e float, g double, h date, i symbol, j string, k boolean, ts timestamp) timestamp(ts)", ctx);

            try (TableWriter writer = engine.getWriter(ctx.getCairoSecurityContext(), "abc")) {
                for (int i = 0; i < 10; i++) {
                    TableWriter.Row row = writer.newRow(Os.currentTimeMicros());
                    row.putInt(0, i * 100);
                    row.putLong(3, 333);
                    row.putFloat(4, 4.44f);
                    row.putDouble(5, 5.55);
                    row.putSym(7, "xyz");
                    row.append();
                }
                writer.commit();
            }
        } catch (SqlException e) {
            throw I.quiet(e);
        }

        try (SqlCompiler compiler = new SqlCompiler(engine)) {
            try (RecordCursorFactory factory = compiler.compile("abc", ctx).getRecordCursorFactory()) {
                try (RecordCursor cursor = factory.getCursor(ctx)) {
                    final io.questdb.cairo.sql.Record record = cursor.getRecord();
                    while (cursor.hasNext()) {
                        System.out.println(record.getInt(0));
                        // access 'record' instance for field values
                    }
                }
            } catch (SqlException e) {
                throw I.quiet(e);
            }
        }
    }
}
