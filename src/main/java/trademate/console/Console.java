/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.console;

import static java.util.concurrent.TimeUnit.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cointoss.ExecutionFlow;
import cointoss.Trading;
import trademate.TradingView;
import viewtify.View;
import viewtify.Viewtify;
import viewtify.ui.UIListView;

/**
 * @version 2017/11/19 6:48:15
 */
public class Console extends View {

    /** The maximum line size. */
    private static final int MAX = 3000;

    /** The background logger. */
    private Logger logger;

    private @FXML TradingView view;

    private @FXML UIListView<String> console;

    final ObservableList<String> messages = FXCollections.observableList(new LinkedList());

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        // create logger
        String name = view.provider.fullName();
        logger = LogManager.getLogger(name);
        ConsoleAppender.consoles.put(name, this);

        console.values(messages);

        Viewtify.inWorker(() -> {
            view.market().add(new Trader());
        });
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
    public void write(String message, Object... params) {
        logger.info(message, params);
    }

    /**
     * @version 2017/11/19 6:52:53
     */
    private class Trader extends Trading {

        private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        /**
         * {@inheritDoc}
         */
        @Override
        protected void initialize() {
            market.timeline.throttle(500, MILLISECONDS).to(e -> {
                ExecutionFlow flow = market.flow;

                StringBuilder builder = new StringBuilder();
                builder.append(formatter.format(LocalTime.now())).append(" ");
                builder.append(flow.latest.price).append(" ");
                builder.append(flow.volume().format(2)).append("   ");
                builder.append(market.flow75.volume().format(2)).append("   ");
                builder.append(market.flow100.volume().format(2)).append("   ");
                builder.append(market.flow200.volume().format(2)).append("   ");
                builder.append(market.flow300.volume().format(2)).append("   ");

                // 値段が上がりづらい要因
                // ・買いが少ない long volumeの量が小さい
                // ・売り方が厚い longPriceIncrease / longVolumeが小さい
                write(builder.toString());
            });
        }
    }
}
