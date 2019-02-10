/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.console;

import java.util.LinkedList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import stylist.Style;
import stylist.StyleDSL;
import trademate.TradingView;
import trademate.setting.SettingView;
import viewtify.Viewtify;
import viewtify.ui.UI;
import viewtify.ui.UIListView;
import viewtify.ui.View;

/**
 * @version 2017/11/19 6:48:15
 */
public class Console extends View {

    /** The maximum line size. */
    private static final int MAX = 3000;

    /** The background logger. */
    private Logger logger;

    private SettingView setting;

    private TradingView view;

    private UIListView<String> console;

    final ObservableList<String> messages = FXCollections.observableList(new LinkedList());

    /**
     * {@inheritDoc}
     */
    @Override
    protected UI declareUI() {
        return new UI() {
            {
                $(console, S.ConsoleView);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        // create logger
        String name = view.service.marketIdentity();
        logger = LogManager.getLogger(name);
        ConsoleAppender.consoles.put(name, this);

        console.values(messages);
    }

    /**
     * Write message to console.
     * 
     * @param message
     */
    void write(String message) {
        Viewtify.inUI(() -> {
            messages.add(0, message);

            if (MAX < messages.size()) {
                messages.remove(messages.size() - 1);
            }
        });
    }

    /**
     * Write message to console.
     * 
     * @param message
     */
    public void info(String message, Object... params) {
        logger.info(message, params);
    }

    /**
     * @version 2018/08/30 12:50:36
     */
    private static class S implements StyleDSL {

        static Style ConsoleView = () -> {
        };
    }
}
