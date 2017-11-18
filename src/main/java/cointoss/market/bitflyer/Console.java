/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitflyer;

import static java.util.concurrent.TimeUnit.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import cointoss.ExecutionFlow;
import cointoss.Market;
import cointoss.Trading;
import viewtify.View;
import viewtify.Viewtify;
import viewtify.ui.UIListView;

/**
 * @version 2017/11/19 6:48:15
 */
public class Console extends View {

    private @FXML UIListView<String> console;

    private final ObservableList<String> messages = FXCollections.observableList(new LinkedList());

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        console.values(messages);

        Viewtify.inWorker(() -> {
            return new Market(BitFlyer.FX_BTC_JPY.service(), BitFlyer.FX_BTC_JPY.log().fromLast(10, ChronoUnit.SECONDS), new Trader());
        });
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
                Viewtify.inUI(() -> {
                    messages.add(0, builder.toString());

                    if (100 < messages.size()) {
                        messages.remove(messages.size() - 1);
                    }
                });
            });
        }
    }
}
