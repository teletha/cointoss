/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import static java.util.concurrent.TimeUnit.*;

import java.time.LocalTime;
import java.util.LinkedList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import cointoss.Trader;
import cointoss.ticker.ExecutionFlow;
import cointoss.util.Chrono;
import cointoss.util.Num;
import viewtify.UI;
import viewtify.View;
import viewtify.Viewtify;
import viewtify.ui.UIListView;

/**
 * @version 2017/11/19 6:48:15
 */
public class ConsoleView extends View {

    /** The maximum line size. */
    private static final int MAX = 3000;

    private @UI SettingView setting;

    private @UI TradingView view;

    private @UI UIListView<String> console;

    final ObservableList<String> messages = FXCollections.observableList(new LinkedList());

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        console.values(messages);

        Viewtify.inWorker(() -> {
            view.market().add(new Dumper());
        });
    }

    /**
     * Write message to console.
     * 
     * @param message
     */
    public void write(String message, Object... params) {
        Viewtify.inUI(() -> {
            messages.add(0, String.format(message, params));

            if (MAX < messages.size()) {
                messages.remove(messages.size() - 1);
            }
        });
    }

    /**
     * @version 2017/11/19 6:52:53
     */
    private class Dumper extends Trader {

        /**
         * {@inheritDoc}
         */
        @Override
        protected void initialize() {
            market.timeline.throttle(1000, MILLISECONDS).to(e -> {
                ExecutionFlow flow = market.flow;

                StringBuilder builder = new StringBuilder();
                builder.append(LocalTime.now().format(Chrono.Time)).append(" ");
                builder.append(flow.latest.price).append(" ");
                builder.append(flow.volume().format(2)).append("   ");
                builder.append(market.flow75.volume().format(2)).append("   ");
                builder.append(market.flow100.volume().format(2)).append("   ");
                builder.append(market.flow200.volume().format(2)).append("   ");
                builder.append(market.flow300.volume().format(2)).append("   ");

                Num upPotential = market.flow.estimateUpPotential();
                Num downPotential = market.flow.estimateDownPotential();
                builder.append(upPotential)
                        .append("   ")
                        .append(downPotential)
                        .append("   ")
                        .append(upPotential.abs().minus(downPotential).divide(100000).scale(4))
                        .append("   ");

                // 値段が上がりづらい要因
                // ・買いが少ない long volumeの量が小さい
                // ・売り方が厚い longPriceIncrease / longVolumeが小さい
                write(builder.toString());
            });
        }
    }
}
