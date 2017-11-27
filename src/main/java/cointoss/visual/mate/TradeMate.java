/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.visual.mate;

import java.net.URL;

import cointoss.util.Num;
import kiss.I;
import viewtify.Viewtify;

/**
 * @version 2017/11/13 16:58:58
 */
public class TradeMate extends Viewtify {

    static {
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");

        // Path log = Filer.locate(".log/TradeMate");
        // String pattern = log.resolve("system%i.log").toString();
        //
        // LoggerContext context = (LoggerContext) LogManager.getContext();
        // RollingRandomAccessFileAppender appender = RollingRandomAccessFileAppender.newBuilder()
        // .withName("TradeMate")
        // .withFileName("system.log")
        // .withFilePattern(pattern)
        // .withImmediateFlush(true)
        // .withStrategy(DefaultRolloverStrategy.createStrategy("1000000", null, null, "-1", null,
        // true, context.getConfiguration()))
        // .withLayout(PatternLayout.newBuilder().withPattern("%msg%n").build())
        // .withPolicy(SizeBasedTriggeringPolicy.createPolicy("64 M"))
        // .build();
        // appender.start();
        //
        // logger = context.getLogger(id);
        // logger.addAppender(appender);
        // logger.setAdditive(false);

        I.load(Num.Codec.class, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected URL fxml() {
        return ClassLoader.getSystemClassLoader().getResource("TradeMate.fxml");
    }

    /**
     * Entry point.
     * 
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }
}
