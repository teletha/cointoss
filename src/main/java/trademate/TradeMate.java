/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import java.util.List;
import java.util.Random;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.execution.LogType;
import cointoss.market.MarketServiceProvider;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import kiss.I;
import kiss.Managed;
import kiss.Signal;
import kiss.Singleton;
import psychopath.Locator;
import viewtify.Viewtify;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.ui.dock.DockSystem;

@Managed(value = Singleton.class)
public class TradeMate extends View {

    /**
     * {@inheritDoc}
     */
    @Override
    protected ViewDSL declareUI() {
        return new ViewDSL() {
            {
                $(DockSystem.UI);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        DockSystem.initialize();

        // ========================================================
        // Clock in Title bar
        // ========================================================
        Chrono.seconds().map(Chrono.DateDayTime::format).combineLatest(Wisdom.random()).on(Viewtify.UIThread).to(v -> {
            stage().v.setTitle(v.ⅰ.substring(5) + "  " + v.ⅱ);
        });

        MarketServiceProvider.availableProviders()
                .on(Viewtify.WorkerThread)
                .flatIterable(MarketServiceProvider::markets)
                .take(MarketService::supportHistoricalTrade)
                .to(service -> {
                    service.log.fromToday(LogType.Fast).to();
                });
    }

    /**
     * Managing words of widsom.
     */
    private static class Wisdom {

        private static final List<String> words = Locator.file("wisdom.txt").lines().toList();

        private static final Random random = new Random();

        private static Signal<String> random() {
            return Chrono.minutes().map(v -> words.get(random.nextInt(words.size())));
        }
    }

    /**
     * Entry point.
     */
    public static void main(String[] args) {
        I.load(Market.class);

        // activate application
        Viewtify.application() //
                .error((msg, error) -> {
                    I.error(msg);
                    I.error(error);
                })
                .icon("icon/app.png")
                .onTerminating(EfficientWebSocket::shutdownNow)
                .activate(TradeMate.class);
    }
}