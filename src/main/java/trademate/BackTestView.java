/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import static trademate.setting.SettingStyles.*;
import static transcript.Transcript.*;

import java.time.Period;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.execution.ExecutionLog;
import cointoss.market.MarketProvider;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.ticker.TickSpan;
import cointoss.util.Chrono;
import trademate.chart.ChartView;
import transcript.Transcript;
import viewtify.Viewtify;
import viewtify.ui.UI;
import viewtify.ui.UIButton;
import viewtify.ui.UIComboBox;
import viewtify.ui.UIDatePicker;
import viewtify.ui.View;
import viewtify.ui.helper.User;

public class BackTestView extends View {

    private static final Transcript LogIsNotFound = Transcript.en("No logs were found for the specified date.");

    private static final Transcript EndDateMustBeAfterStartDate = Transcript.en("The end date must be after the start date.");

    private UIComboBox<MarketService> market;

    private UIDatePicker startDate;

    private UIDatePicker endDate;

    private UIButton startButton;

    private ChartView chart;

    /**
     * {@inheritDoc}
     */
    @Override
    protected UI declareUI() {
        return new UI() {
            {
                $(hbox, () -> {
                    $(chart);
                    $(vbox, () -> {
                        $(market);
                        $(hbox, FormRow, () -> {
                            label(en("Start Date"), FormLabel);
                            $(startDate, FormInput);
                        });
                        $(hbox, FormRow, () -> {
                            label(en("End Date"), FormLabel);
                            $(endDate, FormInput);
                        });
                        $(startButton);
                    });
                });
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        market.values(0, MarketProvider.availableMarkets());
        startDate.initial(Chrono.utcNow().minusDays(10)).uneditable().requireWhen(market).require(() -> {
            ExecutionLog log = market.value().log;

            assert startDate.isBeforeOrSame(log.lastCacheDate()) : LogIsNotFound;
            assert startDate.isAfterOrSame(log.firstCacheDate()) : LogIsNotFound;
        }).observe((o, n) -> {
            endDate.value(v -> v.plus(Period.between(o, n)));
        });

        endDate.initial(Chrono.utcNow()).uneditable().require(() -> {
            assert startDate.isBeforeOrSame(endDate) : EndDateMustBeAfterStartDate;
        });

        // Market market = new Market(BitFlyer.BTC_JPY).readLog(log -> log.at(2018, 1, 17));

        // Viewtify.Terminator.add(market);
        // chart.market.set(market);
        // chart.ticker.set(market.tickers.tickerBy(TickSpan.Minute1));

        startButton.disableWhen(startDate.isInvalid()).when(User.MouseClick).to(e -> {
            Market m = new Market(BitFlyer.FX_BTC_JPY);
            chart.market.set(m);
            chart.ticker.set(m.tickers.tickerBy(TickSpan.Minute1));
            chart.market.to(v -> v.readLog(log -> log.range(startDate.zoned(), endDate.zoned())));

            Viewtify.Terminator.add(m);
        });
    }
}
