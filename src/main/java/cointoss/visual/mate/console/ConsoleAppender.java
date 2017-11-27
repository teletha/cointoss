/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.visual.mate.console;

import java.io.Serializable;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import kiss.I;

/**
 * @version 2017/11/27 13:00:47
 */
@Plugin(name = "UI", category = "Core", elementType = "appender", printObject = true)
class ConsoleAppender extends AbstractAppender {

    /** The console ui. */
    private final Console console = I.make(Console.class);

    /**
     * Log appender for console UI.
     * 
     * @param name
     * @param filter
     * @param layout
     */
    private ConsoleAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
        super(name, filter, layout);
    }

    /**
     * Create new {@link Appender}.
     * 
     * @param name
     * @param ignoreExceptions
     * @param layout
     * @param filter
     * @return
     */
    @PluginFactory
    public static ConsoleAppender createAppender(@PluginAttribute("name") String name, @PluginAttribute("ignoreExceptions") boolean ignoreExceptions, @PluginElement("Layout") Layout layout, @PluginElement("Filters") Filter filter) {
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new ConsoleAppender(name, filter, layout);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void append(LogEvent event) {
        console.write(event.getMessage().getFormattedMessage());
    }
}
