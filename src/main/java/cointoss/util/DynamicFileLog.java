/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class DynamicFileLog {

    public static Logger getLogger(String name) {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        Layout layout = PatternLayout.newBuilder().withPattern("%m%n").withConfiguration(config).build();

        Appender appender = RollingFileAppender.newBuilder()
                .withName(name)
                .withAppend(true)
                .withImmediateFlush(true)
                .withBufferedIo(true)
                .withFileName(".log/" + name + "/trading.log")
                .withFilePattern(".log/" + name + "/trading%d{yyyyMMdd}.log")
                .withLayout(layout)
                .withPolicy(TimeBasedTriggeringPolicy.newBuilder().build())
                .build();
        appender.start();
        AppenderRef[] refs = {AppenderRef.createAppenderRef(name, null, null)};

        LoggerConfig loggerConfig = LoggerConfig.createLogger(false, Level.DEBUG, name, "", refs, null, config, null);
        loggerConfig.addAppender(appender, null, null);
        config.addLogger(name, loggerConfig);
        context.updateLoggers();

        return context.getLogger(name);
    }
}
