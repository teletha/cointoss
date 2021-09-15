/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import static org.tinylog.configuration.Configuration.set;

import java.lang.reflect.Modifier;

import org.tinylog.Logger;
import org.tinylog.configuration.Configuration;
import org.tinylog.policies.Policy;

import cointoss.Currency;
import cointoss.trade.Trader;
import kiss.I;

public class TinyLog {

    public static void initialize() {
        String format = "{date:yyyy-MM-dd HH:mm:ss.SSS} {{level}|min-size=5}\t{message|indent=2}";

        // use writer thread
        set("writingthread", "true");

        // console output
        set("writerConsole", "console");
        set("writerConsole.level", "info");
        set("writerConsole.format", format);

        // file output
        set("writerFile", "rolling file");
        set("writerFile.tag", "-");
        set("writerFile.level", "trace");
        set("writerFile.format", format);
        set("writerFile.file", ".log/system/{date:yyyyMMdd}.log");
        set("writerFile.policies", "daily");
        set("writerFile.backups", "30");
        set("writerFile.buffered", "true");

        I.load(Currency.class);

        I.findAs(Trader.class).forEach(clazz -> {
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                String name = clazz.getSimpleName();
                System.out.println(name);
            }
        });

        set("writerX", "rolling file");
        set("writerX.tag", "test");
        Configuration.set("writerX.file", ".log/tiny/test/{date:yyyyMMdd}.log");
        Configuration.set("writerX.format", "{date:yyyy-MM-dd HH:mm:ss.SSS} {level} \t{message}");
        Configuration.set("writerX.policies", "daily");
        Configuration.set("writerX.backups", "30");
        Configuration.set("writerX.buffered", "true");

        Logger.info("OK");
        Logger.debug("DEBUG");

        Logger.warn("WARN");
        Logger.error(new Error("Nooo!"), "ERROR");

        Logger.tag("test").info("tagged");
    }

    public static void main(String[] args) {
        initialize();
    }

    public static class ResetablePolicy implements Policy {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean continueExistingFile(String path) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean continueCurrentFile(byte[] entry) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void reset() {
        }
    }
}
