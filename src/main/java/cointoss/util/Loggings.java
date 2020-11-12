/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class Loggings {

    private static final BackTestPolicy BackTestPolicy = new BackTestPolicy();

    public static Logger getTradingLogger(String name, boolean backtest) {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        Layout layout = PatternLayout.newBuilder().withPattern("%m%n").withConfiguration(config).build();

        String type = backtest ? "backtest" : "trading";
        Appender appender = RollingFileAppender.newBuilder()
                .setName(name)
                .withAppend(backtest ? false : true)
                .withImmediateFlush(true)
                .withBufferedIo(true)
                .withFileName(".log/trading/" + name + "/" + type + ".log")
                .withFilePattern(".log/trading/" + name + "/" + type + "%d{yyyyMMdd}.log")
                .setLayout(layout)
                .withPolicy(backtest ? BackTestPolicy : TimeBasedTriggeringPolicy.newBuilder().build())
                .build();
        appender.start();
        AppenderRef[] refs = {AppenderRef.createAppenderRef(name, null, null)};

        LoggerConfig loggerConfig = LoggerConfig.createLogger(false, Level.DEBUG, name, "", refs, null, config, null);
        loggerConfig.addAppender(appender, null, null);
        config.addLogger(name, loggerConfig);
        context.updateLoggers();

        return context.getLogger(name);
    }

    public static void requestTradingLoggerReset() {
        BackTestPolicy.needReset = true;
    }

    /**
     * 
     */
    private static class BackTestPolicy implements TriggeringPolicy {

        private boolean needReset = true;

        /**
         * {@inheritDoc}
         */
        @Override
        public void initialize(RollingFileManager manager) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isTriggeringEvent(LogEvent logEvent) {
            if (needReset) {
                needReset = false;
                return true;
            } else {
                return false;
            }
        }
    }
}